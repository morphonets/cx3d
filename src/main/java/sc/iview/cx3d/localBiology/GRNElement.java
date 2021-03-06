package sc.iview.cx3d.localBiology;

import fun.grn.grneat.evaluators.GRNGenomeEvaluator;
import fun.grn.grneat.evolver.GRNGene;
import fun.grn.grneat.evolver.GRNGenome;
import fun.grn.grneat.grn.GRNModel;
import fun.grn.grneat.grn.GRNProtein;

import java.util.Random;

public class GRNElement {
    // Genome that encodes the GRN
    public GRNGenome genome;

    // The state of the GRN
    public GRNModel state;

    // Make a GRNElement
    public GRNElement(GRNGenome grnGenome) {
        genome = grnGenome;
        state = GRNGenomeEvaluator.buildGRNFromGenome(grnGenome);
    }

    public GRNElement clone() {
        GRNElement myCopy = new GRNElement(genome.clone());
        return myCopy;
    }

    public GRNElement(String defaultType, boolean loadFromFile) {
        if( loadFromFile ) {
            try {
                genome = GRNGenome.loadFromFile(defaultType);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            if (defaultType.equalsIgnoreCase("ActiveNeuriteType1")) {
                genome = activeNeuriteType1();
            } else if (defaultType.equalsIgnoreCase("ActiveNeuriteType2")) {
                genome = activeNeuriteType2();
            }
        }
        state = GRNGenomeEvaluator.buildGRNFromGenome(genome);
    }

    public GRNGenome activeNeuriteType1() {
        int numGRNInputs = 4;
        int numGRNOutputs = 2;
        int maxGRNNodes = 50;
        Random rng = new Random();

        GRNGenome grnGenome = new GRNGenome();
        // Make inputs
        for(int k = 0; k < numGRNInputs; k++) {
            grnGenome.addGene(GRNGene.generateRandomGene(GRNProtein.INPUT_PROTEIN, k, rng));
        }
        // Make outputs
        for(int k = 0; k < numGRNOutputs; k++) {
            grnGenome.addGene(GRNGene.generateRandomGene(GRNProtein.OUTPUT_PROTEIN, k, rng));
        }
        // Make hidden
        for(int k = 0; k < rng.nextInt( maxGRNNodes - numGRNInputs - numGRNOutputs ); k++) {
            grnGenome.addGene(GRNGene.generateRandomRegulatoryGene(rng));
        }
        // Set GRN params
        grnGenome.setBeta(grnGenome.getBetaMin() + rng.nextDouble()*(grnGenome.getBetaMax() - grnGenome.getBetaMin()));
        grnGenome.setDelta(grnGenome.getDeltaMin() + rng.nextDouble()*(grnGenome.getDeltaMax() - grnGenome.getDeltaMin()));

        return grnGenome;
    }

    public GRNGenome activeNeuriteType2() {
        int numGRNInputs = 3;
        int numGRNOutputs = 4;
        int maxGRNNodes = 50;
        Random rng = new Random();

        GRNGenome grnGenome = new GRNGenome();
        // Make inputs
        for(int k = 0; k < numGRNInputs; k++) {
            grnGenome.addGene(GRNGene.generateRandomGene(GRNProtein.INPUT_PROTEIN, k, rng));
        }
        // Make outputs
        for(int k = 0; k < numGRNOutputs; k++) {
            grnGenome.addGene(GRNGene.generateRandomGene(GRNProtein.OUTPUT_PROTEIN, k, rng));
        }
        // Make hidden
        for(int k = 0; k < rng.nextInt( maxGRNNodes - numGRNInputs - numGRNOutputs ); k++) {
            grnGenome.addGene(GRNGene.generateRandomRegulatoryGene(rng));
        }
        // Set GRN params
        grnGenome.setBeta(grnGenome.getBetaMin() + rng.nextDouble()*(grnGenome.getBetaMax() - grnGenome.getBetaMin()));
        grnGenome.setDelta(grnGenome.getDeltaMin() + rng.nextDouble()*(grnGenome.getDeltaMax() - grnGenome.getDeltaMin()));

        return grnGenome;
    }
}
