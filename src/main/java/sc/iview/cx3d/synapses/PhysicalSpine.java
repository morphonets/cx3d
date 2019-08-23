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

package sc.iview.cx3d.synapses;

import sc.iview.cx3d.*;
import sc.iview.cx3d.localBiology.*;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.PhysicalBond;
import sc.iview.cx3d.physics.PhysicalObject;
import sc.iview.cx3d.utilities.Matrix;

public class PhysicalSpine extends Excrescence{

	BiologicalSpine biologicalSpine;
	
	public PhysicalSpine() {
		super();
		super.type = SPINE;
	}
	
	public PhysicalSpine(PhysicalObject po, double[] origin, double length) {
		super();
		super.type = SPINE;
		super.po = po;
		super.positionOnPO = origin;
		super.length = length;
	}
	
	public BiologicalSpine getBiologicalSpine() {
		return biologicalSpine;
	}

	public void setBiologicalSpine(BiologicalSpine biologicalSpine) {
		this.biologicalSpine = biologicalSpine;
	}

	@Override
	public boolean synapseWith(Excrescence otherExcressence, boolean createPhysicalBond) {
		// only if the other Excrescence is a bouton
		if(otherExcressence.getType() != BOUTON){
			(new Throwable(this+" is a spine, and thus can't synapse with "+otherExcressence)).printStackTrace();
			return false;
		}
		// making the references
		this.ex = otherExcressence;
		ex.setEx(this);
		// if needed, formation of the PhysicalBound
		if(createPhysicalBond){
			PhysicalBond pb = new PhysicalBond(
					super.po, super.positionOnPO,
					ex.getPo(),  ex.getPositionOnPO(), 
					Matrix.distance(super.getPo().transformCoordinatesPolarToGlobal(super.getPositionOnPO()),
							ex.getPo().transformCoordinatesPolarToGlobal(ex.getPositionOnPO())),
					1);
			// that we insert into the two PhysicalObjects
//			super.po.addPhysicalBond(pb);
//			ex.getPo().addPhysicalBond(pb);
		}
		// debugg : 
		System.out.println("*PhysicalSpine: We made a synapse between "+
				this.type+ " of "+
				po.getCellElement().getCell().getNeuroMLType()+ " and "+
				ex.getType()+" of "+
				ex.getPo().getCellElement().getCell().getNeuroMLType());
		return true;
	}

	public boolean synapseWithSoma(Excrescence otherExcrescence,
			boolean creatPhysicalBond) {
		return false;
	}
	

	public boolean synapseWithShaft(NeuriteElement otherNe, double maxDis, int nrSegments,
                                    boolean createPhysicalBond) {
		return false;
	}

}
