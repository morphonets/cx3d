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

import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.CellElement;
import sc.iview.cx3d.localBiology.LocalBiologyModule;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;

import static sc.iview.cx3d.utilities.Matrix.add;
import static sc.iview.cx3d.utilities.Matrix.normalize;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;


public class RandomBranchingModule implements LocalBiologyModule {

	NeuriteElement neuriteElement;
	
	private double[] direction;
	
	public CellElement getCellElement() {
		return neuriteElement;
	}

	public void setCellElement(CellElement cellElement) {
		if(cellElement.isANeuriteElement()){
			neuriteElement = (NeuriteElement)cellElement;
			direction = neuriteElement.getPhysicalCylinder().getAxis();
		}else{
			cellElement.removeLocalBiologyModule(this);
			System.out.println("Sorry, I only work with neurite elements");
		}
	}
	
	public LocalBiologyModule getCopy() {
		RandomBranchingModule m = new RandomBranchingModule();
		return m;
	}

	public boolean isCopiedWhenNeuriteBranches() {
		return true;
	}

	public boolean isCopiedWhenNeuriteElongates() {
		return false;	// only in growth cones
	}

	public boolean isCopiedWhenNeuriteExtendsFromSoma() {
		return false;	// this method should never be called
	}

	public boolean isCopiedWhenSomaDivides() {
		return false;  	// this method should never be called
	}
	
	public boolean isDeletedAfterNeuriteHasBifurcated() {
		return true;	// Important because of bifurcations!
	}

	public void run() {
		double speed = 100;
		double probabilityToBifurcate = 0.005; // o.oo5
		double probabilityToBranch = 0.005;
		double[] deltaDirection = randomNoise(0.1, 3);
		direction = add(direction, deltaDirection);
		direction = normalize(direction);
		neuriteElement.getPhysical().movePointMass(speed, direction);
		
		if(ECM.getRandomDouble()<probabilityToBifurcate){
			NeuriteElement[] nn = neuriteElement.bifurcate();
			nn[0].getPhysical().setColor(Param.RED);
			nn[1].getPhysical().setColor(Param.BLUE);
			return;
		}
		if(ECM.getRandomDouble()<probabilityToBranch){
			NeuriteElement n = neuriteElement.branch();
			n.getPhysical().setColor(Param.VIOLET);
			return;
		}

	}

	public static void main(String[] args) {
		ECM ecm = ECM.getInstance();
		for (int i = 0; i < 18; i++) {	
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		} 
		ECM.setRandomSeed(7L);
		for(int i = 0; i<1; i++){
			Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
			c.setColorForAllPhysicalObjects(Param.GRAY);
			NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
			neurite.getPhysicalCylinder().setDiameter(2);
			neurite.addLocalBiologyModule(new RandomBranchingModule());
		}
		Scheduler.simulate();
	}

}
