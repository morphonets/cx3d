/*
Copyright (C) 2009 Frédéric Zubler, Rodney J. Douglas,
Dennis Göhlsdorf, Toby Weston, Andreas Hauri, Roman Bauer,
Sabina Pfister & Adrian M. Whatley.

This file is part of CX3D.

CX3D is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

CX3D is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CX3D.  If not, see <http://www.gnu.org/licenses/>.
*/

package ini.cx3d.simulations.tutorial;

import graphics.scenery.SceneryBase;
import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.cells.GRNModule;
import ini.cx3d.localBiology.*;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;

import java.awt.*;
import java.util.Random;
import java.util.Vector;

import static ini.cx3d.utilities.Matrix.*;

public class ActiveNeuriteChemoAttraction extends AbstractLocalBiologyModule {

	static ECM ecm = ECM.getInstance();

	private double[] direction;

	private String[] substanceID;

	private GRNModule grnModule;

	//private double branchingFactor = 0.005;
	private double branchingFactor = 0.5;

	private Random rng = new Random();

	public ActiveNeuriteChemoAttraction() {
		substanceID = new String[] {
				"wnt",
				"slitRobo",
				"semaphorinPlexin",
				"dscam"
		};
	}

	public ActiveNeuriteChemoAttraction(String[] substanceID) {
		this.substanceID = substanceID;
	}

	public ActiveNeuriteChemoAttraction(String[] substanceID, double branchingFactor) {
		this.substanceID = substanceID;
		this.branchingFactor = branchingFactor;
	}

	public ActiveNeuriteChemoAttraction(GRNModule grnModule) {
		this.grnModule = grnModule;
	}

	@Override
	public void setCellElement(CellElement cellElement) {
		super.setCellElement(cellElement);
		if(cellElement.isANeuriteElement())
			direction = cellElement.getPhysical().getAxis();
	}

	@Override
	public boolean isCopiedWhenNeuriteBranches() {
		return true;
	}

	@Override
	public boolean isDeletedAfterNeuriteHasBifurcated() {
		return true;
	}

	public AbstractLocalBiologyModule getCopy() {
		return new ActiveNeuriteChemoAttraction(substanceID);
	}

	public GRNElement getGrn(Cell cell) {
		Vector<LocalBiologyModule> localBiologyList = cell.getSomaElement().getLocalBiologyModulesList();
		for( int k = 0; k < localBiologyList.size(); k++ ) {
			if( localBiologyList.get(k) instanceof GRNModule ) {
				return ((GRNModule) localBiologyList.get(k)).getGrn();
			}
		}
		return null;
	}

	public void run() {
		PhysicalObject physical = super.cellElement.getPhysical();
		Cell cell = super.cellElement.getCell();
		GRNElement grn = getGrn(cell);

		System.out.println("Physical: " + physical + " Cell: " + cell + " GRN: " + grn);

		// Get sensor inputs:
		// - slit-robo, mediolateral
		// - semaphorins-plexins, dorsoventral
		// - wnt, anteriorposterior
		// - dscam1, self-avoidance and self/non-self

		double slitRobo = physical.getExtracellularConcentration("slitRobo");
		double[] slitRoboGrad = physical.getExtracellularGradient("slitRobo");
		double semaphorinPlexin = physical.getExtracellularConcentration("semaphorinPlexin");
		double[] semaphorinPlexinGrad = physical.getExtracellularGradient("semaphorinPlexin");
		double wnt = physical.getExtracellularConcentration("wnt");
		double[] wntGrad = physical.getExtracellularGradient("wnt");
		double dscam = physical.getExtracellularConcentration("dscam");
		double[] dscamGrad = physical.getExtracellularGradient("dscam");

		// If this is a soma element then setup inputs and evaluate GRN (to ensure one time evaluation)

		// Set GRN inputs
		grn.state.proteins.get(0).setConcentration(slitRobo);
		grn.state.proteins.get(1).setConcentration(semaphorinPlexin);
		grn.state.proteins.get(2).setConcentration(wnt);
		grn.state.proteins.get(3).setConcentration(dscam);

		// Update GRN
		grn.state.evolve(10);

		// Extract weights and probability of bifurcation
		double oldDirectionWeight = grn.state.proteins.get(grn.state.proteins.size() - 1).getConcentration();
		double randomnessWeight = grn.state.proteins.get(grn.state.proteins.size() - 2).getConcentration();
		double dscamWeight = grn.state.proteins.get(grn.state.proteins.size() - 3).getConcentration();
		double wntWeight = grn.state.proteins.get(grn.state.proteins.size() - 4).getConcentration();
		double slitRoboWeight = grn.state.proteins.get(grn.state.proteins.size() - 5).getConcentration();
		double semaphorinPlexinWeight = grn.state.proteins.get(grn.state.proteins.size() - 6).getConcentration();
		double bifurcationWeight = grn.state.proteins.get(grn.state.proteins.size() - 7).getConcentration();

		double[] newStepDirection = add(
				scalarMult(oldDirectionWeight, direction),
				scalarMult(dscamWeight, normalize(dscamGrad)),
				scalarMult(wntWeight, normalize(wntGrad)),
				scalarMult(slitRoboWeight, normalize(slitRoboGrad)),
				scalarMult(semaphorinPlexinWeight, normalize(semaphorinPlexinGrad)),
				randomNoise(randomnessWeight,3));
		double speed = 100;

		// 1) movement
		physical.movePointMass(speed, newStepDirection);
		direction = normalize(add(scalarMult(5,direction),newStepDirection));

		// 2) branching based on concentration:
		if(super.cellElement.isANeuriteElement() && ( ecm.getRandomDouble()<bifurcationWeight*branchingFactor) ){
		//if(ecm.getRandomDouble()<branchingFactor){
			System.out.println("Bifurcating");
			NeuriteElement newBranch = ((NeuriteElement) cellElement).branch();
//			Vector<LocalBiologyModule> localBiologyList = newBranch.getLocalBiologyModulesList();
//			for( int k = 0; k < localBiologyList.size(); k++ ) {
//				if( localBiologyList.get(k) instanceof GRNModule ) {
//					((GRNModule) localBiologyList.get(k)).setGrn(getGrn());
//				}
//			}
		}
	}

	public static void main(String[] args) {
		SceneryBase.xinitThreads();

		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(0L);
		Substance slitRobo = new Substance("slitRobo",Color.magenta);
		Substance semaphorinPlexin = new Substance("semaphorinPlexin",Color.cyan);
		Substance wnt = new Substance("wnt", Color.green.darker());
		// Establish convention, A-P = Z, Long = Y, M-L = X
		ecm.addArtificialGaussianConcentrationZ(wnt, 1.0, 400.0, 160.0);
		ecm.addArtificialGaussianConcentrationX(slitRobo, 1.0, 400.0, 160.0);
		ecm.addArtificialGaussianConcentrationY(semaphorinPlexin, 1.0, 400.0, 160.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		Cell c = CellFactory.getCellInstance(new double[] {0.0,0.0,0.0});
		c.setColorForAllPhysicalObjects(Param.VIOLET);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
		neurite.getPhysicalCylinder().setDiameter(2.0);

		GRNModule grnModule = new GRNModule();
		grnModule.setGrn(new GRNElement("ActiveNeuriteType1"));
		grnModule.setCell(c);
		c.getSomaElement().addLocalBiologyModule(grnModule);


		ActiveNeuriteChemoAttraction activeNeuriteModule = new ActiveNeuriteChemoAttraction(grnModule);
		neurite.addLocalBiologyModule(activeNeuriteModule);


		Scheduler.simulate();
	}
}
