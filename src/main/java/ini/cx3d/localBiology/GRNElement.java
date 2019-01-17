package ini.cx3d.localBiology;

import evaluators.GRNGenomeEvaluator;
import evolver.GRNGenome;
import grn.GRNModel;

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
}
