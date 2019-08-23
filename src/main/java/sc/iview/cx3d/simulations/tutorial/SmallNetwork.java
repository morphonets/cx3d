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

import java.awt.Color;

import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.synapses.ConnectionsMaker;

import static sc.iview.cx3d.utilities.Matrix.*;

public class SmallNetwork {
	
	public static void main(String[] args) {
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(0L);		
		Substance L1 = new Substance("L1",Color.red);
		ecm.addArtificialGaussianConcentrationZ(L1, 1.0, 400.0, 60.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		for (int i = 0; i < 8; i++) {
			Cell c;
			if(i<4){
				c= CellFactory.getCellInstance(new double[] {-20+40*ECM.getRandomDouble(),-20+40*ECM.getRandomDouble(),0.0});
				c.setNeuroMLType(Cell.ExcitatoryCell);
				c.setColorForAllPhysicalObjects(Param.VIOLET);
			}else{
				c= CellFactory.getCellInstance(new double[] {-20+40*ECM.getRandomDouble(),-20+40*ECM.getRandomDouble(),200.0});
				c.setNeuroMLType(Cell.InhibitoryCell);
				c.setColorForAllPhysicalObjects(Param.VIOLET.darker());	
			}
			NeuriteElement axon = c.getSomaElement().extendNewNeurite();
			axon.setIsAnAxon(true);
			axon.getPhysicalCylinder().setDiameter(0.5);
			axon.addLocalBiologyModule(new NeuriteChemoAttraction("L1",0.02));

			if(i<4){
				axon.getPhysicalCylinder().setColor(Param.YELLOW);
			}else{
				axon.getPhysicalCylinder().setColor(Param.YELLOW.darker());
			}


			NeuriteElement dendrite = c.getSomaElement().extendNewNeurite();
			dendrite.setIsAnAxon(false);
			dendrite.getPhysicalCylinder().setDiameter(1.5);
			dendrite.addLocalBiologyModule(new NeuriteChemoAttraction("L1",0.02));
		}
		while (ecm.getECMtime() < 6) {
			Scheduler.simulateOneStep();
		}	
		ConnectionsMaker.extendExcressencesAndSynapseOnEveryNeuriteElement();
	}

	


}