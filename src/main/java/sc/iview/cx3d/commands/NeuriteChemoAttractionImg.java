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
import net.imagej.ImageJService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.io.IOService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.util.Colors;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.fiji.snt.analysis.graph.DirectedWeightedGraph;
import sc.fiji.snt.analysis.graph.GraphUtils;
import sc.fiji.snt.viewer.Viewer3D;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.utilities.ConvertUtils;

import java.awt.*;
import java.io.IOException;

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_LINES;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Random Branching", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Neurite Chemoattraction Img", weight = DEMO_LINES) })
public class NeuriteChemoAttractionImg implements Command {

    @Parameter
    private SciView sciView;

    @Parameter
    private Context context;

    @Parameter
    private SNTService sntService;

    @Parameter(type = ItemIO.OUTPUT)
    private Tree tree;

    @Parameter(label = "Simulation end time")
    private float maxTime = 5;

    @Parameter(label = "Environment img")
    private RandomAccessibleInterval<RealType> img;

    public static void main( String... args ) {
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class);

        //UIService ui = context.service( UIService.class );
        //if( !ui.isVisible() ) ui.showUI();

        IOService io = context.service(IOService.class);
		Img<FloatType> img = null;
		try {
			img = (Img<FloatType>) io.open("/home/kharrington/git/cx3d-mvn/fourdots.tif");
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.service(UIService.class).show(img);


        // Currently Cx3D demos need to make their own SciView instance
        SciViewService sciViewService = context.service( SciViewService.class );
        try {
            SciView sciView = sciViewService.getOrCreateActiveSciView();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        CommandService commandService = context.service(CommandService.class);
//        commandService.run(NeuriteChemoAttractionImg.class,true,new Object[]{});
    }

    @Override
    public void run() {
        ECM ecm = ECM.getInstance(context);
		ECM.setRandomSeed(0L);
		Substance attractant = new Substance("A", Color.red);

		IOService io = ecm.getSciViewCX3D().getContext().service(IOService.class);

		System.out.println("Adding Img Concentration");
        ecm.addArtificialImgConcentration(attractant.getId(), img);

		//ecm.addArtificialGaussianConcentrationZ(attractant, 1.0, 400.0, 160.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		Cell c = CellFactory.getCellInstance(new double[] {0.0,0.0,0.0});
		c.setColorForAllPhysicalObjects(Param.VIOLET);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
		neurite.getPhysicalCylinder().setDiameter(2.0);
		neurite.addLocalBiologyModule(new ImgNeuriteChemoAttraction("A"));

		System.out.println("Starting simulation");
		Scheduler.simulate();

        // This should work for Cx3D trees
        Tree tree = ConvertUtils.cellToTree(c);
        tree.setColor(Colors.RED);
        sntService.initialize(true);
        sntService.loadTree(tree);

        Viewer3D recViewer = new Viewer3D(context);
        recViewer.add(tree);
        recViewer.show();

    }
}
