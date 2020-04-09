package sc.iview.cx3d;

import cleargl.GLVector;
import graphics.scenery.*;
import graphics.scenery.volumes.TransferFunction;
import graphics.scenery.volumes.Volume;

import net.imagej.lut.LUTService;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.display.AbstractArrayColorTable;
import net.imglib2.display.ColorTable;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.joml.Vector3f;
import org.scijava.Context;
import org.scijava.ui.UIService;
import sc.iview.SciView;
import sc.iview.cx3d.physics.IntracellularSubstance;
import sc.iview.cx3d.physics.PhysicalCylinder;
import sc.iview.cx3d.physics.PhysicalSphere;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.sciview.Spine;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.synapses.Excrescence;

import javax.print.attribute.AttributeSetUtilities;
import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Hashtable;

public class SciViewCX3D {
    private boolean drawSpines = true;

    public Context getContext() {
        return context;
    }

    public SciView getSciView() {
        return sciView;
    }

    private Context context;
    private UIService ui;
    private SciView sciView;
    private ECM ecm;

    private float scaleFactor = 0.01f;
    private boolean showSubstances = false;
    private boolean nodeEvents = true;

    public Node getCx3dGroup() {
        return cx3d;
    }

    private Node cx3d;// Meta node that contains all Cx3D nodes
    private HashMap<Substance, Node> chemicals;
    private HashMap<Integer, Node> scNodes;
    private HashMap<Substance, Node> volumes;

    public SciViewCX3D(Context context, UIService ui, SciView sciView, ECM ecm) {
        this.context = context;
        this.ui = ui;
        this.sciView = sciView;
        this.ecm = ecm;

        clear();
    }

    public void clear() {
//        if( cx3d != null ) {
//            for (Node child : cx3d.getChildren()) {
//                sciView.deleteNode(child, false);
//            }
//        }

        //sciView.reset();
        //sciView.getFloor().setVisible(false);

        cx3d = new Group();
        sciView.addNode(cx3d);
        chemicals = new HashMap<>();
        scNodes = new HashMap<>();
        volumes = new HashMap<>();
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

		// Loop through all PhysicalNodes FIXME currently this is done externally
		//paintPhysicalNodes();
    }

//    private void paintPhysicalNodes() {
//        Hashtable<Substance, RandomAccessible<? extends RealType>> imgSubs = ecm.getImgArtificialConcentration();
//        LUTService lutService = context.service(LUTService.class);
//        if( showSubstances ) {
//            for (Substance sub : imgSubs.keySet()) {
//                if (volumes.containsKey(sub)) {
//                    // Then the volume is there
//                } else {
//                    RandomAccessibleInterval<? extends RealType> img = imgSubs.get(sub);
//                    Cursor<RealType> cur = Views.iterable(img).cursor();
//                    while (cur.hasNext()) {
//                        cur.next();
//                        cur.get().mul(255.0);
//                    }
//                    OpService ops = getContext().service(OpService.class);
//
//                    RandomAccessibleInterval<UnsignedByteType> conv = Converters.convert(img, (a, b) -> b.setReal(a.getRealDouble()), new UnsignedByteType());
//
//                    Volume node = (Volume) sciView.addVolume(conv, sub.getId());
//
//                    cx3d.addChild(node);
//
//                    //String lutName = "Red.lut";
//                    String lutName = "Fire.lut";
//                    ColorTable colorTable = null;
//                    try {
//                        colorTable = lutService.loadLUT(lutService.findLUTs().get(lutName));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    sciView.setColormap(node, (AbstractArrayColorTable) colorTable);
//
//                    TransferFunction tf = ((Volume) sciView.getActiveNode()).getTransferFunction();
//                    //float currentOffset = tf.getControlPoint$scenery(1).getValue();
//                    //float currentFactor = tf.getControlPoint$scenery(2).getFactor();
//                    tf.clear();
//                    tf.addControlPoint(0.0f, 0.0f);
//                    tf.addControlPoint(0, 0.0f);
//                    tf.addControlPoint(1.0f, 0.01f);
//                    node.setPixelToWorldRatio(scaleFactor);
//                    node.setNeedsUpdate(true);
//                    volumes.put(sub, node);
//                }
//            }
//        }
//	}

    private void paintPhysicalCylinders() {
        Cylinder svCylinder;
        Sphere svSphere;
		for (int i = 0; i < ecm.physicalCylinderList.size(); i++) {
			PhysicalCylinder aCylinder = ecm.physicalCylinderList.get(i);
			Vector3f myNeuriteDistalEnd = new Vector3f(FloatBuffer.wrap(aCylinder.distalEndF())).mul(scaleFactor); // = massLocation
            Vector3f myNeuriteProximalEnd = new Vector3f(FloatBuffer.wrap(aCylinder.proximalEndF())).mul(scaleFactor);

            Color c = aCylinder.getColor();

            // Cylinders themselves
			if( scNodes.containsKey(aCylinder.getID()) ) {
                svCylinder = (Cylinder) scNodes.get(aCylinder.getID());
                svCylinder.setVisible(true);
                svCylinder.orientBetweenPoints(myNeuriteDistalEnd, myNeuriteProximalEnd, true, true);
            } else {
			    svCylinder = Cylinder.betweenPoints(myNeuriteDistalEnd, myNeuriteProximalEnd, (float)aCylinder.getDiameter() * scaleFactor, 1f, 12);
			    Material mat = new Material();
                Vector3f col = new Vector3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
//			    mat.setAmbient(new GLVector(0.1f, 0f, 0f));
//                mat.setDiffuse(new GLVector(0.8f, 0.7f, 0.7f));
//                mat.setDiffuse(new GLVector(0.05f, 0f, 0f));
                mat.setAmbient(col);
                mat.setDiffuse(col);
                mat.setDiffuse(col);
                //mat.setMetallic(0.01f);
                mat.setRoughness(0.5f);
                svCylinder.setMaterial(mat);
			    //sciView.addNode(svCylinder,nodeEvents);
			    cx3d.addChild(svCylinder);
			    svCylinder.setVisible(false);
			    scNodes.put(aCylinder.getID(),svCylinder);
            }

			// For spheres themselves
            int sphereLabel = -1 * aCylinder.getID();
            if( scNodes.containsKey(sphereLabel) ) {
                svSphere = (Sphere) scNodes.get(sphereLabel);
                svSphere.setVisible(true);
                svSphere.setPosition(myNeuriteProximalEnd);
            } else {
			    svSphere = new Sphere((float)aCylinder.getDiameter() * scaleFactor, 12);
			    Material mat = new Material();
			    Vector3f col = new Vector3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
//			    mat.setAmbient(new GLVector(0.1f, 0f, 0f));
//                mat.setDiffuse(new GLVector(0.8f, 0.7f, 0.7f));
//                mat.setDiffuse(new GLVector(0.05f, 0f, 0f));
                mat.setAmbient(col);
                mat.setDiffuse(col);
                mat.setDiffuse(col);
                //mat.setMetallic(0.01f);
                mat.setRoughness(0.5f);
                svSphere.setMaterial(mat);
			    //sciView.addNode(svCylinder,nodeEvents);
			    cx3d.addChild(svSphere);
			    svSphere.setVisible(true);
			    scNodes.put(sphereLabel,svSphere);
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
                                Vector3f cv = new Vector3f((float) drawcolor.getRed()/255.0f, (float) drawcolor.getGreen()/255.0f, (float) drawcolor.getBlue()/255.0f);
                                svCylinder.getMaterial().setDiffuse(cv);
                                svCylinder.getMaterial().setAmbient(cv);
                                svCylinder.getMaterial().setSpecular(cv);
                            }
                        }
                    }
                }
			}

			if (drawSpines) {
				for (Excrescence ex : aCylinder.getExcrescences()) {
                    if( scNodes.containsKey(ex.getID()) ) {
                        Spine svNode = (Spine) scNodes.get(ex.getID());
                        svNode.setVisible(true);
                        svNode.syncSpine(ex);
                        //System.out.println("Position: " + svNode.getPosition());
                    } else {
                        Spine svNode = Spine.createFromExcrescence(ex);
                        svNode.setVisible(false);
                        //sciView.addNode(svNode,nodeEvents);
                        cx3d.addChild(svNode);
                        scNodes.put(ex.getID(),svNode);
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
			Vector3f mySomaMassLocation = new Vector3f(FloatBuffer.wrap(aSphere.getMassLocationF())).mul(scaleFactor);

            Color c = aSphere.getColor();

			if( scNodes.containsKey(aSphere.getID()) ) {
                svSphere = (Icosphere) scNodes.get(aSphere.getID());
                svSphere.setVisible(true);
                svSphere.setPosition(mySomaMassLocation);
            } else {
			    svSphere = new Icosphere(sphereRadius, 2);
			    svSphere.setVisible(false);
			    Material mat = new Material();
			    Vector3f col = new Vector3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
			    mat.setAmbient(col);
                mat.setDiffuse(col);
                mat.setDiffuse(col);
                mat.setMetallic(0.01f);
                mat.setRoughness(0.5f);
                svSphere.setMaterial(mat);
			    //sciView.addNode(svSphere,nodeEvents);
			    cx3d.addChild(svSphere);
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
                                Vector3f cv = new Vector3f((float) sphereColor.getRed()/255.0f, (float) sphereColor.getGreen()/255.0f, (float) sphereColor.getBlue()/255.0f);
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

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }
}
