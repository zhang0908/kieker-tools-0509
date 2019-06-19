package kieker.tools.traceAnalysis.filter.visualization.graphdivide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public abstract class AbstractGraphDivisionAlgorithmExecutor {
	
	public abstract void execute(List<DependencyGraphNode<AllocationComponentOperationPair>> parentNodeList, 
									List<DependencyGraphNode<AllocationComponentOperationPair>> currentNodeList, 
									Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsMap);

	protected Map<Integer, Integer> calculateIndexMap(List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
		 
		Map<Integer, Integer> realIndexMap = new HashMap<Integer, Integer>();
		
		int size = gNodeList.size();
		
		for (int i = 0; i < size; i++) {
			
			int id = gNodeList.get(i).getId();
			
			realIndexMap.put(i, id);
			
			realIndexMap.put(10000 + id, i);
			
		}
		 
		return realIndexMap;
		
	}
	 
	
	protected static int getGraphMatrixDataIndex(DependencyGraphNode<AllocationComponentOperationPair> vertice, Map<Integer, Integer> indexMap) {
		
		try {
			
			if (vertice != null) {
				
				return indexMap.get(10000 + vertice.getId());
				
			}
			
		} catch (Exception e) {
			
			
		}
		
		return -1;
		
	}
	
	 protected static List<DependencyGraphNode<AllocationComponentOperationPair>> getSubGraphNodeList(
			 List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, List<Integer> partAList) {

		List<DependencyGraphNode<AllocationComponentOperationPair>> dest = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		if (partAList != null && partAList.size() > 0) {
		
			for (Integer tmp : partAList) {
			
				dest.add(gNodeList.get(tmp));
			
			}
		
		}
		
		return dest;
	
	}
	 

	 protected List<DependencyGraphNode<AllocationComponentOperationPair>> getSubGraphNodeList2(
			 List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList, List<Integer> partAList) {

		List<DependencyGraphNode<AllocationComponentOperationPair>> dest = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		if (partAList != null && partAList.size() > 0) {
			
			for (Integer tmp : partAList) {
			
				dest.add(gNodeList.get(tmp));
			
			}
		
		}
		
		return dest;
	
	}
	 

		protected void printGraphMatrixData4Test(double[][] graphMatricx, Map<Integer, Integer> indexMap, List<DependencyGraphNode<AllocationComponentOperationPair>> gNodeList) {
			
			System.out.println("graph matricx data:-----------------");
			
			System.out.println();
			
			for (int j = 0; j < gNodeList.size(); j++) {
	    		 
	    		 System.out.println(String.format("%7s", "      " + j + "<-->" + indexMap.get(j) + getNodeName(gNodeList.get(j)) + ", "));
	    	}
			
			System.out.println();
			System.out.print(String.format("%7s", 0 + ", "));
		     for (int j = 0; j < graphMatricx.length; j++) {
	    		 
	    		 System.out.print(String.format("%7s", j + "(" + indexMap.get(j) + ")" + ", "));
	    	 }
		     System.out.println();
		     for (int i = 0; i < graphMatricx.length; i++) {
		    	 
		    	 int totalNum = 0;
		    	 
		    	 System.out.print(String.format("%7s", i + "(" + indexMap.get(i) + ")" + ", "));
		    	 for (int j = 0; j < graphMatricx[i].length; j++) {
		    		 
		    		 totalNum += graphMatricx[i][j];
		    				 
		    		 System.out.print(String.format("%7s", graphMatricx[i][j] + ", "));
		    	 }
		    	 gNodeList.get(i).getEntity().getOperation().getSignature().setSumWeightToAllOtherNodes(totalNum);
		    	 System.out.print(String.format("%7s", totalNum + ", "));
		    	 System.out.println("||");
		    	 
		     }
		     
		     System.out.println("graph matricx data:-----------------");
		}
		
		
		private String getNodeName(DependencyGraphNode<AllocationComponentOperationPair> node) {
			
			String signature = node.getEntity().getOperation().getSignature().toString();
			
	   		return signature.substring(0, signature.lastIndexOf("("));
			
		}


}
