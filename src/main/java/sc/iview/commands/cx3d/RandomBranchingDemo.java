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
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.util.Colors;
import sc.iview.SciView;
import sc.iview.SciViewService;
import sc.iview.vector.ClearGLVector3;
import sc.iview.vector.Vector3;

import static ini.cx3d.utilities.Matrix.randomNoise;
import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_LINES;

/**
 * A demo of lines.
 *
 * @author Kyle Harrington
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, label = "Random Branching", menuRoot = "SciView", //
        menu = { @Menu(label = "Cx3D", weight = DEMO), //
                 @Menu(label = "Random Branching", weight = DEMO_LINES) })
public class RandomBranchingDemo implements Command {

    @Parameter
    private SciView sciView;

    @Parameter
    private Context context;

    @Override
    public void run() {
        ECM ecm = ECM.getInstance(context);
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000,3));
		}
		ECM.setRandomSeed(7L);
		for(int i = 0; i<1; i++){
			Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
			c.setColorForAllPhysicalObjects(Param.GRAY);
			NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] {0,0,1});
			neurite.getPhysicalCylinder().setDiameter(2);
			neurite.addLocalBiologyModule(new RandomBranchingModule());
		}
		Scheduler.simulate();
    }

    public static void main( String... args ) {
        SceneryBase.xinitThreads();

        System.setProperty( "scijava.log.level:sc.iview", "debug" );
        Context context = new Context( ImageJService.class, SciJavaService.class, SCIFIOService.class, ThreadService.class);

        //UIService ui = context.service( UIService.class );
        //if( !ui.isVisible() ) ui.showUI();

        // Currently Cx3D demos need to make their own SciView instance
//        SciViewService sciViewService = context.service( SciViewService.class );
//        SciView sciView = sciViewService.getOrCreateActiveSciView();

        CommandService commandService = context.service(CommandService.class);
        commandService.run(RandomBranchingDemo.class,true,new Object[]{});
    }
}
