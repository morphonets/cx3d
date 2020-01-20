package sc.iview.cx3d.simulations.grn;

import fun.grn.grneat.cli.MutateGenome;
import fun.grn.grneat.evolver.GRNGenome;
import picocli.CommandLine;
import sc.iview.cx3d.simulations.tutorial.ActiveNeuriteChemoAttraction;

import java.util.Random;

import static sc.iview.cx3d.simulations.tutorial.ActiveNeuriteChemoAttraction.writePredicateFilteredRandomGRNToFile;

public class GenerateGRNs {

    static String directory = "./";
    static long randomSeed = 539435146;

    private static int numGRNs = 5;

    public static void main(String[] args) throws Exception {

        Random rng = new Random(randomSeed);

        for( int grn = 0; grn < numGRNs; grn++ ) {
            String basename = "grn_" + grn;

            String grnFilename = directory + basename + ".grn";
            randomSeed = rng.nextLong();

            writePredicateFilteredRandomGRNToFile(grnFilename, ActiveNeuriteChemoAttraction.grnPredicate, randomSeed);

        }
    }
}
