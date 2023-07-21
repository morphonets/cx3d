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

import graphics.scenery.SceneryBase;
import io.scif.SCIFIOService;
import net.imagej.ImageJ;
import net.imagej.ImageJService;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.util.Colors;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.TreeAnalyzer;
import sc.fiji.snt.analysis.TreeStatistics;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.simulations.tutorial.RandomBranchingModule;
import sc.iview.cx3d.utilities.ConvertUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static sc.iview.commands.MenuWeights.*;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Random Branching (SWC output)", visible = false,
		menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Random Branching (SWC output)", weight = DEMO_BASIC_LINES) })
public class RandomBranchingSWC implements Command {

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

    @Parameter(persist=false)
    private String filename;

    @Parameter(label = "Simulation end time", persist = false)
    private float maxTime = 2;

    @Parameter(label = "Growth speed", persist = false)
    private float speed = 100;

	@Parameter(label = "Probability to bifurcate", persist = false)
	private double probabilityToBifurcate = 0.005; // o.oo5

    @Parameter(label = "Probability to branch", persist = false)
	private double probabilityToBranch = 0.005;

    @Parameter(label = "Random seed", persist = false)
    private long randomSeed = 17L;

    @Parameter
    private String statFile;

    @Override
    public void run() {
        String outline = "";

        System.out.println("Running with:");
        System.out.println("Random seed = " + randomSeed);
        System.out.println("filename = " + filename);
        System.out.println("maxTime = " + maxTime);
        System.out.println("speed = " + speed);
        System.out.println("probabilityToBifurcate = " + probabilityToBifurcate);
        System.out.println("probabilityToBranch = " + probabilityToBranch);

        outline += randomSeed + "\t" + maxTime + "\t" + speed + "\t" + probabilityToBifurcate + "\t" + probabilityToBranch + "\t";

        //ECM ecm = ECM.getInstance(getContext());
        ECM ecm = ECM.getInstance(context);

        ECM.setRandomSeed(randomSeed);

        ecm.clearAll();
        ecm.resetTime();
        ecm.getSciViewCX3D().clear();
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		}

        Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
        c.setColorForAllPhysicalObjects(Param.GRAY);
        double[] pos = c.getSomaElement().getLocation();
        NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
        neurite.getPhysicalCylinder().setDiameter(2);

        RandomBranchingModule branchingModule = new RandomBranchingModule();
        branchingModule.setSpeed(speed);
        branchingModule.setProbabilityToBranch(probabilityToBranch);
        branchingModule.setProbabilityToBifurcate(probabilityToBifurcate);
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

        // This should work for Cx3D trees
        Tree realtree = ConvertUtils.cellToTree(c);
        realtree.setLabel("Cx3D_Tree");
        realtree.setColor(Colors.RED);

       TreeAnalyzer ta = new TreeAnalyzer(realtree);

        // TODO fix measurements
        List<String> metrics = TreeStatistics.getMetrics();
        List<Number> measurements = new ArrayList<>();
        outline = "";
        for( String metric : metrics ) {
            measurements.add(ta.getMetric(metric));
            outline += ta.getMetric(metric) + "\t";
        }
        outline = outline.substring(0,outline.length()-1);

        System.out.println("tree created");

        realtree.saveAsSWC(filename);

        System.out.println("SWC saved to " + filename);

        boolean statFileExists = new File(statFile).exists();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(statFile,true));

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
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class);

        UIService ui = context.service( UIService.class );
        if( !ui.isVisible() ) ui.showUI();

        // Currently Cx3D demos need to make their own SciView instance
        SciViewService sciViewService = context.service( SciViewService.class );
        try {
            SciView sciView = sciViewService.getOrCreateActiveSciView();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        CommandService commandService = context.service(CommandService.class);
//        commandService.run(RandomBranchingDemo.class,true,new Object[]{});
    }
}
