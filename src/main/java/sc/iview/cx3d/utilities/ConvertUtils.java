package sc.iview.cx3d.utilities;

import java.util.HashMap;
import java.util.Vector;

import sc.fiji.snt.Path;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.graph.DirectedWeightedGraph;
import sc.fiji.snt.util.SWCPoint;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.localBiology.NeuriteElement;

public class ConvertUtils {

	private ConvertUtils() {
		// Prevent instantiation of private class
	}

    static public DirectedWeightedGraph cellToGraph(Cell c) {
    	return new DirectedWeightedGraph(cellToTree(c));
    }
   
    static public Tree cellToTree(Cell c) {
        
        Vector<NeuriteElement> neurites = c.getNeuriteElements();

        // We cannot use the soma's position because the radius is used to offset the proximal position of the initial neurite
        //pos = c.getSomaElement().getPhysicalSphere().getMassLocation();
        // Instead we will do something very bad and assume the first neurite connects to the soma
        double[] pos = neurites.get(0).getPhysicalCylinder().proximalEnd();

        //System.out.println("Soma radius: " + c.getSomaElement().getPhysicalSphere().getLength());
        SWCPoint soma = new SWCPoint(0, Path.SWC_SOMA, pos[0], pos[1], pos[2], 1, -1);
        //System.out.println("Soma: " + soma);

        // Make a hash map of NeuriteElements of point to ID
        // To lookup (e.g., parent), ask tree for closest index to a given position

        // When we assign coordinates to SWC points we use the distal end, because the soma is at the proximal end of
        //   the first neurite

        HashMap<Long, SWCPoint> indexToCoordinate = new HashMap<>();
        indexToCoordinate.put(0l,soma);
        for( long k = 0; k < neurites.size(); k++ ) {
            NeuriteElement ne = neurites.get((int) k);
            //pos = ne.getLocation();
            pos = ne.getPhysicalCylinder().distalEnd();
            SWCPoint swc = new SWCPoint((int)k+1, Path.SWC_UNDEFINED, pos[0], pos[1], pos[2], 1, 0);// TODO casting
            indexToCoordinate.put(k+1, swc);
        }

        // TODO: now create a graph
        for( long k = 0; k < neurites.size(); k++ ) {
            NeuriteElement ne = neurites.get((int) k);
            double[] proximalPos = ne.getPhysicalCylinder().proximalEnd();
            double[] distalPos = ne.getPhysicalCylinder().distalEnd();
            //System.out.println(k+1 + " Proximal: " + proximalPos[0] + ", " + proximalPos[1] + ", " + proximalPos[2] + " Distal: " + distalPos[0] + ", " + distalPos[1] + ", " + distalPos[2] );

            // Find mininum distance point and use as parentIdx
            int parentIdx = 0;
            double parentVal = Double.POSITIVE_INFINITY;
            double[] a = proximalPos;
            //for (int j = 0; j < neurites.size(); j++) {
            for( long j : indexToCoordinate.keySet() ) {
                double dist = 0;
                //double[] b = neurites.get(j).getLocation();
                //double[] b = neurites.get(j).getPhysicalCylinder().proximalEnd();
                double[] b = new double[]{indexToCoordinate.get(j).x, indexToCoordinate.get(j).y, indexToCoordinate.get(j).z};
                for (int d = 0; d < 3; d++) {
                    dist += Math.pow(a[d] - b[d], 2);
                }
                if (dist < parentVal && j != k+1) {
                    parentIdx = indexToCoordinate.get(j).id;
                    parentVal = dist;
                }
            }
            SWCPoint swc = new SWCPoint((int) k + 1, 0, distalPos[0], distalPos[1], distalPos[2], 1, parentIdx);
            indexToCoordinate.put(k,swc);
        }

        return new Tree(indexToCoordinate.values(), "");
    }
}
