/*
Copyright (C) 2009 Frédéric Zubler, Rodney J. Douglas,
Dennis Göhlsdorf, Toby Weston, Andreas Hauri, Roman Bauer,
Sabina Pfister & Adrian M. Whatley.

This file is part of CX3D.

CX3D is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

CX3D is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with CX3D.  If not, see <http://www.gnu.org/licenses/>.
*/

package sc.iview.cx3d.commands;

import cleargl.GLVector;
import graphics.scenery.Camera;
import graphics.scenery.Node;
import graphics.scenery.volumes.bdv.Volume;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;
import org.scijava.io.IOService;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.localBiology.AbstractLocalBiologyModule;
import sc.iview.cx3d.localBiology.CellElement;
import sc.iview.cx3d.localBiology.NeuriteElement;
import sc.iview.cx3d.physics.PhysicalObject;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

import static sc.iview.cx3d.utilities.Matrix.*;

public class ImgNeuriteChemoAttraction extends AbstractLocalBiologyModule {


	static ECM ecm;
	private static RandomAccessible<FloatType> staticConcentrationImg = null;
	private static Interval staticInterval;

	static {
		try {
			ecm = ECM.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private double[] direction;

	private String substanceID;

	private double branchingFactor = 0.005;

	public ImgNeuriteChemoAttraction(String substanceID) {
		this.substanceID = substanceID;
	}

	public ImgNeuriteChemoAttraction(String substanceID, double branchingFactor) {
		this.substanceID = substanceID;
		this.branchingFactor = branchingFactor;
	}

	@Override
	public void setCellElement(CellElement cellElement) {
		super.setCellElement(cellElement);
		if(cellElement.isANeuriteElement())
			direction = cellElement.getPhysical().getAxis();
	}

	
	@Override
	public boolean isCopiedWhenNeuriteBranches() {
		return true;
	}
	
	@Override
	public boolean isDeletedAfterNeuriteHasBifurcated() {
		return true;
	}
	
	public AbstractLocalBiologyModule getCopy() {
		return new ImgNeuriteChemoAttraction(substanceID);
	}

	public void run() {		
		PhysicalObject physical = super.cellElement.getPhysical();
		double concentration = physical.getExtracellularConcentration(substanceID);
		double[] grad = physical.getExtracellularGradient(substanceID);

		double[] pos = physical.getMassLocation();

		// 1) movement
		double oldDirectionWeight = 1.0;
		double gradientWeight = 0.2;
		double randomnessWeight = 0.6;

		if(physical.getExtracellularConcentration(substanceID)>0.3)
			grad = new double[] {0.0, 0.0, 0.0};

		double[] newStepDirection = add(
				scalarMult(oldDirectionWeight, direction),
				scalarMult(gradientWeight, normalize(grad)),
				randomNoise(randomnessWeight,3));

		double speed = 10;
		//newStepDirection[1] = -pos[1]

		// Bounding positions
		newStepDirection[2] = 0;// no z motion to stay in plane

		long[] nextPos = new long[3];

		double length = speed*Param.SIMULATION_TIME_STEP;
		for( int d = 0; d < 3; d++ ) {
			if( pos[d] + newStepDirection[d] * length > staticInterval.max(d) )
				newStepDirection[d] = ( staticInterval.max(d) - pos[d] ) / ( newStepDirection[d] * length );
			if( pos[d] + newStepDirection[d] * length < staticInterval.min(d) )
				newStepDirection[d] = ( staticInterval.min(d) + pos[d] ) / ( newStepDirection[d] * length );
			nextPos[d] = (long) (pos[d] + newStepDirection[d] * length);
		}

		RandomAccess<FloatType> cra = staticConcentrationImg.randomAccess();
		cra.setPosition(nextPos);

		float nextConc = cra.get().get();
		//System.out.println("Next conc: " + nextConc + " pos: " + Arrays.toString(nextPos));

		// Dont move into low conc
		if( nextConc < 0.0001 )
			physical.movePointMass(speed * 0.0001, newStepDirection);
		else
			physical.movePointMass(speed, newStepDirection);

		direction = normalize(add(scalarMult(5,direction),newStepDirection));

		// 2) branching based on concentration:
		if(ecm.getRandomDouble()<concentration*branchingFactor){
			try {
				if( ECM.getInstance().getECMtime() < 1 )
					((NeuriteElement)cellElement).bifurcate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(0L);		
		Substance attractant = new Substance("A",Color.red);

//		OpService ops = ecm.getSciViewCX3D().getContext().service(OpService.class);
//		String formula = "255 - 63 * (Math.cos(0.03*p[0]) + Math.sin(0.03*p[1]) + Math.cos(0.3*p[2])) + 127";
//		Img<FloatType> img = (Img<FloatType>) ops.run("create.img", new long[]{width, height, depth}, new FloatType());
//        ops.image().equation(img, formula);

		IOService io = ecm.getSciViewCX3D().getContext().service(IOService.class);
		RandomAccessibleInterval<FloatType> img = null;
		try {
			img = (Img<FloatType>) io.open("/home/kharrington/git/morphonets/cx3d/KothapalliEtAl2011_Fig5a_attractantMapMasked_stack.tif");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//img = (RandomAccessibleInterval<FloatType>)(Object)Views.translate(img, new long[]{(long) (img.dimension(0) * -0.5), (long) (img.dimension(1) * -0.5), (long) (img.dimension(2) * -0.5)});

		RandomAccessibleInterval<UnsignedByteType> volImg = Converters.convert(img, (a, b) -> b.set((int)(255 * a.getRealDouble())), new UnsignedByteType());

		System.out.println("Volume is : " + volImg.dimension(0) + " " + volImg.dimension(1) + " " + volImg.dimension(2));

		Volume vol = (Volume)ecm.getSciViewCX3D().getSciView().addVolume(volImg, "circuit", new float[]{1, 10, 1});
		double transformScale = 0.1;

		vol.setScale(new GLVector((float)transformScale, (float)transformScale, (float)transformScale).times(2));

		vol.updateWorld(true, true);

		long[] offset = new long[]{(long) (img.dimension(0) * -0.5), (long) (img.dimension(1) * -0.5), (long) (img.dimension(2) * -0.5)};

		//final Scale3D transformScale3D = new Scale3D(transformScale, transformScale, transformScale);
		final Scale3D transformScale3D = new Scale3D(1, 1, 1);
		RandomAccessible<FloatType> concentrationImg =
				RealViews.affine(
						Views.interpolate(
								Views.extendZero(Views.translate(img, offset)),
								new NLinearInterpolatorFactory<>()),
						transformScale3D);

		ImgNeuriteChemoAttraction imgModule = new ImgNeuriteChemoAttraction("A");

		ImgNeuriteChemoAttraction.setConcentrationImg(concentrationImg);
		ImgNeuriteChemoAttraction.setInterval(Views.translate(img, offset));

		System.out.println("Adding Img Concentration");
        ecm.addArtificialImgConcentration( attractant.getId(), concentrationImg);

		//ecm.addArtificialGaussianConcentrationZ(attractant, 1.0, 400.0, 160.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}
		
		Cell c = CellFactory.getCellInstance(new double[] {0.0,50.0,0.0});
		c.setColorForAllPhysicalObjects(Param.VIOLET);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
		neurite.getPhysicalCylinder().setDiameter(2.0);
		neurite.addLocalBiologyModule(imgModule);

		Camera camera = ecm.getSciViewCX3D().getSciView().getCamera();
		camera.setPosition(new GLVector(0,0,-10));
		camera.setNeedsUpdate(true);
		camera.setDirty(true);

		ecm.getSciViewCX3D().getSciView().getFloor().setVisible(false);

		System.out.println("Starting simulation");
		Scheduler.simulate();
	}

	private static void setInterval(IntervalView<FloatType> translate) {
		staticInterval = translate;
	}

	private static void setConcentrationImg(RandomAccessible<FloatType> concentrationImg) {
		staticConcentrationImg = concentrationImg;
	}
}
