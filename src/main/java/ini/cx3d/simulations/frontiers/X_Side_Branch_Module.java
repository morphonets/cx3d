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

import ini.cx3d.localBiology.CellElement;
import ini.cx3d.localBiology.LocalBiologyModule;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.simulations.ECM;

import java.util.Vector;

/**
 * This class is used in Figure 8 : 
 * it defines a local biology module for making side branches
 * @author fredericzubler
 *
 */
public class X_Side_Branch_Module implements LocalBiologyModule{

	/* The CellElement this module lives in.*/
	CellElement cellElement;
	/* Whether copied or not in branching.*/
	boolean copiedWhenNeuriteBranches = false;

	/* The chemical that activate branching.*/
	Vector<String> branchingFactors = new Vector<String>();

	/* Slope of the probability to branch*/
	public double a;
	/* shift in the probability */
	public double b;

	/* minimum interval before branching */
	double freeInterval = 15+10*ECM.getRandomDouble();

	/* maximal mumber of side branches.*/
	int maxNumberOfSideBranches = 2+((int)Math.round(2*ECM.getRandomDouble()));
	
	/* Modules that are inserted in the new side branch*/
	Vector<LocalBiologyModule> ModulesInSideBranch = new Vector<LocalBiologyModule>();

	public X_Side_Branch_Module() {
		a = 0.03;
		b = -0.01;
	}

	public X_Side_Branch_Module(double a, double b) {
		this.a = a;
		this.b = b;
	}

	// --- LocalBiologyModule interface ----------------------------
	public CellElement getCellElement() {
		return cellElement;
	}

	public boolean isCopiedWhenNeuriteElongates() {
		return false;
	}

	public boolean isCopiedWhenNeuriteBranches() {
		return copiedWhenNeuriteBranches;
	}

	public boolean isDeletedAfterNeuriteHasBifurcated() {
		return false;
	}
	
	/**
	 * Specifies if this receptor is copied.
	 * @param copiedWhenNeuriteBranches
	 */
	// This method is not part of the LocalBiologyModule
	public void setCopiedWhenNeuriteBranches(final boolean copiedWhenNeuriteBranches) {
		this.copiedWhenNeuriteBranches = copiedWhenNeuriteBranches;
	}

	final public boolean isCopiedWhenNeuriteExtendsFromSoma() {
		return false;
	}

	public boolean isCopiedWhenSomaDivides() {
		return false;
	}

	public void setCellElement(CellElement cellElement) {
		this.cellElement = cellElement;
	}

	public X_Side_Branch_Module getCopy(){
		X_Side_Branch_Module bf = new X_Side_Branch_Module(a, b);
		bf.branchingFactors.addAll(this.branchingFactors);
		bf.copiedWhenNeuriteBranches = this.copiedWhenNeuriteBranches;
		bf.maxNumberOfSideBranches = this.maxNumberOfSideBranches;
		// submodules
		if(!ModulesInSideBranch.isEmpty()){
			for (int i = 0; i < ModulesInSideBranch.size(); i++) {
				bf.ModulesInSideBranch.add(ModulesInSideBranch.get(i).getCopy());
			}
		}
		return bf; 
	}

	/** Add a chemical that will make this receptor branch.*/
	public void addBranchingFactor(String bf){
		branchingFactors.add(bf);
	}


	// --- LocalBiologyModule interface ----------------------------

	public void run() {
		PhysicalCylinder cyl = (PhysicalCylinder) cellElement.getPhysical();
		if(cyl.lengthToProximalBranchingPoint()<freeInterval)
			return;
		// only terminal cylinders branch
		if(cyl.getDaughterLeft() != null)
			return;

		double totalConcentration = 0.0;
		for (String s : branchingFactors) {
			double concentr = cyl.getExtracellularConcentration(s);
			totalConcentration += concentr;
		}

		double y = a*totalConcentration + b;
		if(ECM.getRandomDouble()<y){
			maxNumberOfSideBranches -= 1;
			
			double phi = 2*Math.PI*ECM.getRandomDouble();
			double[] growthDirection = {Math.cos(phi), Math.sin(phi), 1 };
			
			NeuriteElement sideNeurite = ((NeuriteElement)cellElement).branch(cyl.getDiameter(), growthDirection);
			
			if(!ModulesInSideBranch.isEmpty()){
				for (int i = 0; i < ModulesInSideBranch.size(); i++) {
					sideNeurite.addLocalBiologyModule(ModulesInSideBranch.get(i).getCopy());
					System.out.println("SideBranchReceptor.run() "+ ModulesInSideBranch.get(i));
				}
			}
			if(maxNumberOfSideBranches<1){
				cellElement.removeLocalBiologyModule(this);
			}
		}
	}

	public void addModuleToSideBranch(LocalBiologyModule m){
		ModulesInSideBranch.add(m);
	}
	
	
	public static void main(String[] args) {
		for (int i = 0; i < 20; i++) {
			int a = ((int)Math.round(ECM.getRandomDouble())) ;
			System.out.println(a);
		}
		
	}
}
