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
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.FloatType;
import org.joml.Vector3f;
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
import sc.iview.cx3d.simulations.grn.ChemoAttractant;

import java.awt.*;

import static sc.iview.cx3d.utilities.Matrix.*;

public class FRAChemoAttractionNeurite extends AbstractLocalBiologyModule {


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

	private double branchingFactor = 2;

	public FRAChemoAttractionNeurite(String substanceID) {
		this.substanceID = substanceID;
	}

	public FRAChemoAttractionNeurite(String substanceID, double branchingFactor) {
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
		return new FRAChemoAttractionNeurite(substanceID);
	}

	public void run() {		
		PhysicalObject physical = super.cellElement.getPhysical();
		double concentration = physical.getExtracellularConcentration(substanceID);
		double[] grad = physical.getExtracellularGradient(substanceID);

		double[] pos = physical.getMassLocation();

		// 1) movement
		double oldDirectionWeight = 0.1;
		double gradientWeight = 2.5;
		double randomnessWeight = 0.2;

		if(physical.getExtracellularConcentration(substanceID)>0.3)
			grad = new double[] {0.0, 0.0, 0.0};

		double[] newStepDirection = add(
				scalarMult(oldDirectionWeight, direction),
				scalarMult(gradientWeight, normalize(grad)),
				randomNoise(randomnessWeight,3));

		double speed = 10;
		//newStepDirection[1] = -pos[1]

		// Bounding positions
		//newStepDirection[2] = 0;// no z motion to stay in plane
        // make z motion very small
        //newStepDirection[2] *= 0.0001;

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
//		if( nextConc < 0.0001 ) {
//		    newStepDirection = randomNoise(randomnessWeight,3);
//		    physical.movePointMass(0.01 * speed, newStepDirection);
//        }
        physical.movePointMass(speed, newStepDirection);

		direction = normalize(add(scalarMult(5,direction),newStepDirection));

		//if( ecm.getRandomDouble() < 0.01 ) System.out.println(concentration*branchingFactor + " " + concentration);

		// 2) branching based on concentration:
		if(ecm.getRandomDouble()< concentration*branchingFactor){
			try {
				if( ECM.getInstance().getECMtime() < 1 )
					((NeuriteElement)cellElement).bifurcate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static final FunctionRandomAccessible<FloatType> gaussianConcentration(
			final int dimension,
			final double min,
			final double max,
			final double scale,
			final double stretch) {

		return new FunctionRandomAccessible<FloatType>(
				3,
				(location, value) -> {
					final double v = location.getDoublePosition(dimension);
					value.set(
							(float) (scale / (stretch * Math.abs(v - min) + 1) +
									 scale / (stretch * Math.abs(v - max) + 1)));
				},
				FloatType::new);
	}

	public static void main(String[] args) throws Exception {
		ECM ecm = ECM.getInstance();
		ECM.setRandomSeed(0L);		
		Substance attractant = new Substance("A",Color.red);

		long[] max = new long[]{500, 500, 500};
		//long[] max = new long[]{1000, 1000, 1000};
		long[] min = new long[]{0, 0, 0};
		//long[] min = new long[]{-500, -500, -500};

        ChemoAttractant A = ChemoAttractant.createGaussianImgAttractor(ecm, 2, 300, true);//160 sigma; imglib2 version
        FRAChemoAttractionNeurite imgModule = new FRAChemoAttractionNeurite("A");
		FRAChemoAttractionNeurite.setConcentrationImg(A.getConcentrationImg());
		FRAChemoAttractionNeurite.setInterval(A.getInterval());

		//ecm.addArtificialGaussianConcentrationZ(attractant, 1.0, 400.0, 160.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}
		
		Cell c = CellFactory.getCellInstance(new double[] {40.0,50.0,0.0});
		c.setColorForAllPhysicalObjects(Param.VIOLET);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
		neurite.getPhysicalCylinder().setDiameter(2.0);
		neurite.addLocalBiologyModule(imgModule);

		Camera camera = ecm.getSciViewCX3D().getSciView().getCamera();
		camera.setPosition(new Vector3f(0,0,-10));
		camera.setNeedsUpdate(true);
		camera.setDirty(true);

		ecm.getSciViewCX3D().getSciView().getFloor().setVisible(false);

		System.out.println("Starting simulation");
		Scheduler.simulate();
	}

	private static void setInterval(Interval translate) {
		staticInterval = translate;
	}

	private static void setConcentrationImg(RandomAccessible<FloatType> concentrationImg) {
		staticConcentrationImg = concentrationImg;
	}
}
