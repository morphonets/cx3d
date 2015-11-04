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

import ini.cx3d.localBiology.AbstractLocalBiologyModule;



/**
 * This class is used in Figure 8 : 
 * it defines a local module for extracellular secretion of substance
 * @author fredericzubler
 *
 */
public class X_Secretion_Module extends AbstractLocalBiologyModule{
	
    private String substanceId = null;
    private double desiredConcentration = 1;
    private double maximumProductionRate = 10000;
    private boolean isSigmoid = true;
    
    public X_Secretion_Module(String substanceId, double desiredConcentration) {
		this.substanceId = substanceId;
		this.desiredConcentration = desiredConcentration;
	}

	public X_Secretion_Module() {
    }
	
	public X_Secretion_Module getCopy() {
		X_Secretion_Module ss = new X_Secretion_Module();
		ss.substanceId = this.substanceId;
		ss.desiredConcentration = this.desiredConcentration;
		ss.maximumProductionRate = this.maximumProductionRate;
		ss.isSigmoid = this.isSigmoid;
		ss.cellElement = this.cellElement;
		return ss;
	}
    
    public void run() {
    	// 1. get the concentration where we are
    	double concentration = cellElement.getPhysical().getExtracellularConcentration(substanceId);
    	// 2. determinate how much we should secrete (or degradate if the concentration is too high):
    	// 		a) difference between desired an actual value
    	double diff = (concentration-desiredConcentration);
    	//		b) sigmoid function (5 is the slope of the sigmoide, chosen s.t when the |diff| = 1,
    	//	       the rate is already max)
    	double quantityChangePerTime = 1.0 / ( 1.0 + Math.exp(5*diff) );
    	// 		c)	scale by to get the maximum value
    	quantityChangePerTime *= maximumProductionRate;
    	// 3. secrete 
    	cellElement.getPhysical().modifyExtracellularQuantity(substanceId, quantityChangePerTime);

    }

    // overwriting methods of SubElement .........................................
    
    public boolean isASecretor(){
		return true;
	}
	
	public boolean isAnExternalSecretor(){
		return true;
	}
    
	/** only compares the substance id, and not the other variables......*/
	public boolean isSimilar(X_Secretion_Module ss){
		if(substanceId.equals(ss.getSubstance()))
			return true;
		return false;
	}
	
	// getters and setters .......................................................
	
	public double getDesiredConcentration() {
		return desiredConcentration;
	}

	public void setDesiredConcentration(double desiredConcentration) {
		this.desiredConcentration = desiredConcentration;
	}

	public void setSubstanceId(String substanceId){
		this.substanceId = substanceId;
	}
	
	public String getSubstance() {
		return substanceId;
	}

	public boolean isCopiedWhenNeuriteElongates() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCopiedWhenNeuriteBranches() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCopiedWhenNeuriteExtendsFromSoma() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCopiedWhenSomaDivides() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeletedAfterNeuriteHasBifurcated() {
		// TODO Auto-generated method stub
		return false;
	}

	

}
