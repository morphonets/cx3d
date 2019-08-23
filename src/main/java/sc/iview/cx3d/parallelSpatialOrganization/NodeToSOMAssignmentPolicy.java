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

/**
 * Interface that provides the basic functionality to search for a SOM responsible for
 * a new node that is inserted at a given coordinate.
 * 
 * @author dennis
 *
 * @param <T>
 */
public interface NodeToSOMAssignmentPolicy<T> {
	/**
	 * Finds the SpatialOrganizationManager that is the default manager to take care of 
	 * nodes at a given coordinate.
	 * @param coordinate The coordinate where the new node should be inserted.
	 * @return A SpatialOrganizationManager that will be very happy to take care of the new node.
	 */
	public SpatialOrganizationManager<T> getResponsibleSOM(double[] coordinate);
}
