package ini.cx3d;

import cleargl.GLVector;
import graphics.scenery.Cylinder;
import graphics.scenery.Material;
import graphics.scenery.Node;
import ini.cx3d.physics.IntracellularSubstance;
import ini.cx3d.physics.PhysicalCylinder;
import ini.cx3d.physics.PhysicalObject;
import ini.cx3d.physics.Substance;
import ini.cx3d.simulations.ECM;
import org.scijava.Context;
import org.scijava.ui.UIService;
import sc.iview.SciView;

import javax.print.attribute.AttributeSetUtilities;
import java.awt.*;
import java.util.HashMap;

public class SciViewCX3D {
    private Context context;
    private UIService ui;
    private SciView sciView;
    private ECM ecm;

    private float scaleFactor = 0.01f;

    private HashMap<Substance, Node> chemicals;
    private HashMap<Integer, Node> scNodes;

    public SciViewCX3D(Context context, UIService ui, SciView sciView, ECM ecm) {
        this.context = context;
        this.ui = ui;
        this.sciView = sciView;
        this.ecm = ecm;

        chemicals = new HashMap<>();
        scNodes = new HashMap<>();
    }

    public void addNewChemical(Substance substance) {
        chemicals.put(substance, new Node());
    }


    /*
     * Sync all of the information from CX3D into sciview
     */
    public void syncCX3D() {
        // Loop through all Spheres
		//paintPhysicalSpheres();

		// Loop through all Cylinders
		paintPhysicalCylinders();

		// Loop through all PhysicalNodes
		//paintPhysicalNodes(g2D);
    }

    private void paintPhysicalCylinders() {
        Cylinder svCylinder;
		for (int i = 0; i < ecm.physicalCylinderList.size(); i++) {
			PhysicalCylinder aCylinder = ecm.physicalCylinderList.get(i);
			GLVector myNeuriteDistalEnd = new GLVector(aCylinder.distalEndF()).times(scaleFactor); // = massLocation
            GLVector myNeuriteProximalEnd = new GLVector(aCylinder.proximalEndF()).times(scaleFactor);

			if( scNodes.containsKey(aCylinder.getID()) ) {
                svCylinder = (Cylinder) scNodes.get(aCylinder.getID());

                svCylinder.orientBetweenPoints(myNeuriteDistalEnd, myNeuriteProximalEnd, true, true);
            } else {
			    svCylinder = Cylinder.betweenPoints(myNeuriteDistalEnd, myNeuriteProximalEnd, (float)aCylinder.getDiameter() * scaleFactor, 1f, 12);
			    Material mat = new Material();
			    mat.setAmbient(new GLVector(0.1f, 0f, 0f));
                mat.setDiffuse(new GLVector(0.8f, 0.7f, 0.7f));
                mat.setDiffuse(new GLVector(0.05f, 0f, 0f));
                mat.setMetallic(0.01f);
                mat.setRoughness(0.5f);
                svCylinder.setMaterial(mat);
			    sciView.addNode(svCylinder);
			    scNodes.put(aCylinder.getID(),svCylinder);
            }


			// ***chagned by haurian: get the drawing color form the internally
			// secreted stuff
			// if it has been selected to be drawn otherwise use the standard of
			// the object
			Color drawcolor = aCylinder.getColor();
			for (Substance sub : chemicals.keySet()) {
			    Node n = chemicals.get(sub);
			    if( n.getVisible() ) {
                    if (sub instanceof IntracellularSubstance) {
                        if (aCylinder.getIntracellularSubstances().containsKey(
                                sub.getId())) {
                            drawcolor = aCylinder.getIntracellularSubstances().get(
                                    sub.getId()).getConcentrationDependentColor();
                            if (aCylinder.getNeuriteElement()
                                    .getLocalBiologyModulesList().size() > 0) {
                                GLVector cv = new GLVector((float) drawcolor.getRed()/255.0f, (float) drawcolor.getGreen()/255.0f, (float) drawcolor.getBlue()/255.0f);
                                svCylinder.getMaterial().setDiffuse(cv);
                                svCylinder.getMaterial().setAmbient(cv);
                                svCylinder.getMaterial().setSpecular(cv);
                            }
                        }
                    }
                }
			}

			// end haurian changes

		}

	}

//    private void paintPhysicalSpheres() {
//
//		// modified by sabina: uncheck this code to sort the physicalSpheres
//		// according to the viewer position.
//		if (this.sortDraw) {
//			Collections.sort(ecm.physicalSphereList, new SortPhysicalObjects());
//		}
//
//		for (int i = 0; i < ecm.physicalSphereList.size(); i++) {
//			PhysicalSphere aSphere = ecm.physicalSphereList.get(i);
//
//			double sphereRadius = 0.5 * aSphere.getDiameter();
//			double[] mySomaMassLocation = aSphere.getMassLocation();
//
//			double x0, y0, x1, y1;
//			double[] newCoord;
//
//			mySomaMassLocation = mult(V, mySomaMassLocation);
//			newCoord = getDisplayCoord(mySomaMassLocation);
//
//			// find the min max cords for exporting a picture!
//			minXdrawn = Math.min(minXdrawn, newCoord[0]);
//			maxXdrawn = Math.max(maxXdrawn, newCoord[0]);
//			minZdrawn = Math.min(minZdrawn, newCoord[1]);
//			maxZdrawn = Math.max(maxZdrawn, newCoord[1]);
//
//			x0 = newCoord[0];
//			y0 = newCoord[1];
//			// ***changed by haurian: get the drawing color form the internally
//			// secreted stuff
//			// if it has been selected to be drawn otherwise use the standard of
//			// the object
//			Color sphereColor = aSphere.getColor();
//			for (Substance sub : tobeDrawn) {
//				if (sub instanceof IntracellularSubstance) {
//					if (aSphere.getIntracellularSubstances().containsKey(
//							sub.getId())) {
//						sphereColor = aSphere.getIntracellularSubstances().get(
//								sub.getId()).getConcentrationDependentColor();
//						if (aSphere.getSomaElement()
//								.getLocalBiologyModulesList().size() > 0) {
//							paintrealSubstance(g2D, aSphere
//									.getIntracellularSubstances().get(
//											sub.getId()), newCoord);
//						}
//						// System.out.println("concentration sphere
//						// "+aSphere.getIntracellularSubstances().get(sub.getId()).getConcentration());
//					}
//				}
//			}
//			// ***
//			// if a "pale cell", we might not consider it
//			// if(drawPaleCells==false && sphereColor.getAlpha()<100){
//			// continue;
//			// }
//
//			// if outside the slice (in case of slice type of representation
//			// type), we skip this somaElement:
//			if (representationType == OM_SLICE_TYPE
//					&& !isInsideTheSlice(mySomaMassLocation, OM_SLICE_THICKNESS)) {
//				continue;
//			} else if (representationType == EM_SLICE_TYPE
//					&& Math.abs(mySomaMassLocation[1] - sliceYPosition) > sphereRadius
//							+ addedToTheRadius) {
//				continue;
//			}
//			// a few temporay local variables
//
//			Ellipse2D.Double E1;
//			Line2D.Double L;
//
//			// links to the neighboring vertices in the
//			// triangulation.................
//			if (drawDelaunayNeighbors) {
//				paintDelaunayNeighbors(g2D, aSphere.getColor(), aSphere
//						.getSoNode());
//			}
//
//			// The somaElement itself
//			// ........................................................
//			g2D.setPaint(sphereColor);
//
//			double radius = getSphereApparentRadius(mySomaMassLocation,
//					sphereRadius);
//			E1 = new Ellipse2D.Double(x0 - radius, y0 - radius, radius * 2,
//					radius * 2);
//			boolean fillSoma = true; // if false, only border and center are
//			// painted
//			if (fillSoma) {
//				g2D.fill(E1);
//			} else {
//				// the border
//				chooseCorrectLineThickness(getMagnification(), g2D);
//				g2D.draw(E1);
//				// the center
//				double massDiameter = 2 * getMagnification()
//						* getScaleFactor(aSphere.getMassLocation());
//				massDiameter = Math.min(massDiameter, 10);
//				E1 = new Ellipse2D.Double(x0 - massDiameter, y0 - massDiameter,
//						2 * massDiameter, 2 * massDiameter);
//				// g2D.fill(E1);
//			}
//
//			if (drawEffectiveSphereRadius) {
//				radius = getSphereApparentRadius(mySomaMassLocation,
//						sphereRadius + addedToTheRadius);
//				E1 = new Ellipse2D.Double(x0 - radius, y0 - radius, radius * 2,
//						radius * 2);
//				g2D.setColor(Color.black);
//				chooseCorrectLineThickness(0.5 * getMagnification(), g2D);
//				g2D.draw(E1);
//			}
//
//			// inserted by roman
//			// if excrescences on soma, we also draw them
//			// .............................
//			if (drawSpines) {
//
//				for (Excrescence ex : aSphere.getExcrescences()) {
//					double[] proximalExEnd = mult(V, ex.getProximalEnd());
//					double[] distalExEnd = mult(V, ex.getDistalEnd());
//					if (representationType != PROJECTION_TYPE) {
//						double[][] twoEndsOfTheNeurite = getCylinderApparentEndPoints(
//								proximalExEnd, distalExEnd);
//						newCoord = twoEndsOfTheNeurite[0];
//						newCoord = getDisplayCoord(newCoord);
//
//						x0 = newCoord[0];
//						y0 = newCoord[1];
//						newCoord = twoEndsOfTheNeurite[1];
//						newCoord = getDisplayCoord(newCoord);
//
//						x1 = newCoord[0];
//						y1 = newCoord[1];
//					} else {
//						newCoord = getDisplayCoord(proximalExEnd);
//						x0 = newCoord[0];
//						y0 = newCoord[1];
//						newCoord = getDisplayCoord(distalExEnd);
//						x1 = newCoord[0];
//						y1 = newCoord[1];
//					}
//					L = new Line2D.Double(x0, y0, x1, y1);
//					g2D.draw(L);
//				}
//
//			}
//
//			// if PhysicalBonds, we also draw them .............................
//			if (drawPhysicalBonds) {
//				for (PhysicalBond pb : aSphere.getPhysicalBonds()) {
//					double[] firstEnd = mult(V, pb.getFirstEndLocation()); // rotation
//					double[] secondEnd = mult(V, pb.getSecondEndLocation());
//					if (false && representationType != PROJECTION_TYPE) {
//						double[][] twoEndsOfTheNeurite = getCylinderApparentEndPoints(
//								firstEnd, secondEnd);
//						newCoord = twoEndsOfTheNeurite[0];
//						newCoord = getDisplayCoord(newCoord);
//						x0 = newCoord[0];
//						y0 = newCoord[1];
//						newCoord = twoEndsOfTheNeurite[1];
//						newCoord = getDisplayCoord(newCoord);
//						x1 = newCoord[0];
//						y1 = newCoord[1];
//					} else {
//						newCoord = getDisplayCoord(firstEnd);
//						x0 = newCoord[0];
//						y0 = newCoord[1];
//						newCoord = getDisplayCoord(secondEnd);
//						x1 = newCoord[0];
//						y1 = newCoord[1];
//					}
//					L = new Line2D.Double(x0, y0, x1, y1);
//					g2D.setColor(Param.X_SOLID_GRAY);
//					g2D.setColor(Color.black);
//					g2D.draw(L);
//				}
//			}
//
//			// until here: inserted by roman
//
//			// the Force acting on the somaElement
//			double[] forceOnASphere = aSphere.getTotalForceLastTimeStep();
//			if (drawForces && forceOnASphere[3] > 0) {
//				double[] myForceEnd = ini.cx3d.utilities.Matrix.add(aSphere
//						.getMassLocation(), forceOnASphere);
//				myForceEnd = mult(V, myForceEnd);
//				newCoord = getDisplayCoord(myForceEnd);
//				x1 = newCoord[0];
//				y1 = newCoord[1];
//				L = new Line2D.Double(x0, y0, x1, y1);
//				g2D.setColor(Color.black);
//				chooseCorrectLineThickness(0.5 * getMagnification(), g2D);
//				g2D.draw(L);
//
//				// arrow head :
//				double theta = 0.5;
//				double[] arrow = { x0 - x1, y0 - y1 };
//				arrow = scalarMult(3 * getMagnification(), normalize(arrow));
//				double[] arrowSide1 = {
//						arrow[0] * Math.cos(theta) - arrow[1] * Math.sin(theta),
//						arrow[0] * Math.sin(theta) + arrow[1] * Math.cos(theta) };
//				double[] arrowSide2 = {
//						arrow[0] * Math.cos(-theta) - arrow[1]
//								* Math.sin(-theta),
//						arrow[0] * Math.sin(-theta) + arrow[1]
//								* Math.cos(-theta) };
//
//				L = new Line2D.Double(x1, y1, x1 + arrowSide1[0], y1
//						+ arrowSide1[1]);
//				g2D.draw(L);
//				L = new Line2D.Double(x1, y1, x1 + arrowSide2[0], y1
//						+ arrowSide2[1]);
//				g2D.draw(L);
//			}
//
//		}
//
//	}
}
