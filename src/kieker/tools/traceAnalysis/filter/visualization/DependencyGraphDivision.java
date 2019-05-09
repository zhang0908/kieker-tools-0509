package kieker.tools.traceAnalysis.filter.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.Color;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster.GraphDivisionKMeans;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.mincut.GraphDivisionStoerWagner;
import kieker.tools.traceAnalysis.filter.visualization.util.dot.TSSCommonUtils;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public class DependencyGraphDivision {
	
//	 public int gNodeNum = 8;
	 public int INF=0x7f7f7f7f;
	 public int[] v,d,vis;
	 
//	 private OperationAllocationDependencyGraph dependencyGraph;
	 
//	 public int[][] graphWeightMatricx;
	 
//	 public DependencyGraphNode<AllocationComponentOperationPair>[] gVertices;
	 
	 private List<List<DependencyGraphNode<AllocationComponentOperationPair>>> minCunCollection = new ArrayList<List<DependencyGraphNode<AllocationComponentOperationPair>>>();
	 
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> leftSubGraphVertextList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> rightSubGraphVertextList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> grahpMinCutPart1 = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> grahpMinCutPart2 = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> leftSubGraphVertextListTmp = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 public List<DependencyGraphNode<AllocationComponentOperationPair>> rightSubGraphVertextListTmp = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	 

	 public DependencyGraphDivision() {
		 
//		 initGraphDataStructrure(graph);
		 
	 }
	 
	 private int[][] initGraphDataStructrure(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, Map<Integer, Integer> indexMap) {
		 
		 int gNodeNum = gNodeList.size();
		 
		 int[][] graphWeightMatricx = new int[gNodeNum][gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 graphWeightMatricx[i][j] = 0;
			 }
			 
		 }
		 
		 for (int i = 0; i < gNodeList.size(); i++) {
			 
			 DependencyGraphNode<AllocationComponentOperationPair> gNode = gNodeList.get(i);
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = gNode.getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 int weight = outEdge.getTargetWeight().get();
				 
				 int start = getGraphNodeIndex(gNode, indexMap);
				 
				 int end = getGraphNodeIndex(outEdge.getTarget(), indexMap);
				 
				 if (start == -1 || end == -1 || start >= graphWeightMatricx.length || end >= graphWeightMatricx.length) {
					 
					 continue;
					 
				 }
				 
				 graphWeightMatricx[start][end] = graphWeightMatricx[end][start] = weight;
				 
			 }
			 
		 }
		 
		 return graphWeightMatricx;
		 
	 }
	 
	 private double[][] initGraphDataStructrure4Cluster(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, Map<Integer, Integer> indexMap) {
		 
		 int gNodeNum = gNodeList.size();
		 
		 double[][] graphWeightMatricx = new double[gNodeNum][gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 graphWeightMatricx[i][j] = 0;
			 }
			 
		 }
		 
		 for (int i = 0; i < gNodeList.size(); i++) {
			 
			 DependencyGraphNode<AllocationComponentOperationPair> gNode = gNodeList.get(i);
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = gNode.getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 int weight = outEdge.getTargetWeight().get();
				 
				 int start = getGraphNodeIndex(gNode, indexMap);
				 
				 int end = getGraphNodeIndex(outEdge.getTarget(), indexMap);
				 
				 if (start == -1 || end == -1 || start == end || start >= graphWeightMatricx.length || end >= graphWeightMatricx.length) {
					 
					 TSSCommonUtils.printRuntimeWarnMessage("error: start = " + start + ", end = " + end + ", startNodeId = " + gNode.getId() + ", endNodeId = " + outEdge.getTarget().getId());
					 
					 continue;
					 
				 }
				 
				 graphWeightMatricx[start][end] = graphWeightMatricx[end][start] = weight;
				 
			 }
			 
		 }
		 
		 return graphWeightMatricx;
		 
	 }
	 
	 private int[][] initGraphDataStructrure2(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
		 
		 int gNodeNum = gNodeList.size();
		 
		 int[][] graphWeightMatricx = new int[gNodeNum][gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 graphWeightMatricx[i][j] = 0;
			 }
			 
		 }
		 
		 for (int i = 0; i < gNodeList.size(); i++) {
			 
			 DependencyGraphNode<AllocationComponentOperationPair> gNode = gNodeList.get(i);
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = gNode.getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 int weight = outEdge.getTargetWeight().get();
				 
				 int start = gNode.getId() - 1;
				 
				 int end = outEdge.getTarget().getId() - 1;
				 
				 if (start == -1 || end == -1 || start >= graphWeightMatricx.length || end >= graphWeightMatricx.length) {
					 
					 TSSCommonUtils.printRuntimeWarnMessage("Invalidate graph node index.....: start=" + start + ", end=" + end);
					 
					 continue;
					 
				 }
				 
				 graphWeightMatricx[start][end] = graphWeightMatricx[end][start] = weight;
				 
			 }
			 
		 }
		 
		 return graphWeightMatricx;
		 
	 }
	 
	 private double[][] initGraphDataStructrureForCluster(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
		 
		 int gNodeNum = gNodeList.size();
		 
		 double[][] graphWeightMatricx = new double[gNodeNum][gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 for (int j = 0; j < gNodeNum; j++) {
				 
				 graphWeightMatricx[i][j] = 0;
			 }
			 
		 }
		 
		 for (int i = 0; i < gNodeList.size(); i++) {
			 
			 DependencyGraphNode<AllocationComponentOperationPair> gNode = gNodeList.get(i);
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = gNode.getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 int weight = outEdge.getTargetWeight().get();
				 
				 int start = gNode.getId() - 1;
				 
				 int end = outEdge.getTarget().getId() - 1;
				 
				 if (start == -1 || end == -1 || start >= graphWeightMatricx.length || end >= graphWeightMatricx.length) {
					 
					 TSSCommonUtils.printRuntimeWarnMessage("Invalidate graph node index.....: start=" + start + ", end=" + end);
					 
					 continue;
					 
				 }
				 
				 graphWeightMatricx[start][end] = graphWeightMatricx[end][start] = weight;
				 
			 }
			 
		 }
		 
		 return graphWeightMatricx;
		 
	 }
	 
	 public void divideDependencyGraphForMicoservice(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, int minCutThreshold) {
		 
		 Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsListMap = new HashMap<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>>();
		 
		 if (TSSCommonUtils.TSS_DIVIDE_ALGORITHM_CLUSTERKMEANS.equalsIgnoreCase(TSSCommonUtils.getTssDivideAlgorithmName())) {
			 
			 divideDependencyGraphBySpectralCluster(parentNodeList, currentNodeList, minCutThreshold, divideRsListMap);
			 
		 } else if (TSSCommonUtils.TSS_DIVIDE_ALGORITHM_STOERWAGNER.equalsIgnoreCase(TSSCommonUtils.getTssDivideAlgorithmName())) {
			 
			 divideDependencyGraphByStoerWagner2(parentNodeList, currentNodeList, minCutThreshold, divideRsListMap);
			 
		 }
		 
		 Iterator<Integer> iterator = divideRsListMap.keySet().iterator();
		 
	     while (iterator.hasNext()) {
	    	 
	    	 int subGraghNo = iterator.next();
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = divideRsListMap.get(subGraghNo);
	    	 
	    	 System.out.println();
			 
			 System.out.print("微系统" + subGraghNo + ", : ");
			 
	    	 updateGraphNodeColor(subNodeListA, subGraghNo);
	    	 
	     }
	     
	     System.out.println();
		 
	 }
	 

	 public void divideDependencyGraphBySpectralCluster(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, 
			 int minCutThreshold, 
			 Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {

		 Map<Integer, List<Integer>> nodeClusterMap = new HashMap<Integer, List<Integer>>();
		 
		 Map<Integer, Integer> indexMap = calculateRealIndex(currentNodeList);
		 
	     double[][] graphMatrix = initGraphDataStructrure4Cluster(currentNodeList, indexMap);
	     
	     printGraphMatrix4Test(graphMatrix, indexMap);
	     
	     int microServiceNum = Integer.parseInt(TSSCommonUtils.getDivideProperties().getProperty(TSSCommonUtils.LABEL_DIVIDE_MICROSERVICE_NUM, TSSCommonUtils.DEFAULT_DIVIDE_MICROSERVICE_NUM));
	     
	     try {
	    	 
	    	 GraphDivisionKMeans.kMeans(graphMatrix, microServiceNum, nodeClusterMap);
	    	 
	     } catch (Exception e) {
	    	 
	    	 e.printStackTrace();
	    	 
	     }
	     
	     Iterator<Integer> iterator = nodeClusterMap.keySet().iterator();
	     
	     while (iterator.hasNext()) {
	    	 
	    	 int cutValue = iterator.next();
	    	 
	    	 List<Integer> partAList = nodeClusterMap.get(cutValue);
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList(parentNodeList, indexMap, partAList);
	    	 
	    	 divideRsList.put(cutValue, subNodeListA);
	     }
	     
	 }

	private void printGraphMatrix4Test(double[][] graphMatricx, Map<Integer, Integer> indexMap) {
		System.out.println("graph matricx data:-----------------");
	     
		System.out.print(String.format("%5s", 0) + ", ");
	     for (int j = 0; j < graphMatricx.length; j++) {
    		 
    		 System.out.print(String.format("%5s", (indexMap.get(j) + 1)) + ", ");
    	 }
	     System.out.println();
	     for (int i = 0; i < graphMatricx.length; i++) {
	    	 
	    	 System.out.print(String.format("%5s", (indexMap.get(i) + 1)) + ", ");
	    	 for (int j = 0; j < graphMatricx[i].length; j++) {
	    		 
	    		 System.out.print(String.format("%5s", graphMatricx[i][j]) + ", ");
	    	 }
	    	 System.out.println("||");
	    	 
	     }
	     System.out.println("graph matricx data:-----------------");
	}

	 public void divideDependencyGraphByStoerWagner(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, int minCutThreshold, 
			 List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {
		 
		 List<Integer> partAList = new ArrayList<Integer>();
	     List<Integer> partBList = new ArrayList<Integer>();
	     
	     Map<Integer, Integer> indexMap = calculateRealIndex(currentNodeList);
	     
	     int[][] graphMatricx = initGraphDataStructrure(currentNodeList, indexMap);
	     
	     int minCutValue = GraphDivisionStoerWagner.stoerWagner(graphMatricx, currentNodeList.size(), partAList, partBList);
	     
	     System.out.println("mincut:" + minCutValue);
	     
	     if (minCutValue <= minCutThreshold) {
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList(parentNodeList, indexMap, partAList);
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListB = getSubGraphNodeList(parentNodeList, indexMap, partBList);
	    	 
	    	 System.out.println();
	    	 System.out.println("----------------------Next Time--------------------------------");
	    	 
	    	 if (subNodeListA != null && subNodeListA.size() > 1) {
	    		 
	    		 System.out.println("/////////////////////sub A:------------");
	    		 
	    		 divideDependencyGraphByStoerWagner(parentNodeList, subNodeListA, minCutThreshold, divideRsList);
	    		 
	    	 } else {
	    		 
	    		 divideRsList.add(subNodeListA);
	    		 
	    	 }
	    	 
	    	 if (subNodeListB != null && subNodeListB.size() > 1) {
	    		 
	    		 System.out.println("/////////////////////sub B:------------");
	    		 
	    		 divideDependencyGraphByStoerWagner(parentNodeList, subNodeListB, minCutThreshold, divideRsList);
	    		 
	    	 } else {
	    		 
	    		 divideRsList.add(subNodeListB);
	    		 
	    	 }
	    	 
	     } else {
	    	 
	    	 System.out.println("graph min cut is over threshold, stop.");
	    	 
	    	 divideRsList.add(currentNodeList);
	    	 
	     }
	     
	 }
	 
	 public void divideDependencyGraphByStoerWagner2(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, int minCutThreshold, 
			 Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsMap) {
		 
		 List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList = new ArrayList<List<DependencyGraphNode<AllocationComponentOperationPair>>>();
		 
		 divideDependencyGraphByStoerWagner2(parentNodeList, currentNodeList, minCutThreshold, divideRsList);
		 
		 for (int i = 0; i < divideRsList.size(); i++) {
			 
			 divideRsMap.put(i, divideRsList.get(i));
			 
		 }
		 
	 }
	 
	 public void divideDependencyGraphByStoerWagner2(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
			 List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, int minCutThreshold, 
			 List<List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsList) {
		 
		 List<Integer> partAList = new ArrayList<Integer>();
	     List<Integer> partBList = new ArrayList<Integer>();
	     
	     int[][] graphMatricxData = initGraphDataStructrure2(parentNodeList);
	     
	     boolean[] graphMatricxStatus = initGraphDataEnableStatus2(parentNodeList, currentNodeList);
	     
	     int minCutValue = GraphDivisionStoerWagner.stoerWagner2(graphMatricxData, graphMatricxStatus, partAList, partBList);
	     
	     System.out.println("mincut:" + minCutValue);
	     
	     if (minCutValue <= minCutThreshold) {
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListA = getSubGraphNodeList2(parentNodeList, partAList);
	    	 
	    	 List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeListB = getSubGraphNodeList2(parentNodeList, partBList);
	    	 
	    	 System.out.println();
	    	 System.out.println("----------------------Next Time--------------------------------");
	    	 
	    	 if (subNodeListA != null && subNodeListA.size() > 1) {
	    		 
	    		 System.out.println("/////////////////////sub A:------------");
	    		 
	    		 divideDependencyGraphByStoerWagner2(parentNodeList, subNodeListA, minCutThreshold, divideRsList);
	    		 
	    	 } else {
	    		 
	    		 divideRsList.add(subNodeListA);
	    		 
	    	 }
	    	 
	    	 if (subNodeListB != null && subNodeListB.size() > 1) {
	    		 
	    		 System.out.println("/////////////////////sub B:------------");
	    		 
	    		 divideDependencyGraphByStoerWagner2(parentNodeList, subNodeListB, minCutThreshold, divideRsList);
	    		 
	    	 } else {
	    		 
	    		 divideRsList.add(subNodeListB);
	    		 
	    	 }
	    	 
	     } else {
	    	 
	    	 System.out.println("graph min cut is over threshold, stop.");
	    	 
	    	 divideRsList.add(currentNodeList);
	    	 
	     }
	     
	 }
	 
	 private boolean[] initGraphDataEnableStatus2(
			List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList,
			List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList) {
		 
		 int gNodeNum = parentNodeList.size();
		 
		 boolean[] graphWeightMatricxStatus = new boolean[gNodeNum];
		 
		 for (int i = 0; i < gNodeNum; i++) {
			 
			 graphWeightMatricxStatus[parentNodeList.get(i).getId() - 1] = false;
			 
		 }
		 
		 for (int i = 0; i < currentNodeList.size(); i++) {
			 
			 graphWeightMatricxStatus[currentNodeList.get(i).getId() - 1] = true;
			 
		 }
		 
		return graphWeightMatricxStatus;
		
	}

	private void updateGraphNodeColor(List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList, int microServiceNo) {
		 
    	 Color tmpAColor = TSSCommonUtils.generateRandomColor();
    	 
    	 for (DependencyGraphNode<AllocationComponentOperationPair> node : subNodeList) {
    		 
    		 String signature = node.getEntity().getOperation().getSignature().toString();
    		 
    		 System.out.print(signature.substring(0, signature.lastIndexOf("(")) + ", ");
    		 
    		 node.setColor(tmpAColor);
    		 
    		 updateGraphNodeInDependencyNodeColor(node, tmpAColor);
    		 
    		 ComponentType oldComponentType = node.getEntity().getAllocationComponent().getAssemblyComponent().getType();
    		 
    		 AssemblyComponent oldAssemblyComponent = node.getEntity().getAllocationComponent().getAssemblyComponent();
    		 
    		 AllocationComponent oldAllocationComponent = node.getEntity().getAllocationComponent();
    		 
    		 String packageName = "MicroService";
    		 String typeName = "" + microServiceNo;
    		 
    		 String fullQualifiedName = packageName + "." + typeName;
    		 
    		 ComponentType newComponentType = new ComponentType(oldComponentType.getId(), fullQualifiedName);
    		 
    		 AssemblyComponent newAssemblyComponent = new AssemblyComponent(oldAssemblyComponent.getId(), oldAssemblyComponent.getName(), newComponentType);
    		 
    		 AllocationComponent newAllocationComponent = new AllocationComponent(oldAllocationComponent.getId() + 10000 + microServiceNo, newAssemblyComponent, oldAllocationComponent.getExecutionContainer());
    		 
    		 node.getEntity().setAllocationComponent(newAllocationComponent);
    		 
    	 }
		 
	 }
	 
	 
	 private void updateGraphNodeInDependencyNodeColor(DependencyGraphNode<AllocationComponentOperationPair> node, Color tmpAColor) {
		 
		 if (node == null || node.getIncomingDependencies() == null || node.getIncomingDependencies().size() < 1) {
			 
			 return;
			 
		 }
		 
		 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> inEdges = node.getIncomingDependencies().iterator();
		 
		 while (inEdges.hasNext()) {
			 
			 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> inEdge = inEdges.next();
			 
			 if (inEdge.getSource().getId() == inEdge.getTarget().getId() || TSSCommonUtils.isGraphSQLTableNode(inEdge.getTarget())) {
				 
				 continue;
				 
			 }
			 
			 inEdge.setColor(tmpAColor);
			 
			 if (inEdge.getTarget() != null) {
				 
				 inEdge.getTarget().setColor(tmpAColor);
				 
			 }
			 
			 Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = inEdge.getTarget().getOutgoingEdges().iterator();
			 
			 while (outEdges.hasNext()) {
				 
				 WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdges.next();
				 
				 if (outEdge.getTarget().getId() == inEdge.getSource().getId()) {
					 
					 outEdge.setColor(tmpAColor);
					 
					 break;
					 
				 }
				 
			 }
			 
			 if (inEdge.getTarget() != null  && !TSSCommonUtils.isGraphSQLTableNode(inEdge.getTarget())) {
				 
				 updateGraphNodeInDependencyNodeColor(inEdge.getTarget(), tmpAColor);
				 
			 }
			 
		 }
		 
	 }
	 
	 private Map<Integer, Integer> calculateRealIndex(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
		 
		 Map<Integer, Integer> realIndexMap = new HashMap<Integer, Integer>();
		 
		 int size = gNodeList.size();
		 
		 for (int i = 0; i < size; i++) {
			 
			 int id = gNodeList.get(i).getId() - 1;
			 
			 int index = id % size;
			 
			 while (realIndexMap.get(index) != null) {
				 
				 index = (index + 1) % size;
				 
			 }
			 
			 realIndexMap.put(index, id);
			 
			 realIndexMap.put(10000 + id, index);
			 
		 }
		 
		 return realIndexMap;
		 
	 }
	 
	 private List<DependencyGraphNode<AllocationComponentOperationPair>> getSubGraphNodeList(
								 List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, 
								 Map<Integer, Integer> indexMap, List<Integer> partAList) {
		 
		 List<DependencyGraphNode<AllocationComponentOperationPair>> dest = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		 
		 if (partAList != null && partAList.size() > 0) {
			 
			 for (Integer tmp : partAList) {
				 
				 dest.add(gNodeList.get(indexMap.get(tmp)));
				 
			 }
			 
		 }
		 
		 return dest;
		 
	 }
	 
	 private List<DependencyGraphNode<AllocationComponentOperationPair>> getSubGraphNodeList2(
			 List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, List<Integer> partAList) {

		List<DependencyGraphNode<AllocationComponentOperationPair>> dest = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		if (partAList != null && partAList.size() > 0) {
			
			for (Integer tmp : partAList) {
			
				dest.add(gNodeList.get(tmp));
			
			}
		
		}
		
		return dest;
	
	}
	 

//	public int divideDependencyGraph(int sn) {
//
//		int n = 11;
//		
//		int res = INF;
//
//		for (int i = 0; i < n; i++) {
//
//			v[i] = i;
//
//		}
//
//		while (n > 1) {
//
//			leftSubGraphVertextListTmp.clear();
//			leftSubGraphVertextListTmp.add(gVertices[sn]);
//
//			rightSubGraphVertextListTmp.clear();
//
//			int maxp = 0, prev = sn;
//			for (int i = 0; i < n; i++) {
//				// 初始化到已圈集合的割大小,并找出最大距离的顶点
//				d[v[i]] = graphWeightMatricx[v[sn]][v[i]];
//				if (d[v[i]] > d[v[maxp]]) {
//
//					maxp = i;
//
//				}
//			}
//			clearArray(vis);
//			vis[v[sn]] = 1;
//			for (int i = 1; i < n; i++) {
//				if (i == n - 1) {
//
//					rightSubGraphVertextListTmp.add(gVertices[v[prev]]);
//
//					for (int k = 0; k < n; k++) {
//
//						if (vis[v[k]] == 0) {
//
//							rightSubGraphVertextListTmp.add(gVertices[v[k]]);
//
//						}
//
//					}
//
//					leftSubGraphVertextListTmp.remove(leftSubGraphVertextListTmp.size() - 1);
//
//					// 只剩最后一个没加入集合的点，更新最小割
////		                res=Math.min(res,d[v[maxp]]);
//
//					if (res > d[v[maxp]]) {
//
//						res = d[v[maxp]];
//
//						leftSubGraphVertextList.clear();
//						leftSubGraphVertextList.addAll(leftSubGraphVertextListTmp);
//
////		                	rightSubGraphVertextList.clear();
////		                	rightSubGraphVertextList.addAll(minCunCollection.get(rightSubGraphVertextListTmp.get(0).nIndex));
//
//						grahpMinCutPart1.clear();
//						grahpMinCutPart1.addAll(rightSubGraphVertextList);
//
//					}
//
//					for (int j = 0; j < n; j++) {
//						// 合并最后一个点以及推出它的集合中的点
//						graphWeightMatricx[v[prev]][v[j]] += graphWeightMatricx[v[j]][v[maxp]];
//						graphWeightMatricx[v[j]][v[prev]] = graphWeightMatricx[v[prev]][v[j]];
//					}
//					// 第maxp个节点去掉，第n个节点变成第maxp个
//					v[maxp] = v[--n];
//
////		                System.out.println((v[prev] + 1) + ", " + (v[maxp] + 1));
//
//				} else {
//
//					vis[v[maxp]] = 1;
//					prev = maxp;
//					maxp = -1;
//
//					leftSubGraphVertextListTmp.add(gVertices[prev]);
//
//					for (int j = 0; j < n; j++) {
//
//						// 将上次求的maxp加入集合，合并与它相邻的边到割集
//						if (vis[v[j]] == 0) {
//							d[v[j]] += graphWeightMatricx[v[prev]][v[j]];
//							if (maxp == -1 || d[v[maxp]] < d[v[j]]) {
//
//								maxp = j;
//
//							}
//						}
//
//					}
//
//				}
//			}
//
//			mergeNodeCollection();
//
//		}

//		doWithGraphMinCutCollection();
//
//		System.out.print("Collection  I: ");
//		for (DependencyGraphNode<AllocationComponentOperationPair> gVertext : grahpMinCutPart1) {
//
//			System.out.print(gVertext.getId() + ", ");
//
//		}
//
//		System.out.println();
//
//		System.out.print("Collection II: ");
//		for (DependencyGraphNode<AllocationComponentOperationPair> gVertext : grahpMinCutPart2) {
//
//			System.out.print(gVertext.getId() + ", ");
//
//		}
//
//		System.out.println();
//		System.out.print("Mincut  value: ");
//
//		return res;
//	}
	
	private int getGraphNodeIndex(DependencyGraphNode<AllocationComponentOperationPair> vertice, Map<Integer, Integer> indexMap) {
		
		try {
			
			if (vertice != null) {
				
				return indexMap.get(10000 + (vertice.getId() - 1));
				
			}
			
		} catch (Exception e) {
			
			
		}
		
		return -1;
		
	}
//
//	public void doWithGraphMinCutCollection() {
//		
//		boolean validate = true;
//		
//		for (int i = 0; i < gVertices.length; i++) {
//			
//			validate = true;
//			
//			for (DependencyGraphNode<AllocationComponentOperationPair> gVertice : grahpMinCutPart1) {
//			
//				if(getGraphNodeIndex(gVertice) == getGraphNodeIndex(gVertices[i])) {
//					
//					validate = false;
//					
//					break;
//					
//				}
//				
//			}
//			
//			if (validate == true) {
//				
//				grahpMinCutPart2.add(gVertices[i]);
//				
//			}
//			
//		}
//		
//	}
		
//	public void mergeNodeCollection() {
//		
//		if (rightSubGraphVertextListTmp != null && rightSubGraphVertextListTmp.size() > 1) {
//			
//			DependencyGraphNode<AllocationComponentOperationPair> tmp = rightSubGraphVertextListTmp.get(0);
//			
//			for (int i = 1; i < rightSubGraphVertextListTmp.size(); i++) {
//				
//				minCunCollection.get(getGraphNodeIndex(tmp)).addAll(minCunCollection.get(getGraphNodeIndex(rightSubGraphVertextListTmp.get(i))));
//				
//				minCunCollection.get(getGraphNodeIndex(rightSubGraphVertextListTmp.get(i))).clear();
//				
//			}
//			
//			rightSubGraphVertextList.clear();
//       	rightSubGraphVertextList.addAll(minCunCollection.get(getGraphNodeIndex(tmp)));
//			
//		}
//		
//	}
//		
	
	 
	 public void clearArray(int[] s) {
		 
		 for (int i = 0; i < s.length; i++) {
			 
			 s[i] = 0;
			 
		 }
		 
	 }
	 
	public static void main(String[] args) {
		
//		DependencyGraphDivision instance = new DependencyGraphDivision();
		
//		
//		int res04 = instance.Stoer_Wagner_04(8, 1);
//		
//		System.out.println(res04);
		

	}
	
}
