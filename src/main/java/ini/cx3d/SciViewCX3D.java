package ini.cx3d;

import cleargl.GLVector;
import graphics.scenery.*;
import ini.cx3d.physics.*;
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
		paintPhysicalSpheres();

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

    private void paintPhysicalSpheres() {
        Icosphere svSphere;
		for (int i = 0; i < ecm.physicalSphereList.size(); i++) {
			PhysicalSphere aSphere = ecm.physicalSphereList.get(i);
			float sphereRadius = 0.5f * (float) aSphere.getDiameter() * scaleFactor;
			GLVector mySomaMassLocation = new GLVector(aSphere.getMassLocationF()).times(scaleFactor);

			if( scNodes.containsKey(aSphere.getID()) ) {
                svSphere = (Icosphere) scNodes.get(aSphere.getID());
                svSphere.setPosition(mySomaMassLocation);
            } else {
			    svSphere = new Icosphere(sphereRadius, 2);
			    Material mat = new Material();
			    mat.setAmbient(new GLVector(0.1f, 0f, 0f));
                mat.setDiffuse(new GLVector(0.8f, 0.7f, 0.7f));
                mat.setDiffuse(new GLVector(0.05f, 0f, 0f));
                mat.setMetallic(0.01f);
                mat.setRoughness(0.5f);
                svSphere.setMaterial(mat);
			    sciView.addNode(svSphere);
			    scNodes.put(aSphere.getID(),svSphere);
            }

			Color sphereColor = aSphere.getColor();
			for (Substance sub : chemicals.keySet()) {
			    Node n = chemicals.get(sub);
			    if( n.getVisible() ) {
                    if (sub instanceof IntracellularSubstance) {
                        if (aSphere.getIntracellularSubstances().containsKey(
                                sub.getId())) {
                            sphereColor = aSphere.getIntracellularSubstances().get(
                                    sub.getId()).getConcentrationDependentColor();
                            if (aSphere.getSomaElement()
                                    .getLocalBiologyModulesList().size() > 0) {
                                GLVector cv = new GLVector((float) sphereColor.getRed()/255.0f, (float) sphereColor.getGreen()/255.0f, (float) sphereColor.getBlue()/255.0f);
                                svSphere.getMaterial().setDiffuse(cv);
                                svSphere.getMaterial().setAmbient(cv);
                                svSphere.getMaterial().setSpecular(cv);
                            }
                        }
                    }
                }
			}

		}

	}
}
