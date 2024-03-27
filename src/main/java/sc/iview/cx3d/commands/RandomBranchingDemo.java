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

import static sc.iview.commands.MenuWeights.DEMO;
import static sc.iview.commands.MenuWeights.DEMO_BASIC_LINES;
import static sc.iview.cx3d.utilities.Matrix.randomNoise;

import io.scif.SCIFIOService;
import net.imagej.ImageJService;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;
import org.scijava.util.Colors;

import graphics.scenery.SceneryBase;
import net.imagej.ImageJ;
import sc.fiji.snt.SNTService;
import sc.fiji.snt.SciViewSNT;
import sc.fiji.snt.Tree;
import sc.iview.SciView;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;
import sc.iview.cx3d.simulations.tutorial.RandomBranchingModule;
import sc.iview.cx3d.utilities.ConvertUtils;

/**
 * Random branching demo from Cx3D
 *
 * @author Kyle Harrington
 */
@Plugin(type = Command.class, label = "Random Branching", menuRoot = "SciView", //
		menu = { @Menu(label = "Demo", weight = DEMO), //
				@Menu(label = "Cx3D", weight = DEMO), //
				@Menu(label = " Random Branching", weight = DEMO_BASIC_LINES) })
public class RandomBranchingDemo implements Command {

	@Parameter
	private SciView sciView;

	@Parameter
	private Context context;

	@Parameter
	private SNTService sntService;

	@Parameter(label = "Simulation end time")
	private float maxTime = 2;

	@Parameter(label = "Growth speed")
	private float speed = 100;

	@Parameter(label = "Probability to bifurcate")
	private double probabilityToBifurcate = 0.005; // o.oo5

	@Parameter(label = "Probability to branch")
	private double probabilityToBranch = 0.005;

	@Override
	public void run() {
		// ECM ecm = ECM.getInstance(getContext());
		ECM ecm = ECM.getInstance(context);

		ECM.setRandomSeed(7L);

		ecm.clearAll();
		ecm.resetTime();
		ecm.getSciViewCX3D().clear();
		for (int i = 0; i < 18; i++) {
			ecm.getPhysicalNodeInstance(randomNoise(1000, 3));
		}

		Cell c = CellFactory.getCellInstance(randomNoise(40, 3));
		c.setColorForAllPhysicalObjects(Param.GREEN);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite(new double[] { 0, 0, 1 });
		neurite.getPhysicalCylinder().setDiameter(2);

		RandomBranchingModule branchingModule = new RandomBranchingModule();
		branchingModule.setSpeed(speed);
		branchingModule.setProbabilityToBranch(probabilityToBranch);
		branchingModule.setProbabilityToBifurcate(probabilityToBifurcate);
		neurite.addLocalBiologyModule(branchingModule);

		Scheduler.simulate(maxTime);

		sciView.centerOnNode(ecm.getSciViewCX3D().getCx3dGroup());

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Tree realtree = ConvertUtils.cellToTree(c);
		realtree.setLabel("Cx3D_Tree");
		realtree.setColor(Colors.RED);

		SciViewSNT sciviewSNT = sntService.getOrCreateSciViewSNT();
		if (sciView != null) {
			sciviewSNT.setSciView(sciView);
			sciView.waitForSceneInitialisation();
		}
		//realtree.setLabel("Cx3D_Tree (SNT)");
		sciviewSNT.addTree(realtree);
	}

	public static void main(String... args) {
		SceneryBase.xinitThreads();
		System.setProperty( "scijava.log.level:sc.iview", "debug" );
		Context context = new Context(ImageJService.class, SciJavaService.class, SCIFIOService.class, SNTService.class);
		final ImageJ ij = new ImageJ(context);
		ij.ui().showUI();
		final CommandService cmdService = ij.context().getService(CommandService.class);
		cmdService.run(RandomBranchingDemo.class, true);
	}
}
