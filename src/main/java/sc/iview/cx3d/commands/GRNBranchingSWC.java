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
package sc.iview.cx3d.commands;

import fun.grn.grneat.evaluators.GRNGenomeEvaluator;
import fun.grn.grneat.evolver.GRNGenome;
import fun.grn.grneat.grn.GRNModel;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.Colors;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.TreeAnalyzer;
import sc.fiji.snt.analysis.graph.GraphUtils;
import sc.iview.SciView;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.simulations.tutorial.ActiveNeuriteChemoAttraction;
import sc.iview.cx3d.utilities.SNT;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_LINES;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Genetically-regulated Branching (SWC output)", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Genetically-regulated Branching (SWC output)", weight = DEMO_LINES) })
public class GRNBranchingSWC implements Command {

    @Parameter
    private SciView sciView;

    @Parameter
    private Context context;

    @Parameter
    private SNTService sntService;

    @Parameter
    private LogService logService;

    //@Parameter(type = ItemIO.OUTPUT)
//    @Parameter
//    private Tree tree;

//    @Parameter(style="save")
//    private File file = new File("random_branching.swc");

    @Parameter(label="Filepath for SWC", persist=false)
    private String filenameSWC;

    @Parameter(label="File path for a GRN in GRNEAT format")
    private String filenameGRN;

    @Parameter(label = "Should a new GRN be generated and written to the filename")
    private boolean generateGRN;

    @Parameter(label = "Simulation end time", persist = false)
    private float maxTime = 2;

    @Parameter(label = "Random seed", persist = true)
    private long randomSeed = 17L;

    @Parameter
    private String filenameStats;

    @Override
    public void run() {
        String outline = "";

        System.out.println("Running with:");
        System.out.println("Random seed = " + randomSeed);
        System.out.println("filenameSWC = " + filenameSWC);
        System.out.println("filenameGRN = " + filenameGRN);
        System.out.println("filenameStats = " + filenameStats);
        System.out.println("maxTime = " + maxTime);

        outline += randomSeed + "\t" + maxTime + "\t" + filenameGRN + "\t";

        //ECM ecm = ECM.getInstance(getContext());
        ECM ecm = ECM.getInstance(context);

        ECM.setRandomSeed(randomSeed);

        ecm.clearAll();
        ecm.resetTime();
        ecm.getSciViewCX3D().clear();

        Substance A = new Substance("A",Color.magenta);
		ecm.addArtificialGaussianConcentrationZ(A, 1.0, 300.0, 160.0);

		Substance B = new Substance("B",Color.magenta);
		ecm.addArtificialGaussianConcentrationX(B, 1.0, 300.0, 160.0);

		Substance C = new Substance("C",Color.magenta);
		ecm.addArtificialGaussianConcentrationY(C, 1.0, 300.0, 160.0);

		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		}

        Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
        c.setColorForAllPhysicalObjects(Param.GRAY);
        double[] pos = c.getSomaElement().getLocation();
        NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
        neurite.getPhysicalCylinder().setDiameter(2);

        if( generateGRN ) {
            try {
                //ActiveNeuriteChemoAttraction.writeRandomGRNToFile(filenameGRN);
                ActiveNeuriteChemoAttraction.writePredicateFilteredRandomGRNToFile(filenameGRN, ActiveNeuriteChemoAttraction.grnPredicate, randomSeed);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ActiveNeuriteChemoAttraction branchingModule = new ActiveNeuriteChemoAttraction(filenameGRN);
        branchingModule.getGrnModule().setCell(c);
        c.getSomaElement().addLocalBiologyModule(branchingModule.getGrnModule());
        neurite.addLocalBiologyModule(branchingModule);

        System.out.println("simulating");

		Scheduler.simulate(maxTime);

		sciView.centerOnNode(ecm.getSciViewCX3D().getCx3dGroup());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("simulation done");

        DefaultDirectedGraph graph = sc.iview.cx3d.utilities.GraphUtils.cellToGraph(c);

        System.out.println("graph created");

        // This should work for Cx3D trees
        Tree realtree = GraphUtils.createTree(graph);
        realtree.setLabel("Cx3D_Tree");
        realtree.setColor(Colors.RED);

        TreeAnalyzer ta = new TreeAnalyzer(realtree);

        // TODO fix measurements
        List<String> metrics = new ArrayList<>(SNT.getAvailableTreeAnalyzerMetrics());
        List<Number> measurements = new ArrayList<>();
        outline = "";
        for( String metric : metrics ) {
            measurements.add(ta.getMetric(metric));
            outline += ta.getMetric(metric) + "\t";
        }

        // FIXME hack
        metrics.add("randomSeed");
        outline += randomSeed + "\t";
        metrics.add("filenameGRN");
        outline += filenameGRN + "\t";

        outline = outline.substring(0,outline.length()-1);

        System.out.println("tree created");

        realtree.saveAsSWC(filenameSWC);

        System.out.println("SWC saved to " + filenameSWC);

        boolean statFileExists = new File(filenameStats).exists();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filenameStats,true));

            if(!statFileExists) {
                String header = "";

                for( String metric : metrics ) {
                    header += metric + "\t";
                }
                bw.write(header.substring(0,header.length()-1) + "\n");// drop the last tab w/ substr
            }

            bw.write(outline + "\n");

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.exit(0);

//        tree.merge(realtree);
//        sntService.initialize(true);
//        sntService.getPathAndFillManager().clear();
//        sntService.loadTree(tree);

    }

    public static void main( String... args ) {
        SciView sciView = SciView.createSciView();
        CommandService commandService = sciView.getScijavaContext().service(CommandService.class);

        Map<String, Object> argmap = new HashMap<>();
        argmap.put("filenameSWC", "test_17.swc");
        argmap.put("filenameGRN", "test_17.grn");
        argmap.put("filenameStats", "test_17.csv");
        argmap.put("generateGRN", true);
        argmap.put("maxTime", 6);
        argmap.put("randomSeed", 917171717);

        commandService.run(GRNBranchingSWC.class,true, argmap);
    }
}
