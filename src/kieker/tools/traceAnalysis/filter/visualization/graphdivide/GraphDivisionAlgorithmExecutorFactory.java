package kieker.tools.traceAnalysis.filter.visualization.graphdivide;

import kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster.AlgorithmClusteringKMeansExecutor;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster.AlgorithmClusteringSpectralExecutor;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.mincut.AlgorithmStoerWagnerExecutor;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.util.TSSCommonUtils;

public class GraphDivisionAlgorithmExecutorFactory {
	
	public static AbstractGraphDivisionAlgorithmExecutor getGraphDivisionAlgorithmExecutor() {
		
		if (TSSCommonUtils.TSS_DIVIDE_ALGORITHM_CLUSTER_KMEANS.equalsIgnoreCase(TSSCommonUtils.getTssDivideAlgorithmName())) {
			 
			return new AlgorithmClusteringKMeansExecutor();
			 
		} else if (TSSCommonUtils.TSS_DIVIDE_ALGORITHM_CLUSTER_SPECTRAL.equalsIgnoreCase(TSSCommonUtils.getTssDivideAlgorithmName())) {
			 
			return new AlgorithmClusteringSpectralExecutor();
			 
		} else if (TSSCommonUtils.TSS_DIVIDE_ALGORITHM_STOERWAGNER.equalsIgnoreCase(TSSCommonUtils.getTssDivideAlgorithmName())) {
			 
			return new AlgorithmStoerWagnerExecutor();
			 
		}
		
		return null;
		
	}

}
