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

package sc.iview.cx3d.simulations.tutorial;

import fun.grn.grneat.evaluators.GRNGenomeEvaluator;
import fun.grn.grneat.evolver.GRNGene;
import fun.grn.grneat.evolver.GRNGenome;
import fun.grn.grneat.grn.GRNModel;
import fun.grn.grneat.grn.GRNProtein;
import graphics.scenery.SceneryBase;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.real.FloatType;
import sc.iview.cx3d.Param;
import sc.iview.cx3d.cells.Cell;
import sc.iview.cx3d.cells.CellFactory;
import sc.iview.cx3d.cells.GRNModule;
import sc.iview.cx3d.localBiology.*;
import sc.iview.cx3d.physics.PhysicalCylinder;
import sc.iview.cx3d.physics.PhysicalObject;
import sc.iview.cx3d.physics.Substance;
import sc.iview.cx3d.simulations.ECM;
import sc.iview.cx3d.simulations.Scheduler;

import java.awt.*;
import java.util.Random;
import java.util.Vector;
import java.util.function.Predicate;

import static sc.iview.cx3d.utilities.Matrix.*;

public class ActiveNeuriteChemoAttraction extends AbstractLocalBiologyModule {

	static ECM ecm = ECM.getInstance();

	private double[] direction;

	private String[] substanceID =
			new String[] {
				"A",
                "B",
                "C",
			};;

	public GRNModule getGrnModule() {
		return grnModule;
	}

	private GRNModule grnModule;

	//private double branchingFactor = 0.005;
	//private double branchingFactor = 0.5;
	private String filenameGRN;

	public ActiveNeuriteChemoAttraction() {
	}

	public ActiveNeuriteChemoAttraction(String filenameGRN) {
		this.filenameGRN = filenameGRN;

		grnModule = new GRNModule();
		GRNElement grnElement = new GRNElement(filenameGRN, true);


		grnModule.setGrn(grnElement);
	}

	public ActiveNeuriteChemoAttraction(GRNModule grnModule) {
		this.grnModule = grnModule;
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
		return new ActiveNeuriteChemoAttraction(grnModule);
	}

	public GRNElement getGrn(Cell cell) {
		Vector<LocalBiologyModule> localBiologyList = cell.getSomaElement().getLocalBiologyModulesList();
		for( int k = 0; k < localBiologyList.size(); k++ ) {
			if( localBiologyList.get(k) instanceof GRNModule ) {
				return ((GRNModule) localBiologyList.get(k)).getGrn();
			}
		}
		return null;
	}

	/**
	 * Write a randomly generated GRN to the specific file
	 * TODO consider a predicate test/requirement for the GRN
	 * @param filename
	 */
	public static void writeRandomGRNToFile(String filename) throws Exception {
		int numGRNInputs = 4;
        int numGRNOutputs = 4;
        int maxGRNNodes = 50;
        Random rng = new Random();

        GRNGenome genome = new GRNGenome();
        // Make inputs
        for(int k = 0; k < numGRNInputs; k++) {
            genome.addGene(GRNGene.generateRandomGene(GRNProtein.INPUT_PROTEIN, k, rng));
        }
        // Make outputs
        for(int k = 0; k < numGRNOutputs; k++) {
            genome.addGene(GRNGene.generateRandomGene(GRNProtein.OUTPUT_PROTEIN, k, rng));
        }
        // Make hidden
        for(int k = 0; k < rng.nextInt( maxGRNNodes - numGRNInputs - numGRNOutputs ); k++) {
            genome.addGene(GRNGene.generateRandomRegulatoryGene(rng));
        }
        // Set GRN params
        genome.setBeta(genome.getBetaMin() + rng.nextDouble()*(genome.getBetaMax() - genome.getBetaMin()));
        genome.setDelta(genome.getDeltaMin() + rng.nextDouble()*(genome.getDeltaMax() - genome.getDeltaMin()));

		genome.writeToFile(filename);
	}

	/**
	 * Write a randomly generated GRN to the specific file
	 * TODO consider a predicate test/requirement for the GRN
	 * @param filename
	 * @param predicate determines whether a GRN is acceptable
	 */
	public static void writePredicateFilteredRandomGRNToFile(String filename, Predicate<GRNGenome> predicate, long randomSeed) throws Exception {
        Random rng = new Random(randomSeed);

        GRNGenome genome = randomGRN(rng);

        int numAttempts = 0;

        while( !predicate.test(genome) ){
        	if( numAttempts > 5000 ) {
        		throw new Exception("Too many attempts in writePredicateFilteredRandomGRNToFile");
			}

        	genome = randomGRN(rng);
        	numAttempts++;
		}

		genome.writeToFile(filename);
	}

	public static int numGRNInputs = 6;
	public static int numGRNOutputs = 6;
	public static int maxGRNNodes = 50;

	public static GRNGenome randomGRN(Random rng) {
		GRNGenome genome = new GRNGenome();
        // Make inputs
        for(int k = 0; k < numGRNInputs; k++) {
            genome.addGene(GRNGene.generateRandomGene(GRNProtein.INPUT_PROTEIN, k, rng));
        }
        // Make outputs
        for(int k = 0; k < numGRNOutputs; k++) {
            genome.addGene(GRNGene.generateRandomGene(GRNProtein.OUTPUT_PROTEIN, k, rng));
        }
        // Make hidden
        for(int k = 0; k < rng.nextInt( maxGRNNodes - numGRNInputs - numGRNOutputs ); k++) {
            genome.addGene(GRNGene.generateRandomRegulatoryGene(rng));
        }
        // Set GRN params
        genome.setBeta(genome.getBetaMin() + rng.nextDouble()*(genome.getBetaMax() - genome.getBetaMin()));
        genome.setDelta(genome.getDeltaMin() + rng.nextDouble()*(genome.getDeltaMax() - genome.getDeltaMin()));
        return genome;
	}

	public void run() {
		PhysicalObject physical = super.cellElement.getPhysical();
		Cell cell = super.cellElement.getCell();
		GRNElement grn = getGrn(cell);

		//System.out.println("Physical: " + physical + " Cell: " + cell + " GRN: " + grn);

		double[] pos = physical.getMassLocation();

		// Get sensor inputs:
		double A = physical.getExtracellularConcentration("A");
		double[] AGrad = physical.getExtracellularGradient("A");

		double B = physical.getExtracellularConcentration("B");
		double[] BGrad = physical.getExtracellularGradient("B");

		double C = physical.getExtracellularConcentration("C");
		double[] CGrad = physical.getExtracellularGradient("C");

		// If this is a soma element then setup inputs and evaluate GRN (to ensure one time evaluation)

		float branchOrder = 0;
		if( physical instanceof PhysicalCylinder ) {
			branchOrder = ((PhysicalCylinder) physical).getBranchOrder();
			branchOrder = branchOrder / 10;
		}

		// Set GRN inputs
		grn.state.proteins.get(0).setConcentration(A);
		grn.state.proteins.get(1).setConcentration(B);
		grn.state.proteins.get(2).setConcentration(C);
		grn.state.proteins.get(3).setConcentration(physical.getLength()/10f);
		grn.state.proteins.get(4).setConcentration(physical.getVolume()/10f);
		grn.state.proteins.get(5).setConcentration(branchOrder);
		//physicalCylinder.getBranchOrder()
		//((NeuriteElement) cellElement).

		// Update GRN
		grn.state.evolve(2);

		// Extract weights and probability of bifurcation
		//double oldDirectionWeight = grn.state.proteins.get(grn.state.proteins.size() - 1).getConcentration();
        double oldDirectionWeight = 1;
		double randomnessWeight = grn.state.proteins.get(grn.state.proteins.size() - 1).getConcentration();
		//double randomnessWeight = grn.state.proteins.get(grn.state.proteins.size() - 2).getConcentration();
        //double randomnessWeight = 0.25;
		double AWeight = grn.state.proteins.get(grn.state.proteins.size() - 2).getConcentration();
		double BWeight = grn.state.proteins.get(grn.state.proteins.size() - 3).getConcentration();
		double CWeight = grn.state.proteins.get(grn.state.proteins.size() - 4).getConcentration();

		double bifurcationWeight = grn.state.proteins.get(grn.state.proteins.size() - 5).getConcentration();
		double branchingFactor = grn.state.proteins.get(grn.state.proteins.size() - 6).getConcentration();

		bifurcationWeight *= 0.005;
		branchingFactor *= 0.005;
		bifurcationWeight = Math.max( 0.0004, Math.min(bifurcationWeight, 0.006) );
		branchingFactor = Math.max( 0.0004, Math.min(branchingFactor, 0.006) );

		double[] newStepDirection = add(
				scalarMult(oldDirectionWeight, direction),
				scalarMult(AWeight, normalize(AGrad)),
				scalarMult(BWeight, normalize(BGrad)),
				scalarMult(CWeight, normalize(CGrad)),
				randomNoise(randomnessWeight,3));
		double speed = 10;

		newStepDirection[2] *= 0.0001;

		long[] nextPos = new long[3];

		double length = speed*Param.SIMULATION_TIME_STEP;
		for( int d = 0; d < 3; d++ ) {
			if( pos[d] + newStepDirection[d] * length > ECM.staticInterval.max(d) )
				newStepDirection[d] = ( ECM.staticInterval.max(d) - pos[d] ) / ( newStepDirection[d] * length );
			if( pos[d] + newStepDirection[d] * length < ECM.staticInterval.min(d) )
				newStepDirection[d] = ( ECM.staticInterval.min(d) + pos[d] ) / ( newStepDirection[d] * length );
			nextPos[d] = (long) (pos[d] + newStepDirection[d] * length);
		}

//		RandomAccess<FloatType> cra = staticConcentrationImg.randomAccess();
//		cra.setPosition(nextPos);
//
//		float nextConc = cra.get().get();
//		//System.out.println("Next conc: " + nextConc + " pos: " + Arrays.toString(nextPos));
//
//		// Dont move into low conc
//		if( nextConc < 0.0001 ) {
//		    newStepDirection = randomNoise(randomnessWeight,3);
//		    physical.movePointMass(0.01 * speed, newStepDirection);
//        } else {
//            physical.movePointMass(speed, newStepDirection);
//        }

		// 1) movement
		physical.movePointMass(speed, newStepDirection);
		direction = normalize(add(scalarMult(1,direction),newStepDirection));

		if(super.cellElement.isANeuriteElement() && ( ecm.getRandomDouble() < bifurcationWeight ) ){
		//if(ecm.getRandomDouble()<branchingFactor){
			NeuriteElement[] newBranch = ((NeuriteElement) cellElement).bifurcate();
			//System.out.println("Cell " + cell.getID() + " is bifurcating at " + newBranch[0].getLocation()[0] + ", " + newBranch[0].getLocation()[1] + ", " + newBranch[0].getLocation()[2] );
		}

		if(super.cellElement.isANeuriteElement() && ( ecm.getRandomDouble() < branchingFactor ) ){
		//if(ecm.getRandomDouble()<branchingFactor){
			NeuriteElement newBranch = ((NeuriteElement) cellElement).branch();
			//System.out.println("Cell " + cell.getID() + " is branching at " + newBranch.getLocation()[0] + ", " + newBranch.getLocation()[1] + ", " + newBranch.getLocation()[2] );
		}

	}

	public static Predicate grnPredicate = (Predicate<GRNGenome>) genome -> {
        double A = 0.01;
        double B = 0.01;
        double C = 0.01;
        double length = 0.2;
        double volume = 0.3;
        double branchOrder = 0.5;

        // TODO run the GRN for some number of steps, test that the outputs are dynamic
        GRNModel state = GRNGenomeEvaluator.buildGRNFromGenome(genome);


        state.proteins.get(0).setConcentration(A);
		state.proteins.get(1).setConcentration(B);
		state.proteins.get(2).setConcentration(C);
		state.proteins.get(3).setConcentration(length);
		state.proteins.get(4).setConcentration(volume);
		state.proteins.get(5).setConcentration(branchOrder);
		//physicalCylinder.getBranchOrder()
		//((NeuriteElement) cellElement).


        // warmpup
        state.evolve(25);

        int numTestSteps = 100;
        int numOutputs = ActiveNeuriteChemoAttraction.numGRNOutputs;
        double[][] outputs = new double[numOutputs][numTestSteps];
        for( int t = 0; t < numTestSteps; t++ ) {
            // Update GRN
            state.evolve(2);

            // Extract weights and probability of bifurcation
            double oldDirectionWeight = state.proteins.get(state.proteins.size() - 1).getConcentration();
            double AWeight = state.proteins.get(state.proteins.size() - 2).getConcentration();
            double BWeight = state.proteins.get(state.proteins.size() - 3).getConcentration();
            double CWeight = state.proteins.get(state.proteins.size() - 4).getConcentration();

            double bifurcationWeight = state.proteins.get(state.proteins.size() - 5).getConcentration();
            double branchingFactor = state.proteins.get(state.proteins.size() - 6).getConcentration();

            bifurcationWeight *= 0.005;
            branchingFactor *= 0.005;

            bifurcationWeight = Math.max( 0.004, Math.min(bifurcationWeight, 0.006) );
            branchingFactor = Math.max( 0.004, Math.min(branchingFactor, 0.006) );

            outputs[0][t] = oldDirectionWeight;
            outputs[1][t] = AWeight;
            outputs[2][t] = BWeight;
            outputs[3][t] = CWeight;
            outputs[4][t] = bifurcationWeight;
            outputs[5][t] = branchingFactor;
        }

        boolean debugPredicate = false;

        int numStationary = 0;
        // Test if the output changed from beginning to end
        for( int oid = 0; oid < numOutputs; oid++ ) {
            if (Math.abs(outputs[oid][0] - outputs[oid][numTestSteps - 1]) < 0.0001) {
                if( debugPredicate ) System.out.println("Nondynamic GRN " + oid + " " + outputs[oid][0] + " " + outputs[oid][numTestSteps - 1]);
                numStationary++;
            }
        }

        // Some outputs can be stationary/constant
        if( numStationary > numOutputs - 3 )
            return false;

        // Check that this will branch
        int branchIdx = 4;
        if( outputs[branchIdx][0] < 0.004 && outputs[branchIdx][numTestSteps-1] < 0.004 ) {
            if( debugPredicate ) System.out.println("Nonbranching GRN");
            return false;
        }
        // Check that this will not over branch
        if( outputs[branchIdx][0] > 0.006 && outputs[branchIdx][numTestSteps-1] > 0.006 ) {
            if( debugPredicate ) System.out.println("Overbranching GRN");
            return false;
        }

        // Check that this will bifurcate
        int bifurcateIdx = 5;
        if( outputs[bifurcateIdx][0] < 0.004 && outputs[bifurcateIdx][numTestSteps-1] < 0.004 ) {
            if( debugPredicate ) System.out.println("Nonbifurcating GRN");
            return false;
        }
        // Check that this will not over bifurcate
        if( outputs[bifurcateIdx][0] > 0.006 && outputs[bifurcateIdx][numTestSteps-1] > 0.006 ) {
            if( debugPredicate ) System.out.println("Overbifurcating GRN");
            return false;
        }

        return true;
    };

	public static void main(String[] args) {
		SceneryBase.xinitThreads();

		/*
		 * This should be its own evaluate() function
		 *
		 * Evaluate a GRNGenome by creating and evaluating a Cell for a certain amount of time
		 * Takes GRNGenome
		 * Returns a Cell with morphology
		 */
    	GRNElement grnElement = new GRNElement("ActiveNeuriteType2", false);
		double maxTime = 2;
		long randomSeed = 17L;

        // Configure Cell
        ECM ecm = ECM.getInstance();

		ECM.setRandomSeed(randomSeed);
		Substance A = new Substance("A",Color.magenta);
		ecm.addArtificialGaussianConcentrationZ(A, 1.0, 400.0, 160.0);

		Substance B = new Substance("B",Color.magenta);
		ecm.addArtificialGaussianConcentrationX(B, 1.0, 400.0, 160.0);

		Substance C = new Substance("C",Color.magenta);
		ecm.addArtificialGaussianConcentrationY(C, 1.0, 400.0, 160.0);

		int nbOfAdditionalNodes = 10;
		for (int i = 0; i < nbOfAdditionalNodes; i++) {
			double[] coord = randomNoise(500, 3);
			ecm.getPhysicalNodeInstance(coord);
		}

		Cell c = CellFactory.getCellInstance(new double[] {0.0,0.0,0.0});
		c.setColorForAllPhysicalObjects(Param.VIOLET);
		NeuriteElement neurite = c.getSomaElement().extendNewNeurite();
		neurite.getPhysicalCylinder().setDiameter(2.0);

		GRNModule grnModule = new GRNModule();
        grnModule.setGrn(grnElement);
		grnModule.setCell(c);
		c.getSomaElement().addLocalBiologyModule(grnModule);


		ActiveNeuriteChemoAttraction activeNeuriteModule = new ActiveNeuriteChemoAttraction(grnModule);
		neurite.addLocalBiologyModule(activeNeuriteModule);

        // Simulate
        Scheduler.simulate(maxTime);

	}
}
