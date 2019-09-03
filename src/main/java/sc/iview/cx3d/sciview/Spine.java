package sc.iview.cx3d.sciview;

import cleargl.GLVector;
import graphics.scenery.Cylinder;
import graphics.scenery.Material;
import graphics.scenery.Node;
import graphics.scenery.Sphere;
import sc.iview.cx3d.synapses.Excrescence;
import sc.iview.cx3d.synapses.PhysicalSpine;

public class Spine extends Node {
    public static float scaleFactor = 0.01f;

    public static float neckRadius = 0.01f;

    public Cylinder getNeck() {
        return neck;
    }

    public Sphere getHead() {
        return head;
    }

    protected Cylinder neck;
    protected Sphere head;

    public void syncSpine(Excrescence ex) {
        PhysicalSpine spine = (PhysicalSpine) ex;
        GLVector distalExEnd = new GLVector(ex.getDistalEndF()).times(scaleFactor); // = massLocation
        GLVector proximalExEnd = new GLVector(ex.getProximalEndF()).times(scaleFactor);

        float thickness = (float) spine.getLength();

        float r = 0.8f;
        float g = 0.1f;
        float b = 0.2f;

        Material mat = head.getMaterial();
        mat.setAmbient(new GLVector(r,g,b));
        mat.setDiffuse(new GLVector(r,g,b));
        mat.setSpecular(new GLVector(0.05f, 0f, 0f));

        head.setScale(new GLVector(thickness,thickness,thickness));
        orientBetweenPoints(distalExEnd, proximalExEnd, true, true);
        setDirty(true);
        setNeedsUpdate(true);
        // TODO should we update the ex?
        //getMetadata().put("excrescence",ex);
    }

    public static Spine createFromExcrescence(Excrescence ex) {
        Spine spine = new Spine();
        GLVector distalExEnd = new GLVector(ex.getDistalEndF()).times(scaleFactor); // = massLocation
        GLVector proximalExEnd = new GLVector(ex.getProximalEndF()).times(scaleFactor);

        spine.neck = Cylinder.betweenPoints(distalExEnd, proximalExEnd, (float)neckRadius, 1f, 5);
        spine.head = new Sphere(neckRadius*2,5);
        spine.head.setPosition(new GLVector(distalExEnd));

        Material mat = new Material();
        mat.setAmbient(new GLVector(0.0f, 0.6f, 0.1f));
        mat.setDiffuse(new GLVector(0.8f, 0.7f, 0.7f));
        mat.setSpecular(new GLVector(0.05f, 0f, 0f));
        mat.setMetallic(0.01f);
        mat.setRoughness(0.5f);

        spine.neck.setMaterial(mat);
        spine.head.setMaterial(mat);
        spine.addChild(spine.neck);
        spine.addChild(spine.head);
        spine.getMetadata().put("excrescence",ex);

        return spine;
    }
}
