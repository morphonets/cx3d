package sc.iview.cx3d.utilities;

import java.util.HashSet;
import java.util.Set;

public class SNT {
    
    static public Set<String> getAvailableTreeAnalyzerMetrics() {
        Set<String> metrics = new HashSet<>();
        metrics.add("Height");
        metrics.add("No. of terminal branches");
        metrics.add("No. of tips");
        metrics.add("No. of branches");
        metrics.add("No. of nodes");
        metrics.add("No. of paths");
        metrics.add("Length of primary branches (sum)");
        metrics.add("Cable length");
        metrics.add("No. of primary branches");
        metrics.add("Highest path order");
        metrics.add("Assigned value");
        metrics.add("Length of terminal branches (sum)");
        metrics.add("Depth");
        metrics.add("Width");
        metrics.add("No. of branch points");
        metrics.add("No. of fitted paths");
        metrics.add("Mean radius");
        metrics.add("Horton-Strahler bifurcation ratio");
        metrics.add("Horton-Strahler number");
        return metrics;
    }
    
}
