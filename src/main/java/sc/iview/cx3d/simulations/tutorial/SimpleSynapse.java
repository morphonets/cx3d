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

import static sc.iview.cx3d.utilities.Matrix.randomNoise;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.PhysicalCylinder;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.synapses.BiologicalBouton;
import sc.iview.cx3d.synapses.BiologicalSpine;
import sc.iview.cx3d.synapses.PhysicalBouton;
import sc.iview.cx3d.synapses.PhysicalSpine;

public class SimpleSynapse {
	
	
	public static void main(String[] args) {
		
		ECM ecm = ECM.getInstance();	
		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}
		
		double[] up = {0.0,0.0,1.0}, down = {0.0,0.0,-1.0};
		// 1) two cells : and excitatory (down) and an inhibitory one (up)
		Cell excit = CellFactory.getCellInstance(new double[] {-2.5,0,-30});
		excit.setNeuroMLType(Cell.ExcitatoryCell);
		excit.setColorForAllPhysicalObjects(Param.GREEN);
		Cell inhib = CellFactory.getCellInstance(new double[] {2.5,0,30});
		inhib.setNeuroMLType(Cell.InhibitoryCell);
		inhib.setColorForAllPhysicalObjects(Param.RED);
		// 2) excitatory cell makes an axon, inhibitory cell makes a dendrite
		NeuriteElement axon = excit.getSomaElement().extendNewNeurite(up);
		axon.setIsAnAxon(true);
		PhysicalCylinder axonCyl = axon.getPhysicalCylinder();
		NeuriteElement dendrite = inhib.getSomaElement().extendNewNeurite(down);
		dendrite.setIsAnAxon(false);
		PhysicalCylinder dendriteCyl = dendrite.getPhysicalCylinder();
		//		elongate them
		while (axon.getLocation()[2]<dendrite.getLocation()[2]) {
			axon.elongateTerminalEnd(1/Param.SIMULATION_TIME_STEP, up);
			dendrite.elongateTerminalEnd(1/Param.SIMULATION_TIME_STEP, down);
			Scheduler.simulateOneStep();
		}
		// 3) a bouton on the axon:
		// 		create the physical part
		double[] globalCoord = new double[] {axon.getLocation()[2] + dendrite.getLocation()[2],0,0};
		double[] polarAxonCoord = axonCyl.transformCoordinatesGlobalToPolar(globalCoord);
		polarAxonCoord = new double[] {polarAxonCoord[0], polarAxonCoord[1]}; // so r is implicit 
		
		PhysicalBouton pBouton = new PhysicalBouton(axonCyl,polarAxonCoord,3);
		axonCyl.addExcrescence(pBouton);
		// 		create the biological part and set call backs
		BiologicalBouton bBouton = new BiologicalBouton();
		pBouton.setBiologicalBouton(bBouton);
		bBouton.setPhysicalBouton(pBouton);

		// 4) a spine on the dendrite:
		// 		create the physical part
		double[] polarDendriteCoord = dendriteCyl.transformCoordinatesGlobalToPolar(globalCoord);
		polarDendriteCoord = new double[] {polarDendriteCoord[0], polarDendriteCoord[1]}; // so r is implicit 
		
		PhysicalSpine pSpine = new PhysicalSpine(dendriteCyl,polarDendriteCoord,3);
		dendriteCyl.addExcrescence(pSpine);
		// 		create the biological part and set call backs
		BiologicalSpine bSpine = new BiologicalSpine();
		pSpine.setBiologicalSpine(bSpine);
		bSpine.setPhysicalSpine(pSpine);
		
		// 5) synapse formation
		pBouton.synapseWith(pSpine, true);
	
	}
}
