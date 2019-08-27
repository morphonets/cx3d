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

package sc.iview.cx3d.simulations.tutorial;

import graphics.scenery.SceneryBase;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.cells.GRNModule;
import sc.iview.cx3d.localBiology.*;
import sc.iview.cx3d.physics.PhysicalObject;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;

import java.awt.*;
import java.util.Vector;

import static sc.iview.cx3d.utilities.Matrix.*;

public class ActiveNeuriteChemoAttraction extends AbstractLocalBiologyModule {

	static ECM ecm = ECM.getInstance();

	private double[] direction;

	private String[] substanceID;

	private GRNModule grnModule;

	//private double branchingFactor = 0.005;
	private double branchingFactor = 0.5;

	public ActiveNeuriteChemoAttraction() {
		substanceID = new String[] {
				"A",
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

		//System.out.println("Physical: " + physical + " Cell: " + cell + " GRN: " + grn);

		// Get sensor inputs:
		// - A

		double A = physical.getExtracellularConcentration("A");
		double[] AGrad = physical.getExtracellularGradient("A");

		// If this is a soma element then setup inputs and evaluate GRN (to ensure one time evaluation)

		// Set GRN inputs
		grn.state.proteins.get(0).setConcentration(A);
		grn.state.proteins.get(1).setConcentration(physical.getLength()/10f);
		grn.state.proteins.get(2).setConcentration(physical.getVolume()/10f);

		// Update GRN
		grn.state.evolve(2);

		// Extract weights and probability of bifurcation
		double oldDirectionWeight = grn.state.proteins.get(grn.state.proteins.size() - 1).getConcentration();
		double randomnessWeight = grn.state.proteins.get(grn.state.proteins.size() - 2).getConcentration();
		double AWeight = grn.state.proteins.get(grn.state.proteins.size() - 3).getConcentration();
		double bifurcationWeight = grn.state.proteins.get(grn.state.proteins.size() - 4).getConcentration();

		double[] newStepDirection = add(
				scalarMult(oldDirectionWeight, direction),
				scalarMult(AWeight, normalize(AGrad)),
				randomNoise(randomnessWeight,3));
		double speed = 100;

		// 1) movement
		physical.movePointMass(speed, newStepDirection);
		direction = normalize(add(scalarMult(5,direction),newStepDirection));

		// 2) branching based on concentration:
		if(super.cellElement.isANeuriteElement() && ( ecm.getRandomDouble()<bifurcationWeight*branchingFactor) ){
		//if(ecm.getRandomDouble()<branchingFactor){
			NeuriteElement newBranch = ((NeuriteElement) cellElement).branch();
			System.out.println("Cell " + cell.getID() + " is branching at " + newBranch.getLocation()[0] + ", " + newBranch.getLocation()[1] + ", " + newBranch.getLocation()[2] );
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

		/*
		 * This should be its own evaluate() function
		 *
		 * Evaluate a GRNGenome by creating and evaluating a Cell for a certain amount of time
		 * Takes GRNGenome
		 * Returns a Cell with morphology
		 */
    	GRNElement grnElement = new GRNElement("ActiveNeuriteType2");
		double maxTime = 2;
		long randomSeed = 17L;

        // Configure Cell
        ECM ecm = ECM.getInstance();

		ECM.setRandomSeed(randomSeed);
		Substance A = new Substance("A",Color.magenta);
		// Establish convention, A-P = Z, Long = Y, M-L = X
		ecm.addArtificialGaussianConcentrationZ(A, 1.0, 400.0, 160.0);

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
        grnModule.setGrn(grnElement);
		grnModule.setCell(c);
		c.getSomaElement().addLocalBiologyModule(grnModule);


		ActiveNeuriteChemoAttraction activeNeuriteModule = new ActiveNeuriteChemoAttraction(grnModule);
		neurite.addLocalBiologyModule(activeNeuriteModule);

        // Simulate
        Scheduler.simulate(maxTime);

	}
}
