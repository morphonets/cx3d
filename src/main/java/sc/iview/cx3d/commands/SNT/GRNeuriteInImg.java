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
package sc.iview.cx3d.commands.SNT;

import cleargl.GLVector;
import graphics.scenery.volumes.bdv.Volume;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
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
import sc.iview.cx3d.simulations.grn.ChemoAttractant;
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

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_LINES;
import static sc.iview.cx3d.commands.FRAChemoAttractionNeurite.gaussianConcentration;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Genetically-regulated Neurite in Img (SWC output)", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Genetically-regulated Neurite in Img (SWC output)", weight = DEMO_LINES) })
public class GRNeuriteInImg implements Command {

    @Parameter
    private boolean sciViewEnabled;

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

        ECM.setSciviewEnabled(sciViewEnabled);

        //ECM ecm = ECM.getInstance(getContext());
        ECM ecm = ECM.getInstance(context);

        ECM.setRandomSeed(randomSeed);

        ecm.clearAll();
        ecm.resetTime();

        if( ECM.isSciviewEnabled() )
            ecm.getSciViewCX3D().clear();

        //ChemoAttractant A = ChemoAttractant.createGaussianAttractor(ecm, 2, 300);//160 sigma; original CX3d version
        ChemoAttractant A = ChemoAttractant.createGaussianImgAttractor(ecm, 2, 300);//160 sigma; imglib2 version
//        Substance A = new Substance("A",Color.magenta);
//		ecm.addArtificialGaussianConcentrationZ(A, 1.0, 300.0, 160.0);

        //ChemoAttractant B = ChemoAttractant.createGaussianAttractor(ecm, 0, 300);//160 sigma
        ChemoAttractant B = ChemoAttractant.createGaussianImgAttractor(ecm, 0, 300);//160 sigma
//		Substance B = new Substance("B",Color.magenta);
//		ecm.addArtificialGaussianConcentrationX(B, 1.0, 300.0, 160.0);

        //ChemoAttractant C = ChemoAttractant.createGaussianAttractor(ecm, 1, 300);//160 sigma
        ChemoAttractant C = ChemoAttractant.createGaussianImgAttractor(ecm, 1, 300);//160 sigma
//		Substance C = new Substance("C",Color.magenta);
//		ecm.addArtificialGaussianConcentrationY(C, 1.0, 300.0, 160.0);

		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		}

        Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
        c.setColorForAllPhysicalObjects(Param.GRAY);
        double[] pos = c.getSomaElement().getLocation();

        //double[] initialOutgrowth = new double[]{0, 0, 1};
        double[] initialOutgrowth = randomNoise(1,3);
        double magnitude = 0;
        for( int k = 0 ; k < initialOutgrowth.length; k++ ) magnitude += initialOutgrowth[k] * initialOutgrowth[k];
        magnitude = Math.sqrt(magnitude);
        for( int k = 0 ; k < initialOutgrowth.length; k++ ) initialOutgrowth[k] /= magnitude;

        NeuriteElement neurite = c.getSomaElement().extendNewNeurite(initialOutgrowth);
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

		if( ECM.isSciviewEnabled() )
		    ecm.getSciViewCX3D().getSciView().centerOnNode(ecm.getSciViewCX3D().getCx3dGroup());

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

        if( !ECM.isSciviewEnabled() )
            System.exit(0);

//        tree.merge(realtree);
//        sntService.initialize(true);
//        sntService.getPathAndFillManager().clear();
//        sntService.loadTree(tree);

    }

    public static void main( String... args ) {
        boolean useSciview = true;

        CommandService commandService;
        if( useSciview ) {
            SciView sciView = null;
            try {
                sciView = SciView.createSciView();
            } catch (Exception e) {
                e.printStackTrace();
            }
            commandService = sciView.getScijavaContext().service(CommandService.class);
        } else {
            ImageJ imagej = new ImageJ();
            commandService = imagej.command();
        }

        Map<String, Object> argmap = new HashMap<>();
        argmap.put("filenameSWC", "test_17.swc");
        argmap.put("filenameGRN", "test_17.grn");
        argmap.put("filenameStats", "test_17.csv");
        argmap.put("generateGRN", true);
        argmap.put("sciViewEnabled", useSciview);
        argmap.put("maxTime", 6);
        argmap.put("randomSeed", 917171717);
        //argmap.put("sciView", null);

        commandService.run(GRNeuriteInImg.class,true, argmap);
    }
}
