package kieker.tools.traceAnalysis.filter.visualization.graphdivide.mincut;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.AbstractGraphDivisionAlgorithmExecutor;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.util.TSSCommonUtils;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public class AlgorithmStoerWagnerExecutor extends AbstractGraphDivisionAlgorithmExecutor {
	

	public void execute(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, 
			Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsMap) {
		 
		List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList = new ArrayList<List<DependencyGraphNode<AllocationComponentOperationPair>>>();
		 
		int minCutThreshold = TSSCommonUtils.getMinCutThresholdForStoerWagnerAlgorithm();
		 
		divideDependencyGraph(parentNodeList, currentNodeList, minCutThreshold, divideRsList);
		
		for (int i = 0; i < divideRsList.size(); i++) {
			 
			divideRsMap.put(i, divideRsList.get(i));
			 
		}
		 
	}
	 
	public void divideDependencyGraph(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, int minCutThreshold, 
			List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {
		 
		List<Integer> partAList = new ArrayList<Integer>();
	    List<Integer> partBList = new ArrayList<Integer>();
	     
	    Map<Integer, Integer> indexMap = calculateIndexMap(currentNodeList);
	     
	    double[][] graphMatricx = buildGraphMatrixDataStructrure(currentNodeList, indexMap);
	    
	    printGraphMatrixData4Test(graphMatricx, indexMap, currentNodeList);
	    
	    TSSCommonUtils.printlnRuntimeWarnMessage("Graph Division--------------------------------");
	     
	    int minCutValue = AlgorithmStoerWagner.stoerWagner(graphMatricx, currentNodeList.size(), partAList, partBList);
	     
	    TSSCommonUtils.printlnRuntimeWarnMessage("The minimum cut value of this graph division is :" + minCutValue);
	     
	    if (minCutValue <= minCutThreshold) {
	    	 
	    	List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList(parentNodeList, partAList);
	    	 
	    	List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListB = getSubGraphNodeList(parentNodeList, partBList);
	    	
	    	if (subNodeListA != null && subNodeListA.size() > 1) {
	    		 
	    		divideDependencyGraph(parentNodeList, subNodeListA, minCutThreshold, divideRsList);
	    		 
	    	} else {
	    		 
	    		divideRsList.add(subNodeListA);
	    		 
	    	}
	    	 
	    	if (subNodeListB != null && subNodeListB.size() > 1) {
	    		 
	    		divideDependencyGraph(parentNodeList, subNodeListB, minCutThreshold, divideRsList);
	    		 
	    	} else {
	    		 
	    		divideRsList.add(subNodeListB);
	    		 
	    	}
	    	 
	    } else {
	    	 
	    	TSSCommonUtils.printlnRuntimeWarnMessage("Threshold has been reached, stop graph division.");
	    	 
	    	divideRsList.add(currentNodeList);
	    	 
	    }
	     
	}
	
	
	private double[][] buildGraphMatrixDataStructrure(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, Map<Integer, Integer> indexMap) {
		 
		int size = gNodeList.size();
		 
		double[][] graphMatricxDataStructure = new double[size][size];
		 
		for (int i = 0; i < size; i++) {
			 
			for (int j = 0; j < size; j++) {
				 
				graphMatricxDataStructure[i][j] = 0;
			}
			 
		}
		 
		for (int i = 0; i < gNodeList.size(); i++) {
			
			DependencyGraphNode<AllocationComponentOperationPair> node = gNodeList.get(i);
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node.getOutgoingEdges().iterator();
			
			while (outEdgeIterator.hasNext()) {
				
				WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgeIterator.next();
				 
				int weight = outEdge.getTargetWeight().get();
				 
				int xIndex = getGraphMatrixDataIndex(node, indexMap);
				 
				int yIndex = getGraphMatrixDataIndex(outEdge.getTarget(), indexMap);
				 
				if (xIndex == -1 || yIndex == -1 || xIndex >= graphMatricxDataStructure.length || yIndex >= graphMatricxDataStructure.length) {
					
					if (xIndex == -1 || xIndex >= graphMatricxDataStructure.length || yIndex >= graphMatricxDataStructure.length) {
						
						TSSCommonUtils.printlnRuntimeWarnMessage("Graph matrix data index out of range, please check it. nodeId=" 
								+ node.getId() + ", xIndex=" + xIndex + ", yIndex=" + yIndex + ", size=" + graphMatricxDataStructure.length);
						
					}
					
					continue;
					 
				}
				
				if (graphMatricxDataStructure[xIndex][yIndex] != 0 && graphMatricxDataStructure[xIndex][yIndex] != weight) {
					
					TSSCommonUtils.printlnRuntimeWarnMessage("Inconsistent edge weight, please check it. node id :" + node.getId() + ", outNode id : " + outEdge.getTarget().getId());
					
					continue;
					
				}
				 
				graphMatricxDataStructure[xIndex][yIndex] = graphMatricxDataStructure[yIndex][xIndex] = weight;
				 
			}
			 
		}
		 
		return graphMatricxDataStructure;
		 
	}
	 
	public void divideDependencyGraphByStoerWagner2(List<DependencyGraphNode<AllocationComponentOperationPair>> allNodeList, 
			List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList, int minCutThreshold, 
			List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {
		 
		List<Integer> partAList = new ArrayList<Integer>();
	    List<Integer> partBList = new ArrayList<Integer>();
	     
	    double[][] graphMatrixDataStructure = buildGraphMatrixDataStructrure2(allNodeList);
	     
	    boolean[] graphMatrixEnableStatus = buildGraphMatrixDataEnableStatus2(allNodeList, sqlTableNodeList);
	     
		TSSCommonUtils.printlnRuntimeWarnMessage("Graph Division--------------------------------");
		    
	    int minCutValue = AlgorithmStoerWagner.stoerWagner2(graphMatrixDataStructure, graphMatrixEnableStatus, partAList, partBList);
	     
		TSSCommonUtils.printlnRuntimeWarnMessage("The minimum cut value of this graph division is :" + minCutValue);
		    
	    if (minCutValue <= minCutThreshold) {
	    	 
	    	List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList2(allNodeList, partAList);
	    	 
	    	List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListB = getSubGraphNodeList2(allNodeList, partBList);
	    	 
	    	if (subNodeListA != null && subNodeListA.size() > 1) {
	    		 
	    		divideDependencyGraphByStoerWagner2(allNodeList, subNodeListA, minCutThreshold, divideRsList);
	    		 
	    	} else {
	    		 
	    		divideRsList.add(subNodeListA);
	    		 
	    	}
	    	 
	    	if (subNodeListB != null && subNodeListB.size() > 1) {
	    		 
	    		divideDependencyGraphByStoerWagner2(allNodeList, subNodeListB, minCutThreshold, divideRsList);
	    		 
	    	} else {
	    		 
	    		divideRsList.add(subNodeListB);
	    		 
	    	}
	    	 
	    } else {

	    	TSSCommonUtils.printlnRuntimeWarnMessage("Threshold has been reached, stop graph division.");
	    	 
	    	divideRsList.add(sqlTableNodeList);
	    	 
	    }
	     
	}
	 
	private boolean[] buildGraphMatrixDataEnableStatus2(
			List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList,
			List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList) {
		
		int gNodeNum = parentNodeList.size();
		
		boolean[] graphWeightMatricxStatus = new boolean[gNodeNum];
		 
		for (int i = 0; i < gNodeNum; i++) {
			 
			graphWeightMatricxStatus[parentNodeList.get(i).getId() - 1] = false;
			 
		}
		 
		for (int i = 0; i < subNodeList.size(); i++) {
			 
			graphWeightMatricxStatus[subNodeList.get(i).getId() - 1] = true;
			 
		}
		 
		return graphWeightMatricxStatus;
		
	}

	private double[][] buildGraphMatrixDataStructrure2(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
		
		int gNodeNum = gNodeList.size();
		
		double[][] graphMatricxDataStructure = new double[gNodeNum][gNodeNum];
		
		for (int i = 0; i < gNodeNum; i++) {
			
			for (int j = 0; j < gNodeNum; j++) {
				
				graphMatricxDataStructure[i][j] = 0;
			}
			
		}
		
		for (int i = 0; i < gNodeList.size(); i++) {
			
			DependencyGraphNode<AllocationComponentOperationPair> node = gNodeList.get(i);
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = node.getOutgoingEdges().iterator();
			
			while (outEdges.hasNext()) {
				
				WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				
				int weight = outEdge.getTargetWeight().get();
				
				int xIndex = node.getId() - 1;
				
				int yIndex = outEdge.getTarget().getId() - 1;
				
				if (xIndex == -1 || yIndex == -1 || xIndex >= graphMatricxDataStructure.length || yIndex >= graphMatricxDataStructure.length) {
					
					TSSCommonUtils.printlnRuntimeWarnMessage("Invalidate graph node index.....: start=" + xIndex + ", end=" + yIndex);
					
					continue;
					
				}
				
				if (graphMatricxDataStructure[xIndex][yIndex] != 0 && graphMatricxDataStructure[xIndex][yIndex] != weight) {
					
						TSSCommonUtils.printlnRuntimeWarnMessage("Inconsistent edge weight, please check it. node id :" + node.getId() + ", outNode id : " + outEdge.getTarget().getId());
						
						continue;
						
				}
				
				graphMatricxDataStructure[xIndex][yIndex] = graphMatricxDataStructure[yIndex][xIndex] = weight;
				
			}
			
		}
		 
		return graphMatricxDataStructure;
		
	}
	
}
