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

package sc.iview.cx3d.parallelSpatialOrganization;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import sc.iview.cx3d.spatialOrganization.PositionNotAllowedException;
import sc.iview.cx3d.spatialOrganization.SpatialOrganizationEdge;
import sc.iview.cx3d.spatialOrganization.SpatialOrganizationNode;
import sc.iview.cx3d.spatialOrganization.SpatialOrganizationNodeMovementListener;


/** 
 * 
 * @author Toby Weston, Dennis Goehlsdorf
 * @version 0.1
 * @Date: 1/8/2008
 * 
 * Encapsulates the details of interacting with the remote parallelSpatialOrganization 
 * SpaceNode.
 * Implements the interface: SpatialOrganizationNode to provide backwards compatibility with the existing 
 * biological simulation.
 */

public class SpaceNodeFacade<T> implements SpatialOrganizationNode<T>, Serializable {
	//Get a logger
	private static Logger theLogger = Logger.getLogger(SpaceNodeFacade.class.getName());	
	///////////////////////////////////////////////////////////////////
	/// Replace with config. file at some point.....
	///////////////////////////////////////////////////////////////////	
	{
		theLogger.setLevel(Level.FINEST);
		//theLogger.setLevel(Level.INFO);
	}
	///////////////////////////////////////////////////////////////////	
	
	ManagedObjectReference<T> myReference;
	
	static final long serialVersionUID = 947274274827772L; 
	
	/**
	 * Creates a new instance of SpaceNodeFacade. 
	 * @param myNodeReference The reference that the new facade will be associated with.
	 */
	public SpaceNodeFacade(ManagedObjectReference<T> myNodeReference) {
		this.myReference = myNodeReference;
	}
	

	/**
	 * @param listener
	 * @see SpatialOrganizationNode#addSpatialOrganizationNodeMovementListener(SpatialOrganizationNodeMovementListener)
	 */
	public void addSpatialOrganizationNodeMovementListener(SpatialOrganizationNodeMovementListener<T> listener) {
		// TODO yeah, thats a problem, too...
		
	}
	

	/**
	 * Delegates the moveFrom call to the SOM.
	 * @param delta
	 * @throws PositionNotAllowedException
	 * @see SpatialOrganizationNode#moveFrom(double[])
	 */
	public void moveFrom(double[] delta) throws PositionNotAllowedException {
		myReference.getSOM().addMovementAction(myReference, delta);
	}
	
	/**
	 * Delegates the getPosition call to the SOM.
	 * @return the position of the SpaceNode
	 * @see SpatialOrganizationNode#getPosition()
	 */
	public double[] getPosition() {
		try {
			return myReference.getSOM().getNodePosition(myReference);
		} catch (ManagedObjectDoesNotExistException e) {
			throw new RuntimeException("Request for the position of an unknown node!");
		}
	}

	/**
	 * @return
	 * @see SpatialOrganizationNode#getEdges()
	 */
	public Iterable<SpatialOrganizationEdge<T>> getEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @see SpatialOrganizationNode#getNeighbors()
	 */
	public Iterable<T> getNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param position
	 * @param userObject
	 * @return
	 * @throws PositionNotAllowedException
	 * @see SpatialOrganizationNode#getNewInstance(double[], T)
	 */
	public SpatialOrganizationNode<T> getNewInstance(double[] position, T userObject) throws PositionNotAllowedException {
		theLogger.finer("In SpaceNodeFacade --- getNewInstance()");
		theLogger.finer("before getting SOM");
		SpatialOrganizationManager<T> tempSOM = myReference.getSOM();
		
		theLogger.finer("before getting ref");
		ManagedObjectReference<T> tempRef  =  tempSOM.addInsertionAction(userObject, position, myReference);

		theLogger.finest("make new facade and return");
		return new SpaceNodeFacade<T>(tempRef);
	}

	/**
	 * @return
	 * @see SpatialOrganizationNode#getPermanentListOfNeighbors()
	 */
	public Iterable getPermanentListOfNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @return
	 * @see SpatialOrganizationNode#getUserObject()
	 */
	public T getUserObject() {
		try {
			return myReference.getSOM().getNodeUserObject(myReference);
		} catch (ManagedObjectDoesNotExistException e) {
			throw new RuntimeException("Request for the position of an unknown node!");
		}
	}

	/**
	 * @param position
	 * @return
	 * @see SpatialOrganizationNode#getVerticesOfTheTetrahedronContaining(double[])
	 */
	public Object[] getVerticesOfTheTetrahedronContaining(double[] position) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return
	 * @see SpatialOrganizationNode#getVolume()
	 */
	public double getVolume() {
		try {
			return myReference.getSOM().getNodeVolume(myReference);
		} catch (ManagedObjectDoesNotExistException e) {
			throw new RuntimeException("Request for the volume of an unknown node!");
		}
	}


	/**
	 * 
	 * @see SpatialOrganizationNode#remove()
	 */
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	

}
