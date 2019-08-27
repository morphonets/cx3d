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

package sc.iview.cx3d.cells;

import sc.iview.cx3d.localBiology.AbstractLocalBiologyModule;
import sc.iview.cx3d.localBiology.CellElement;
import sc.iview.cx3d.localBiology.GRNElement;
import sc.iview.cx3d.physics.PhysicalSphere;

public class GRNModule extends AbstractLocalBiologyModule {

	/* the cell it belongs to.*/
	private Cell cell;
	/* turned on or off */
	private boolean enable = true;

    public GRNElement getGrn() {
        return grn;
    }

    public void setGrn(GRNElement grn) {
        this.grn = grn;
    }

    /* the GRN itself */
    private GRNElement grn;
	
	/* the speed of metabolic update.*/
	private double dVdt = 150.0;
	/* the minimum size to obtain before being allowed to divide.*/
	private double minimumDiameter = 20.0;
	
	
	public Cell getCell() {
		return cell;
	}

	public boolean isEnabled() {
		return enable;
	}

	// the cell cycle model lies in this method:
	public void run() {
		if(!enable)
			return;
		PhysicalSphere ps = cell.getSomaElement().getPhysicalSphere();
		getGrn().state.evolve(1);
		//System.out.println( "GRNModule update: " + getGrn().state.toString() );

//		// is diameter smaller than min
//		if(ps.getDiameter() < minimumDiameter){
//			ps.changeVolume(dVdt);
//		}else{
//			// otherwise divide
//			cell.divide();
//		}
	}

    @Override
    public CellElement getCellElement() {
        return null;
    }

    @Override
    public void setCellElement(CellElement cellElement) {

    }

    @Override
    public AbstractLocalBiologyModule getCopy() {
        GRNModule cc = new GRNModule();
        cc.enable = this.enable;
        cc.setGrn(this.getGrn());// TODO: copy the GRN?
        return cc;
    }

    public void setCell(Cell cell) {
		this.cell = cell;
	}

	public void reset(){
	    grn.state.reset();
		//cell.divide();
	}
	
	public void setEnabled(boolean enabled) {
		this.enable = enabled;
	}

    @Override
    public boolean isCopiedWhenNeuriteBranches() {
        return false;
    }

    @Override
    public boolean isCopiedWhenSomaDivides() {
        return false;
    }

    @Override
    public boolean isCopiedWhenNeuriteElongates() {
        return false;
    }

    @Override
    public boolean isCopiedWhenNeuriteExtendsFromSoma() {
        return false;
    }

    @Override
    public boolean isDeletedAfterNeuriteHasBifurcated() {
        return false;
    }

    public boolean isCopiedWhenCellDivides() {
		return true;
	}
}
