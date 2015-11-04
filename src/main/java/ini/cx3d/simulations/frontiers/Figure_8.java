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
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.localBiology.SomaElement;
import ini.cx3d.physics.PhysicalSphere;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;


import java.awt.Color;

/**
 * This class was used to produce Figure 8 of the paper
 * "A framework for modeling the growth and development of neurons and networks", Zubler & Douglas 2009.
 * 
 * It starts with a three layered cortex, and grows the dendritic and axonal arbors of two cells.
 * It need the following classes : X_Bifurcation_Module, X_Movement_Module, X_Secretion_Module, X_Side_Branch_Module
 * 
 * @author fredericzubler
 *
 */
public class Figure_8 {

	
	// color of the elements
	private static 	Color col_6 = new Color(176,179,172);
	private static 	Color col_5 = new Color(136,138,133);
	private static 	Color col_4 = new Color(221,225,217);
	private static 	Color col_AXON = Color.black;
	private static 	Color col_DENDRITE = Param.X_SOLID_RED;
	
	
	public static void main(String[] args) {
		// 1) Prepare the environment :
		// 		get a reference to the extracellular matrix (ECM)
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(6L);
		
		// 		add additional PhysicalNodes (for diffusion)
		int nbOfAdditionalNodes = 100;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		
		//	Putting the cortical layers
		threeLayerOfSecretingCells();
		
		// Running the physics some time, to avoid overlap during simulation
		for (int i = 0; i < 300; i++) {
			for (PhysicalSphere pss : ECM.getInstance().physicalSphereList) {
				pss.runPhysics();
			}
		}
		
		// Simulate a while (secretion of substances), to get the right concentrations...
		for (int i = 0; i < 200; i++) {
			Scheduler.simulateOneStep();
		}

		// 2) Create cells =================================================================
		for (int i = 0; i < 2; i++) {


			double[] cellLocation = new double[] {-30+60*i,6*i,40};
			Cell cell = CellFactory.getCellInstance(cellLocation);
			cell.setColorForAllPhysicalObjects(col_AXON);

			// AXON-------------------------------------------------------------------------
			// 3) Extend an axon from the cell
			NeuriteElement axon = cell.getSomaElement().extendNewNeurite(new double[] {0,0,-1});
			axon.getPhysical().setDiameter(1.0);
			axon.setIsAnAxon(true);
			axon.getPhysical().setColor(col_AXON);

			// 4) in axon : movement away a + side branch B
			X_Movement_Module m1 = new X_Movement_Module();
			m1.addRepellent("5");
			m1.setRandomness(0.1);
			m1.setCopiedWhenNeuriteBranches(false);
			axon.addLocalBiologyModule(m1);
			X_Side_Branch_Module s1 = new X_Side_Branch_Module(1,-0.1);
			s1.addBranchingFactor("6");
			axon.addLocalBiologyModule(s1);

			// movement
			X_Movement_Module m2 = new X_Movement_Module();
			m2.addAttractant("4");
			m2.setCopiedWhenNeuriteBranches(true);
			m2.setMinimalBranchDiameter(0.55);
			m2.setLinearDiameterDecrease(0);
			m2.setRandomness(0);
			axon.addLocalBiologyModule(m2);
			
			// branching
			X_Bifurcation_Module b = new X_Bifurcation_Module(0.3,0);
			b.addBranchingFactor("4");
			b.setDiameterOfDaughter(.84); //0.891
			b.setCopiedWhenNeuriteBranches(true);
			b.minConcentration = 0.3;
			b.setMinimalBranchDiameter(0.55);
			axon.addLocalBiologyModule(b);
			


			// DENDRITE ----------------------------------------------------------------------
			// 5) Extend an apical dendrite from the cell

			NeuriteElement apicalDendrite = cell.getSomaElement().extendNewNeurite(new double[] {0,.1,1});
			apicalDendrite.getPhysical().setDiameter(1.0);
			apicalDendrite.setIsAnAxon(false);
			apicalDendrite.getPhysical().setColor(col_DENDRITE);
			apicalDendrite.getPhysicalCylinder().setDiameter(3.0);

			// 6) movement and bifurcation for the apical dendrite

			// movement
			m1 = new X_Movement_Module();
			m1.addAttractant("4");
			m1.setCopiedWhenNeuriteBranches(true);
			m1.setMinimalBranchDiameter(2);
			m1.setLinearDiameterDecrease(0.001);
			m1.setRandomness(0);
			apicalDendrite.addLocalBiologyModule(m1);
			
			// branching
			b = new X_Bifurcation_Module(0.1,0);
			b.setDiameterOfDaughter(1.0);
			b.addBranchingFactor("4");
			b.setCopiedWhenNeuriteBranches(true);
			b.minConcentration = 0.0;
			b.maxProba = 0.2; 
			b.setMinimalBranchDiameter(2);
			apicalDendrite.addLocalBiologyModule(b);

		}


		Scheduler.setPrintCurrentECMTime(true);
		for (int i = 0; i < 25000; i++) {
			Scheduler.simulateOneStep();
		}
	}

	
	
	
	
	
	
	
	
	
	public static void threeLayerOfSecretingCells(double L6_thickness,
			int L6_number,
			double L5_thickness,
			int L5_number,
			double L4_thickness,
			int L4_number,
			int numberOfAdditionalNodesBelowL6){

		double offset = 0;
		// 2) Getting an ECM and registering signaling substances
		ECM ecm = ECM.getInstance(); 
		ecm.addNewSubstanceTemplate(new Substance("6", Param.RED));
		ecm.addNewSubstanceTemplate(new Substance("5", Param.YELLOW));
		ecm.addNewSubstanceTemplate(new Substance("4", Param.GREEN));
				
		// 3) a three-layer cortex
		int total = L6_number + L5_number + L4_number;
		for (int i = 0; i < total; i++) {
			double[] cellOrigin; 
			Color color;
			X_Secretion_Module eS = new X_Secretion_Module();
			// choosing the characteristics for 3 different types
			if(i<L6_number){
				// a) Layer 6 cells secreting the chemical "6"
				cellOrigin = new double[] {-100+200*ECM.getRandomDouble(), -100+200*ECM.getRandomDouble(), offset+L6_thickness*ECM.getRandomDouble()};
				eS.setSubstanceId("6");
				color = Param.RED;
				color = col_6;
				
			}else if(i<L5_number+L6_number){
				// b) Layer 5 cells secreting the chemical "5"
				cellOrigin = new double[] {-100+200*ECM.getRandomDouble(), -100+200*ECM.getRandomDouble(), 
						offset+L6_thickness+L5_thickness*ECM.getRandomDouble()};
				eS.setSubstanceId("5");
				color = Param.YELLOW;
				color = col_5;
			}else{
				// c) Layer 4 cells secreting the chemical "4"
				cellOrigin = new double[] {-100+200*ECM.getRandomDouble(), -100+200*ECM.getRandomDouble(), 
						offset+L6_thickness+L5_thickness+L4_thickness*ECM.getRandomDouble()};
				eS.setSubstanceId("4");
				color = Param.GREEN;
				color = col_4;
			}
			// creating the layer specific cell
			Cell cell = CellFactory.getCellInstance(cellOrigin);
			SomaElement soma = cell.getSomaElement();
			soma.addLocalBiologyModule(eS);
			PhysicalSphere ps = soma.getPhysicalSphere();
			ps.setColor(color);
		}

		// additional nodes below L4
		for (int i = 0; i < numberOfAdditionalNodesBelowL6; i++) {
			double[] coord =  new double[] {-100+200*ECM.getRandomDouble(), -100+200*ECM.getRandomDouble(), offset-500*ECM.getRandomDouble()};
			ecm.getPhysicalNodeInstance(coord);
		}
	}


	public static void threeLayerOfSecretingCells(){

		// 1) the thickness of the layers :
		double L6_thickness = 200;
		int L6_number = 600;
		double L5_thickness = 200;
		int L5_number = 600;
		double L4_thickness = 200;
		int L4_number = 600;
		int numberOfAdditionalNodesBelowL6 = 100;
		

		threeLayerOfSecretingCells(L6_thickness, L6_number, L5_thickness, L5_number, L4_thickness, L4_number, numberOfAdditionalNodesBelowL6);
		
		
	}

	public static void artificialSubstances(){
		ECM ecm = ECM.getInstance();
		// 		add additional PhysicalNodes (for diffusion)
		int nbOfAdditionalNodes = 100;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		// 2) Create some artificial chemical gradients
		// 		horizontal gaussian (peak concentration, peak coordinate, variance)
		ecm.addArtificialGaussianConcentrationZ(new Substance("reelin", Color.red), 1, 500, 200); 
		ecm.addArtificialGaussianConcentrationZ(new Substance("A", Color.red), 1, 400, 200); 
		ecm.addArtificialGaussianConcentrationZ(new Substance("B", Color.blue), 1, -100, 200);
		ecm.addArtificialGaussianConcentrationZ(new Substance("C", Color.green), 1, -300, 100);
		//		horizontal linerar gradient
		ecm.addArtificialLinearConcentrationZ(new Substance("D", Color.cyan), 1, 300, -300);
		//		vertical gaussian
		ecm.addArtificialGaussianConcentrationX(new Substance("E", Color.red), 1, 0, 100);
		ecm.addArtificialGaussianConcentrationX(new Substance("F", Color.green), 1, 300, 100);
	}
}
