package sc.iview.cx3d.simulations.grn;

import fun.grn.grneat.evolver.GRNGenome;
import picocli.CommandLine;
import sc.iview.cx3d.simulations.tutorial.ActiveNeuriteChemoAttraction;

import java.util.Random;

import static sc.iview.cx3d.simulations.tutorial.ActiveNeuriteChemoAttraction.writePredicateFilteredRandomGRNToFile;

import fun.grn.grneat.cli.MutateGenome;

public class GenerateGRNs {

    static String directory = "./";
    static long randomSeed = 7299713;

    private static String[] mutationModes = {"minDistance", "fixedMutations"};
    private static String mutationMode = mutationModes[1];
    private static double numMutationsPerMutant = 5;//
    static double minDistance = 0.001;

    private static int numGRNs = 25;
    private static int numMutants = 10;

    public static void main(String[] args) throws Exception {

        Random rng = new Random(randomSeed);

        for( int grn = 0; grn < numGRNs; grn++ ) {
            String basename = "grn_lineage_" + grn;

            String grnFilename = directory + basename + "_num_0.grn";
            randomSeed = rng.nextLong();

            GRNGenome[] genomes = new GRNGenome[numMutants];

            writePredicateFilteredRandomGRNToFile(grnFilename, ActiveNeuriteChemoAttraction.grnPredicate, randomSeed);

            genomes[0] = GRNGenome.loadFromFile(grnFilename);
            genomes[1] = genomes[0];

            String parentGRN = grnFilename;

            for( int mut = 0; mut < numMutants - 1; mut++ ) {
                grnFilename = directory + basename + "_num_" + (mut + 1) + ".grn";

                genomes[( mut + 1 )] = genomes[0]; // Initialize the mutant to the previous mutant
                //genomes[( mut + 1 )] = genomes[mut]; // Initialize the mutant to the previous mutant
                genomes[( mut + 1 )].writeToFile(grnFilename);

                if( mutationMode.equalsIgnoreCase(mutationModes[0]) ) {// minDistance mode

                    double dist = genomes[0].distanceTo(genomes[(mut + 1)], true);
                    System.out.println("Distance = " + dist);

                    while (dist < minDistance * (mut + 1)) {
                        dist = genomes[0].distanceTo(genomes[(mut + 1)], true);
                        System.out.println("Distance = " + dist);

                        args = new String[]{
                                "--randomSeed", String.valueOf(rng.nextLong()),
                                "--inputPath", grnFilename,
                                "--outputPath", grnFilename,
                                "--pAddMutation", String.valueOf(0.333333),
                                "--addMutationMaxSize", String.valueOf(2),
                                "--pDelMutation", String.valueOf(0.333333),
                                "--delMutationMinSize", String.valueOf(2),
                                "--pChangeMutation", String.valueOf(0.33333)};

                        //int exitCode = new MutateGenome().call(args);
                        try {
                            CommandLine.call(new MutateGenome(), args);
                        } catch (Exception e) {
                            continue;
                        }

                        genomes[(mut + 1)] = GRNGenome.loadFromFile(grnFilename);
                    }
                } else if( mutationMode.equalsIgnoreCase(mutationModes[1]) ) {// fixedMutations mode

                    for( int m = 0; m < numMutationsPerMutant * mut; m++ ) {

                        double dist = genomes[0].distanceTo(genomes[(mut + 1)], true);
                        System.out.println("Distance = " + dist);

                        args = new String[]{
                                "--randomSeed", String.valueOf(rng.nextLong()),
                                "--inputPath", grnFilename,
                                "--outputPath", grnFilename,
                                "--pAddMutation", String.valueOf(0.333333),
                                "--addMutationMaxSize", String.valueOf(10),
                                "--pDelMutation", String.valueOf(0.333333),
                                "--delMutationMinSize", String.valueOf(2),
                                "--pChangeMutation", String.valueOf(0.33333)};

                        //int exitCode = new MutateGenome().call(args);
                        try {
                            CommandLine.call(new MutateGenome(), args);
                        } catch (Exception e) {
                            continue;
                        }

                        genomes[(mut + 1)] = GRNGenome.loadFromFile(grnFilename);
                    }
                }
            }
        }
    }
}
