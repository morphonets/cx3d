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

import static ini.cx3d.utilities.Matrix.normalize;
import static ini.cx3d.utilities.Matrix.randomNoise;

import java.awt.Rectangle;

import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.AbstractLocalBiologyModule;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;

/**
 * This class was used to produce Figure 3 G of the paper
 * "A framework for modeling the growth and development of neurons and networks", Zubler & Douglas 2009.
 * 
 * It implements the AbstractLocalBiologyModule, and secretes and moves up a chemical, for cell clustering
 * 
 * @author fredericzubler
 *
 */
public class Figure_3_G extends AbstractLocalBiologyModule {
	
	private String substanceID;
	
	public Figure_3_G(String substanceID) {
		this.substanceID = substanceID;
	}
	
	public AbstractLocalBiologyModule getCopy() {
		return new Figure_3_G(substanceID);
	}

	// this function is executed each time step
	public void run() {		
		PhysicalObject physical = super.cellElement.getPhysical();
		// move
		double speed = 100;
		double[] grad = physical .getExtracellularGradient(substanceID);
		physical.movePointMass(speed, normalize(grad));
		// secrete
		physical.modifyExtracellularQuantity(substanceID, 1000);
	}
	
	public static void main(String[] args) {
		ini.cx3d.utilities.SystemUtilities.tic();
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(1L);
		Substance yellowSubstance = new Substance("Yellow",1000, 0.01);
		Substance violetSubstance = new Substance("Violet",1000, 0.01);
		ecm.addNewSubstanceTemplate(yellowSubstance);
		ecm.addNewSubstanceTemplate(violetSubstance);
		for (int i = 0; i < 400; i++) {	
			ecm.getPhysicalNodeInstance(randomNoise(700,3));
		} 
		for(int i = 0; i<1000; i++){
			Cell c = CellFactory.getCellInstance(randomNoise(500, 3));
			c.getSomaElement().addLocalBiologyModule(new Figure_3_G("Yellow"));
			c.setColorForAllPhysicalObjects(Param.X_SOLID_YELLOW);
		}
		for(int i = 0; i<1000; i++){
			Cell c = CellFactory.getCellInstance(randomNoise(500, 3));
			c.getSomaElement().addLocalBiologyModule(new Figure_3_G("Violet"));
			c.setColorForAllPhysicalObjects(Param.X_SOLID_VIOLET);
		}
		Scheduler.setPrintCurrentECMTime(true);
		
		for (int i = 0; i < 10000; i++) {
			Scheduler.simulateOneStep();
			
		}
		
	}
}
