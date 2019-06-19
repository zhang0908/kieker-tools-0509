package kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.AbstractGraphDivisionAlgorithmExecutor;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.util.TSSCommonUtils;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public abstract class AlgorithmClusteringExecutor extends AbstractGraphDivisionAlgorithmExecutor {
	
	public void execute(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
						 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, 
						 Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {

		Map<Integer, List<Integer>> nodeClusterMap = new HashMap<Integer, List<Integer>>();
		 
		Map<Integer, Integer> indexMap = calculateIndexMap(currentNodeList);
		 
	    double[][] graphMatrix = buildGraphDataStructrure(currentNodeList, indexMap);

	    printGraphMatrixData4Test(graphMatrix, indexMap, currentNodeList);
	     
	    int microServiceNum = TSSCommonUtils.getMicroserviceNum();
	     
	    try {
	    	 
	    	execute(graphMatrix, microServiceNum, nodeClusterMap);
	    	 
	    } catch (Exception e) {
	    	 
	    	e.printStackTrace();
	    	 
	    }
	     
	    Iterator<Integer> iterator = nodeClusterMap.keySet().iterator();
	     
	    while (iterator.hasNext()) {
	    	 
	    	int groupIndex = iterator.next();
	    	 
	    	List<Integer> partAList = nodeClusterMap.get(groupIndex);
	    	
	    	System.out.println();
	    	System.out.print("Group" + groupIndex + " : ");
	    	for (Integer tmp : partAList) {
	    		
	    		System.out.print(tmp + ", ");
	    		
	    	}
	    	 
	    	List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList(currentNodeList, partAList);
	    	 
	    	divideRsList.put(groupIndex, subNodeListA);
	    }
	     
	    System.out.println();
	    
	}
	
	public abstract void execute(double[][] graphMatrix, int microServiceNum, Map<Integer, List<Integer>> nodeClusterMap);
	 

	 private double[][] buildGraphDataStructrure(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, Map<Integer, Integer> indexMap) {
		 
		 int gNodeNum = gNodeList.size();
		 
		 double[][] graphMatrixData = new double[gNodeNum][gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 graphMatrixData[i][j] = 0;
			 }
			 
		 }
		 
		 for (int i = 0; i < gNodeList.size(); i++) {
			 
			 DependencyGraphNode<AllocationComponentOperationPair> gNode = gNodeList.get(i);
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = gNode.getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 int weight = outEdge.getTargetWeight().get();
				 
				 int xIndex = getGraphMatrixDataIndex(gNode, indexMap);
				 
				 int yIndex = getGraphMatrixDataIndex(outEdge.getTarget(), indexMap);
				 
				 if (xIndex == -1 || yIndex == -1 || xIndex == yIndex || xIndex >= graphMatrixData.length || yIndex >= graphMatrixData.length) {
					 
					 TSSCommonUtils.printlnRuntimeWarnMessage("Invalidate graph node index.....: start=" + xIndex + ", end=" + yIndex);
					 
					 continue;
					 
				 }
				 
				 if ((graphMatrixData[xIndex][yIndex] != 0 && graphMatrixData[xIndex][yIndex] != weight) 
						 || (graphMatrixData[yIndex][xIndex] != 0 && graphMatrixData[yIndex][xIndex] != weight)) {
						
						TSSCommonUtils.printlnRuntimeWarnMessage("Inconsistent edge weight, please check it. node id :" + gNode.getId() + ", outNode id : " + outEdge.getTarget().getId());
						
						continue;
						
				}
				 
				 graphMatrixData[xIndex][yIndex] = graphMatrixData[yIndex][xIndex] = weight;
				 
			 }
			 
		 }
		 
		 // 检查矩阵graphMatrixData是否存在孤岛，SpectralCluster算法不支持存在孤岛的矩阵
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 boolean isIsolated = false;
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 if (graphMatrixData[i][j] != 0.0) {
					 
					 isIsolated = true;
					 
					 break;
					 
				 }
				 
			 }
			 
			 // 存在孤岛，设置一个默认初始值
			 if (!isIsolated) {
				 
				 graphMatrixData[i][0] = graphMatrixData[0][i] = 0.1;
				 
			 }
			 
		 }
		 
		 return graphMatrixData;
		 
	 }
	 
}
