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

package sc.iview.cx3d.simulations.frontiers;

import static sc.iview.cx3d.utilities.Matrix.randomNoise;
import static sc.iview.cx3d.utilities.Matrix.add;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.AbstractLocalBiologyModule;
import sc.iview.cx3d.localBiology.CellElement;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.localBiology.SomaElement;
import sc.iview.cx3d.physics.IntracellularSubstance;
import sc.iview.cx3d.physics.PhysicalObject;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.utilities.Matrix;
import sc.iview.cx3d.utilities.SystemUtilities;


public class Simplified_Figure_11{


	public static void main(String[] args) {

		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(1L);
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(500,3));
		}
		
		// defining the templates for the intracellular substance
		double D = 1000; // diffusion cst
		double d = 0.01;	// degradation cst
		IntracellularSubstance tubulin = new IntracellularSubstance("tubulin",D,d);
		tubulin.setVolumeDependant(false);
		ecm.addNewIntracellularSubstanceTemplate(tubulin);
		// getting a cell
		Cell c = CellFactory.getCellInstance(new double[] {0,0,0});
		c.setColorForAllPhysicalObjects(Param.RED);
		// insert production module
		SomaElement soma = c.getSomaElement();
		soma.addLocalBiologyModule(new InternalSecretor());
		//insert growth cone module
		NeuriteElement ne = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
		ne.getPhysical().setDiameter(1.0);
		ne.addLocalBiologyModule(new GrowthCone());
		
		// run, Forrest, run..
		SystemUtilities.tic();
		for (int i = 0; i < 10000; i++) {
			Scheduler.simulateOneStep();
		}
		SystemUtilities.tac();
		
		
	}

	private static class InternalSecretor extends AbstractLocalBiologyModule {

		// secretion rate (quantity/time)
		private double secretionRate = 60;  
		
		// needed for copy in the cell in case of division
		public AbstractLocalBiologyModule getCopy() {
			return new InternalSecretor();
		}
		
		// method called at each time step: secretes tubulin in the extracellular space 
		public void run() {
			super.cellElement.getPhysical().modifyIntracellularQuantity(
					"tubulin", secretionRate);
		}
	}
	
	public static class GrowthCone extends AbstractLocalBiologyModule{
		
		// some parameters 
		private static double speedFactor = 5000;	
		private static double consumptionFactor = 100;
		private static double bifurcationProba = 0.003;
		// direction at previous time step:
		private double[] previousDir;
		// initial direction is parallel to the cylinder axis
		// therefore we overwrite this method from the superclass:
		public void setCellElement(CellElement cellElement){
			super.cellElement = cellElement;
			this.previousDir = cellElement.getPhysical().getAxis();
		}
		// to ensure distribution in all terminal segments:
		public AbstractLocalBiologyModule getCopy() {return new GrowthCone();}

		public boolean isCopiedWhenNeuriteBranches() {return true;}
		
		public boolean isDeletedAfterNeuriteHasBifurcated() {return true;}
		
		// growth cone model
		public void run() {
			// getting the concentration and defining the speed
			PhysicalObject cyl = super.cellElement.getPhysical();
			double concentration = cyl.getIntracellularConcentration("tubulin");
			double speed = concentration*speedFactor;
			if(speed>100)  // can't be faster than 100
				speed = 100;
			// movement and consumption
			double[] direction = Matrix.add(previousDir, randomNoise(0.1,3));
			previousDir = Matrix.normalize(direction);
			cyl.movePointMass(speed, direction);
			cyl.modifyIntracellularQuantity("tubulin", -concentration*consumptionFactor);
			// test for bifurcation
			if(ECM.getRandomDouble()<bifurcationProba)
				((NeuriteElement)(super.cellElement)).bifurcate();
		}
	}
}
