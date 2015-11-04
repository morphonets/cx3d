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

package ini.cx3d.simulations.frontiers;

import static ini.cx3d.utilities.Matrix.randomNoise;
import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.AbstractLocalBiologyModule;
import ini.cx3d.localBiology.SomaElement;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalNode;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;

import java.awt.Color;
import java.awt.Rectangle;


/**
 * This class was used to produce Figure 10 of the paper
 * "A framework for modeling the growth and development of neurons and networks", Zubler & Douglas 2009.
 * 
 * Pattern formation by lateral inhibition, with cell membrane markers notch and delta.
 * Based on Sabina Pfister's MATLAB simulation of Collier et al, J. theor. Biol 1996
 * 
 * @author fredericzubler
 *
 */
public class Figure_10 extends AbstractLocalBiologyModule{

	/* Notch */
	private IntracellularSubstance substanceN;
	/* Delta */
	private IntracellularSubstance substanceD;

	/* If true, color-codes the cell Body */
	public boolean changingTheColorOfThePhysicalObject = true;

	/* If true, Delta cells divide */
	public boolean dividing = false;  // <---- you can change this !!

	/** Constructor specifying the two substances */
	public Figure_10(IntracellularSubstance substanceN, IntracellularSubstance substanceD) {
		super();
		this.substanceN = substanceN;
		this.substanceD = substanceD;
	}

	public boolean isCopiedWhenSomaDivides() {return true;}

	public boolean isDeletedAfterNeuriteHasBifurcated() {return false;}

	public Figure_10 getCopy(){
		return new Figure_10(substanceN,substanceD);
	}

	/** Overwrites the run of the superclass; contains the simulation procedure**/
	public void run(){
		PhysicalObject po = cellElement.getPhysical();

		// 1) 	Look for the concentration of delta around:
		// 		(I sum the Substances on all the neighbors I'm in contact with, and then make an average)
		double suroundingConcentrationD = 0.0;
		int nbOfNeighboringSomata = 0;
		for (PhysicalNode pn : po.getSoNode().getNeighbors()) {
			if(pn instanceof PhysicalSphere && po.isInContact(((PhysicalSphere)pn)) ){			
				suroundingConcentrationD += ((PhysicalSphere)pn).getMembraneConcentration(substanceD.getId());
				nbOfNeighboringSomata += 1;
			}
		}
		double averageD = 0;
		if(nbOfNeighboringSomata != 0){
			averageD = suroundingConcentrationD / nbOfNeighboringSomata;
		}

		// 2) 	Look for the concentration in my cellElement:
		double D = po.getMembraneConcentration(substanceD.getId());
		double N = po.getMembraneConcentration(substanceN.getId());

		// 3) 	How do I modify my own expression:
		double dN_dt = f(averageD) - N;
		double dD_dt = (g(N) - D);
		po.modifyMembraneQuantity(substanceN.getId(), dN_dt*50);
		po.modifyMembraneQuantity(substanceD.getId(), dD_dt*50);
		
		if(N<0) N=0;
		if(N>1.0) N=1.0;
		if(D<0) D=0;
		if(D>1.0) D=1.0;
		
		
		// 4)	Change my physical objects color
		if(changingTheColorOfThePhysicalObject){
			cellElement.getPhysical().setColor(new Color((float)N, (float)D, 0f));
		}

		// 5) Division of cells with high delta and small notch
		if(dividing){
			PhysicalSphere sphere = (PhysicalSphere) cellElement.getPhysical();
			if(sphere.getDiameter()<20){
				sphere.changeDiameter(7);
			}else{
				if(D > 0.9 && N <0.2){
					cellElement.getCell().divide();
				}
			}
		}
		
	}

	/* Used for change in Notch activity. */
	private double f(double x){
		return Math.min(1.0, 20*x);
	}

	/* Used for change in Delta activity. */
	private double g(double x){
		return Math.max(0.0, 1.0-x);
	}

	// ========= Some getters and setters =====================================================

	public IntracellularSubstance getIntracellularSubstanceD() {
		return substanceD;
	}

	public void setIntracellularSubstanceD(IntracellularSubstance substanceD) {
		this.substanceD = substanceD;
	}

	public IntracellularSubstance getIntracellularSubstanceN() {
		return substanceN;
	}

	public void setIntracellularSubstanceN(IntracellularSubstance substanceN) {
		this.substanceN = substanceN;
	}

	public boolean isChangingTheColorOfThePhysicalObject() {
		return changingTheColorOfThePhysicalObject;
	}

	public void setChangingTheColorOfThePhysicalObject(
			boolean changingTheColorOfThePhysicalObject) {
		this.changingTheColorOfThePhysicalObject = changingTheColorOfThePhysicalObject;
	}


	// ========= The Main Method =====================================================


	public static void main(String[] args) {
		// 1) Prepare the environment :
		// 		a reference to ECM, the extra-cellular-matrix
		ECM ecm = ECM.getInstance();

		// 2) Define two different templates for intracellular Substances:
		// 		(notch)
		IntracellularSubstance substanceN = new IntracellularSubstance("N",0,0);
		substanceN.setVisibleFromOutside(true);
		// 		(delta)
		IntracellularSubstance substanceD = new IntracellularSubstance("D",0,0);
		substanceD.setVisibleFromOutside(true);

		// 3) Putting a bunch of cells, with random initial concentrations, and a DeltaNotch module
		for (int i = 0; i < 200; i++) {
			// a) the Cell
			double[] cellOrigin = randomNoise(55,3);  
			Cell cell = CellFactory.getCellInstance(cellOrigin);
			SomaElement soma = cell.getSomaElement();
			// b) with the initial concentrations
			IntracellularSubstance subN = new IntracellularSubstance(substanceN);
			IntracellularSubstance subD = new IntracellularSubstance(substanceD);
			subN.setConcentration(0.9+0.1*ECM.getRandomDouble());
			subN.updateQuantityBasedOnConcentration(1.0);
			subD.setConcentration(0.9+0.1*ECM.getRandomDouble());
			subD.updateQuantityBasedOnConcentration(1.0);
			soma.getPhysicalSphere().getIntracellularSubstances().put(subN.getId(),subN);
			soma.getPhysicalSphere().getIntracellularSubstances().put(subD.getId(),subD);
			// c) set the Delta-Notch mechanism
			Figure_10 dn = new Figure_10(subN, subD);
			soma.addLocalBiologyModule(dn);
			// d) color the cells with a concentration code 
			soma.getPhysical().setColor(
					new Color((float)subN.getConcentration(), (float)subD.getConcentration(), 0f)
			);
		}


		int nbOfPreTimeSteps = 0;  // change this value, if you want to run physics to separate spheres
		for (int i = 0; i < nbOfPreTimeSteps; i++) { 
			for (PhysicalSphere pss : ecm.physicalSphereList) {
				pss.runPhysics();
			}
		}
		
		// Run the simulation:
		Scheduler.setPrintCurrentECMTime(true);
		for (int i = 0; i < 25000; i++) {
			Scheduler.simulateOneStep();
		}

	}

}
