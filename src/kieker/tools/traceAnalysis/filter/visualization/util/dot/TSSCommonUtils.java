package kieker.tools.traceAnalysis.filter.visualization.util.dot;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.Color;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public class TSSCommonUtils {

	public final static String DEFAULT_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL = "100";
	public final static String DEFAULT_GRAPH_EDGE_WEIGHT_VALUE_SAME_API = "50";
	public final static String DEFAULT_GRAPH_EDGE_WEIGHT_VALUE_SAME_SCENE = "10";
	public final static String DEFAULT_GRAPH_MINCUT_THRESHOLD = "500";
	public final static String DEFAULT_LABLE_CONTAINER_CLASS_FUNCTION_LOWERCASE = "class-function";
	public final static String DEFAULT_LABLE_CONTAINER_DATABASE_LOWERCASE = "database-sql";
	
	public final static String LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL = "graph.edge.weight.value.same.sql";
	public final static String LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_API = "graph.edge.weight.value.same.api";
	public final static String LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SCENE = "graph.edge.weight.value.same.scene";
	public final static String LABEL_GRAPH_MINCUT_THRESHOLD = "graph.mincut.threshold";
	public final static String LABEL_CONTAINER_CLASS_FUNCTION_LOWERCASE = "lable.container.class.function.lowercase";
	public final static String LABEL_CONTAINER_DATABASE_LOWERCASE = "lable.container.database.lowercase";
	public final static String LABEL_ISSHOW_NODE_INDEX = "show.node.index";
	public static final String DEFAULT_DIVIDE_MICROSERVICE_NUM = "5";
	public static final String LABEL_DIVIDE_MICROSERVICE_NUM = "divide.microservice.num";
	public final static String LABEL_IS_PRINT_RUNTIME_WARN_MESSAGE = "print.runtime.warn.message";
	public final static String LABEL_IS_SHOW_LINK_BETWEEN_SQLTABLE_NODE = "show.link.between.sqltable.node";
	public final static String LABLE_TSS_DIVIDE_ALGORITHM = "lable.tss.divide.algorithm";
	public final static String TSS_DIVIDE_ALGORITHM_STOERWAGNER = "StoerWagner";
	public final static String TSS_DIVIDE_ALGORITHM_CLUSTERKMEANS = "ClusterKMeans";
	
	private static String[] COLOR_RGB_LIST = {"FF83FA", "FF0000", "EE7AE9", "CDCD00", "BF3EFF", "B3EE3A", "A020F0", "8FBC8F", "8B6914", "636363", "2E8B57", "191970"};
	
	private static int colorIndex = -1;
	
	private static Properties divideProperties = new Properties();
	
	public static boolean isPrintRuntimeWarnMsg = false;
	
	static {
		
		try {
			 
			divideProperties.load(Object.class.getResourceAsStream("/conf/divide-traditional-software.properties"));
			
			System.out.println("LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL=" + divideProperties.getProperty(LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL));
			System.out.println("LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_API=" + divideProperties.getProperty(LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_API));
			System.out.println("LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SCENE=" + divideProperties.getProperty(LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SCENE));
			System.out.println("LABEL_GRAPH_MINCUT_THRESHOLD=" + divideProperties.getProperty(LABEL_GRAPH_MINCUT_THRESHOLD));
			System.out.println("LABEL_LABLE_CONTAINER_CLASS_FUNCTION_LOWERCASE=" + divideProperties.getProperty(LABEL_CONTAINER_CLASS_FUNCTION_LOWERCASE));
			System.out.println("LABEL_LABLE_CONTAINER_DATABASE_LOWERCASE=" + divideProperties.getProperty(LABEL_CONTAINER_DATABASE_LOWERCASE));
			
			isPrintRuntimeWarnMsg = Boolean.parseBoolean(divideProperties.getProperty(LABEL_IS_PRINT_RUNTIME_WARN_MESSAGE, "false"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static Properties getDivideProperties() {
		
		return divideProperties;
		
	}
	
	public static boolean isGraphSqlDaoNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		try {
			
			String label = node.getPayload().getAllocationComponent().toString().toLowerCase();
			
			return label.toLowerCase().indexOf(divideProperties.getProperty(LABEL_CONTAINER_CLASS_FUNCTION_LOWERCASE, DEFAULT_LABLE_CONTAINER_CLASS_FUNCTION_LOWERCASE)) != -1 && label.endsWith("dao");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return false;
		
	}
	
	public static boolean isGraphSQLTableNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		try {
			
			String label = node.getPayload().getAllocationComponent().toString().toLowerCase();
			
			if (label.indexOf(divideProperties.getProperty(LABEL_CONTAINER_DATABASE_LOWERCASE, DEFAULT_LABLE_CONTAINER_DATABASE_LOWERCASE)) != -1) {
				
				return true;
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return false;
		
	}
	
	public static boolean isGraphTopestApiFunctionNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		try {
			
			String label = node.getPayload().getAllocationComponent().toString().toLowerCase();
			
			if (label.toLowerCase().indexOf(divideProperties.getProperty(LABEL_CONTAINER_CLASS_FUNCTION_LOWERCASE, DEFAULT_LABLE_CONTAINER_CLASS_FUNCTION_LOWERCASE)) != -1) {
				
				Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> inEdges = node.getIncomingDependencies().iterator();
				
				while (inEdges.hasNext()) {
					
					DependencyGraphNode<AllocationComponentOperationPair> inNode = inEdges.next().getTarget();
					
					if (inNode.getIncomingDependencies().size() == 0) {
						
						return true;
					}
					
				}
				
			}
			
		} catch (Exception e) {
			
			
		}
		
		return false;
	}

	public static boolean isShowNodeIndex() {
		
		return Boolean.parseBoolean(divideProperties.getProperty(TSSCommonUtils.LABEL_ISSHOW_NODE_INDEX, "false"));
		
	}

	public static int getEdgeWeightValueUnderSameSQLDao() {
		
		return Integer.parseInt(divideProperties.getProperty(TSSCommonUtils.LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL, TSSCommonUtils.DEFAULT_GRAPH_EDGE_WEIGHT_VALUE_SAME_SQL));
	
	}

	public static int getEdgeWeightValueUnderSameApiFunction() {
		return Integer.parseInt(divideProperties.getProperty(TSSCommonUtils.LABEL_GRAPH_EDGE_WEIGHT_VALUE_SAME_API, TSSCommonUtils.DEFAULT_GRAPH_EDGE_WEIGHT_VALUE_SAME_API));
	}

	public static Color generateRandomColor() {
		
		colorIndex = (++colorIndex) % (COLOR_RGB_LIST.length - 1);
		
		return new Color(Integer.parseInt(COLOR_RGB_LIST[colorIndex], 16));
		
	}

	public static void printRuntimeWarnMessage(String msg) {
		
		if (isPrintRuntimeWarnMsg) {
			
			System.out.println(String.format("%s", msg));
			
		}
		
	}
	
	public static boolean isShowLinkBetweenSqltableNode() {
		
		return Boolean.parseBoolean(divideProperties.getProperty(TSSCommonUtils.LABEL_IS_SHOW_LINK_BETWEEN_SQLTABLE_NODE, "false"));
		
	}
	
	public static String getTssDivideAlgorithmName() {
		
		return divideProperties.getProperty(TSSCommonUtils.LABLE_TSS_DIVIDE_ALGORITHM, TSS_DIVIDE_ALGORITHM_CLUSTERKMEANS);
		
	}

}
