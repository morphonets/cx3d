/*-
 * #%L
 * Scenery-backed 3D visualization package for ImageJ.
 * %%
 * Copyright (C) 2016 - 2018 SciView developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.iview.commands.cx3d;

import graphics.scenery.SceneryBase;
import ini.cx3d.Param;
import ini.cx3d.cells.Cell;
import ini.cx3d.cells.CellFactory;
import ini.cx3d.localBiology.NeuriteElement;
import ini.cx3d.simulations.ECM;
import ini.cx3d.simulations.Scheduler;
import ini.cx3d.simulations.tutorial.RandomBranchingModule;
import io.scif.SCIFIOService;
import net.imagej.ImageJService;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.util.Colors;
import org.scijava.widget.Button;
import sc.fiji.snt.Path;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.graph.GraphUtils;
import sc.fiji.snt.util.PointInImage;
import sc.fiji.snt.util.SWCPoint;
import sc.fiji.snt.viewer.Viewer3D;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.vector.ClearGLVector3;
import sc.iview.vector.Vector3;

import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

import static ini.cx3d.utilities.Matrix.randomNoise;
import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_LINES;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Random Branching", menuRoot = "SciView", //
        menu = { @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Random Branching", weight = DEMO_LINES) })
public class RandomBranchingDemo implements Command {

    @Parameter
    private SciView sciView;

    @Parameter
    private Context context;

    @Parameter
    private SNTService sntService;

    @Parameter(type = ItemIO.OUTPUT)
    private Tree tree;

    @Parameter(label = "Simulation end time")
    private float maxTime = 2;

    public static void main( String... args ) {
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class);

        //UIService ui = context.service( UIService.class );
        //if( !ui.isVisible() ) ui.showUI();

        // Currently Cx3D demos need to make their own SciView instance
        SciViewService sciViewService = context.service( SciViewService.class );
        SciView sciView = sciViewService.getOrCreateActiveSciView();

//        CommandService commandService = context.service(CommandService.class);
//        commandService.run(RandomBranchingDemo.class,true,new Object[]{});
    }

    @Override
    public void run() {
        //ECM ecm = ECM.getInstance(getContext());
        ECM ecm = ECM.getInstance(context);
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		}
		ECM.setRandomSeed(7L);

        Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
        c.setColorForAllPhysicalObjects(Param.GRAY);
        double[] pos = c.getSomaElement().getLocation();
        NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
        neurite.getPhysicalCylinder().setDiameter(2);
        neurite.addLocalBiologyModule(new RandomBranchingModule());

		Scheduler.simulate(maxTime);

		Vector<NeuriteElement> neurites = c.getNeuriteElements();

		// TODO: make a Graph() from Cell c
        final DefaultDirectedWeightedGraph<SWCPoint, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(
                null);
        // We cannot use the soma's position because the radius is used to offset the proximal position of the initial neurite
        //pos = c.getSomaElement().getPhysicalSphere().getMassLocation();
        // Instead we will do something very bad and assume the first neurite connects to the soma
        pos = neurites.get(0).getPhysicalCylinder().proximalEnd();

        //System.out.println("Soma radius: " + c.getSomaElement().getPhysicalSphere().getLength());
        SWCPoint soma = new SWCPoint(0, 1, pos[0], pos[1], pos[2], 1, -1);
        graph.addVertex(soma);
        System.out.println("Soma: " + soma);

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
            SWCPoint swc = new SWCPoint((int)k+1, 0, pos[0], pos[1], pos[2], 1, 0);// TODO casting
            indexToCoordinate.put(k+1, swc);
        }

        // TODO: now create a graph
        for( long k = 0; k < neurites.size(); k++ ) {
            NeuriteElement ne = neurites.get((int) k);
            double[] proximalPos = ne.getPhysicalCylinder().proximalEnd();
            double[] distalPos = ne.getPhysicalCylinder().distalEnd();
            System.out.println(k+1 + " Proximal: " + proximalPos[0] + ", " + proximalPos[1] + ", " + proximalPos[2] + " Distal: " + distalPos[0] + ", " + distalPos[1] + ", " + distalPos[2] );

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
            graph.addVertex(swc);
            indexToCoordinate.put(k,swc);
        }

        // Have to make edges after all vertices are added
        for( long k = 0; k < neurites.size(); k++ ) {
            NeuriteElement ne = neurites.get((int) k);
            SWCPoint swc = indexToCoordinate.get(k);
            long parentIdx = swc.parent;
            System.out.println(k + " " + swc);

            final DefaultWeightedEdge edge = new DefaultWeightedEdge();
            graph.addEdge(indexToCoordinate.get(k),indexToCoordinate.get(parentIdx),edge);
        }

        // This should work for Cx3D trees
        Tree tree = GraphUtils.createTree(graph);
        tree.setColor(Colors.RED);
        sntService.initialize(true);
        sntService.loadTree(tree);

        Viewer3D recViewer = new Viewer3D(context);
        recViewer.add(tree);
        recViewer.show();


        // Checking with SNT test
//        SNTService sntService = context.getService(SNTService.class);
//        Tree initialTree = sntService.demoTree();
//        DefaultDirectedGraph<SWCPoint, DefaultWeightedEdge> g = initialTree.getGraph();
//        Viewer3D recViewer = new Viewer3D(context);
//        Tree convertedTree = GraphUtils.createTree(g);
//        initialTree.setColor(Colors.RED);
//        recViewer.add(initialTree);
//        convertedTree.setColor(Colors.CYAN);
//        recViewer.add(convertedTree);
//        recViewer.show();
//        //GraphUtils.show(g);
//        sntService.initialize(true);
//        sntService.loadTree(convertedTree);
        //GraphUtils.show(graph);

    }
}
