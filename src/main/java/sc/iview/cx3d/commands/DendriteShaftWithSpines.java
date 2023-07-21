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
import org.jgrapht.graph.DefaultDirectedGraph;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.util.Colors;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.Tree;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.PhysicalCylinder;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.simulations.tutorial.RandomBranchingModule;
import sc.iview.cx3d.synapses.*;
import sc.iview.cx3d.utilities.ConvertUtils;

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_BASIC_LINES;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Random Branching", menuRoot = "SciView", //
        menu = { @Menu(label = "Demo", weight = DEMO), //
                 @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Random Branching", weight = DEMO_BASIC_LINES) })
public class DendriteShaftWithSpines implements Command {

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

    @Override
    public void run() {

        ECM ecm = ECM.getInstance();
		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		double[] up = {0.0,0.0,1.0}, down = {0.0,0.0,-1.0};
		Cell inhib = CellFactory.getCellInstance(new double[] {2.5,0,30});
		inhib.setNeuroMLType(Cell.InhibitoryCell);
		inhib.setColorForAllPhysicalObjects(Param.RED);
		// 2) excitatory cell makes an axon, inhibitory cell makes a dendrite

		NeuriteElement dendrite = inhib.getSomaElement().extendNewNeurite(down);
		dendrite.setIsAnAxon(false);
		PhysicalCylinder dendriteCyl = dendrite.getPhysicalCylinder();
		//		elongate them
        for( int k = 0; k < 100; k++ ) {
			dendrite.elongateTerminalEnd(1/Param.SIMULATION_TIME_STEP, down);
			Scheduler.simulateOneStep();
		}
		double[] globalCoord = new double[] {dendrite.getLocation()[2],0,0};

		// 4) a spine on the dendrite:
		// 		create the physical part
		double[] polarDendriteCoord = dendriteCyl.transformCoordinatesGlobalToPolar(globalCoord);
		polarDendriteCoord = new double[] {polarDendriteCoord[0], polarDendriteCoord[1]}; // so r is implicit

		for( NeuriteElement ne : inhib.getNeuriteElements() ) {
			ne.makeSpines(10);
		}

		Scheduler.simulateOneStep();
		Scheduler.simulateOneStep();

		tree = ConvertUtils.cellToTree(inhib);

//		PhysicalSpine pSpine = new PhysicalSpine(dendriteCyl,polarDendriteCoord,3);
//		dendriteCyl.addExcrescence(pSpine);
//		// 		create the biological part and set call backs
//		BiologicalSpine bSpine = new BiologicalSpine();
//		pSpine.setBiologicalSpine(bSpine);
//		bSpine.setPhysicalSpine(pSpine);

    }

	public static void main( String... args ) {
		SciView sciView = null;
		try {
			sciView = SciView.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

		CommandService commandService = sciView.getScijavaContext().service(CommandService.class);
        commandService.run(DendriteShaftWithSpines.class,true,new Object[]{});
    }
}
