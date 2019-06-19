package kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster;

import java.util.List;
import java.util.Map;

public class AlgorithmClusteringKMeansExecutor extends AlgorithmClusteringExecutor {

	@Override
	public void execute(double[][] graphMatrix, int microServiceNum, Map<Integer, List<Integer>> nodeClusterMap) {
		
		AlgorithmClusterKMeans.kMeans(graphMatrix, microServiceNum, nodeClusterMap);
		
	}

}
