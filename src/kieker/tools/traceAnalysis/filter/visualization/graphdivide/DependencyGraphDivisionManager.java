package kieker.tools.traceAnalysis.filter.visualization.graphdivide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.graph.Color;
import kieker.tools.traceAnalysis.filter.visualization.graph.EdgeType;
import kieker.tools.traceAnalysis.filter.visualization.graph.NoOriginRetentionPolicy;
import kieker.tools.traceAnalysis.filter.visualization.graphdivide.util.TSSCommonUtils;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

public class DependencyGraphDivisionManager {
	
	private int sameDomainBaseWeightValue = TSSCommonUtils.getEdgeWeightValueUnderSameDoamin();
	private int sameSqlDAOBaseWeightValue = TSSCommonUtils.getEdgeWeightValueUnderSameSqlDao();
	private int sameTopApiBaseWeightValue = TSSCommonUtils.getEdgeWeightValueUnderSameApiFunction();
	
	private Map<Integer, String> nodeToDomainMap = new HashMap<Integer, String>();
	private List<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> splitEdgeList = new ArrayList<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>>();
	private List<DependencyGraphNode<AllocationComponentOperationPair>> splitSqlDaoNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	private List<DependencyGraphNode<AllocationComponentOperationPair>> splitTopApiNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
	private Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> tableNodeId2TopApiListMap = new HashMap<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>>();
	
	
	public void divideDependencyGraphIntoMicoservices(List<DependencyGraphNode<AllocationComponentOperationPair>> allNodeList) {
		
		try {
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
			List<DependencyGraphNode<AllocationComponentOperationPair>> sqlDaoNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
			List<DependencyGraphNode<AllocationComponentOperationPair>> topApiNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();

			for (DependencyGraphNode<AllocationComponentOperationPair> node : allNodeList) {

				if (TSSCommonUtils.isGraphSQLTableNode(node)) {

					sqlTableNodeList.add(node);

				} else if (TSSCommonUtils.isGraphSqlDaoNode(node)) {
					
					sqlDaoNodeList.add(node);
					
				} else if (TSSCommonUtils.isGraphTopestApiFunctionNode(node)) {
					
					topApiNodeList.add(node);
					
				}

			}
			
			identifyDomainNodeMap(sqlTableNodeList);
			
			recalculateEdgeWeight4TopApiNode(topApiNodeList);
			
			recalculateEdgeWeight4SqlDaoNode(sqlDaoNodeList);

			recalculateEdgeWeight4SameDomainNode(sqlTableNodeList);

			divideDependencyGraphByAlgorithm(allNodeList, sqlTableNodeList);

			if (!TSSCommonUtils.isShowLinkBetweenSqltableNode()) {
				
				clearGraphSqlTableNodeOutgoingLinks(sqlTableNodeList);
				
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}
	
	/**
	 * DDD领域所属SqlTable Node集合识别
	 * @param sqlTableNodeList
	 */
	private void identifyDomainNodeMap(List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			String domainName = node.getPayload().getOperation().getSignature().getDomainName();
			
			int nodeId = node.getId();
			
			nodeToDomainMap.put(nodeId, domainName);
			
		}
		
	}

	private void recalculateEdgeWeight4SameDomainNode(List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		Map<String, List<DependencyGraphNode<AllocationComponentOperationPair>>> domainNodesMap = new HashMap<String, List<DependencyGraphNode<AllocationComponentOperationPair>>>();
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			String domainName = node.getPayload().getOperation().getSignature().getDomainName();
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> nodeList = domainNodesMap.get(domainName);
			
			if (nodeList == null) {
				
				nodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
			}
			
			nodeList.add(node);
			
			domainNodesMap.put(domainName, nodeList);
			
		}
		
		printDomainInfo(domainNodesMap);
		
		Iterator<String> domainIterator = domainNodesMap.keySet().iterator();
		
		while (domainIterator.hasNext()) {
			
			String domainName = domainIterator.next();
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> nodeList = domainNodesMap.get(domainName);
			
			// 同一领域，不考虑执行频率
			rebuildLinkBettweenSqlTableNode(nodeList, 0, sameDomainBaseWeightValue, null);// 同领域拆分，不需要计算代价，所以EdgeType设置为0
			
		}
		
	}

	private void printDomainInfo(Map<String, List<DependencyGraphNode<AllocationComponentOperationPair>>> moduleNodesMap) {
		
		System.out.println("--------------------Domain division information-------------------------------");
		System.out.println("Domain Num : " + moduleNodesMap.size());
		
		Iterator<String> domainIterator = moduleNodesMap.keySet().iterator();
		
		while (domainIterator.hasNext()) {
			
			String domainName = domainIterator.next();
			
			System.out.print(domainName + " : ");
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> nodeList = moduleNodesMap.get(domainName);
			
			for (DependencyGraphNode<AllocationComponentOperationPair> tmp : nodeList) {
				
				System.out.print(getName4SQLTableNode(tmp) + ", ");
				
			}
			
			System.out.println();
			
		}
		
		System.out.println("------------------------------------------------------------------------------");
	}
	
	private String getName4SQLTableNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		if (TSSCommonUtils.isGraphSQLTableNode(node)) {
			
			return node.getEntity().getOperation().getSignature().getName();
			
		}
		
		return "";
		
	}
	

	private void recalculateEdgeWeight4SqlDaoNode(List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		System.out.println("-----------------------------------Same sql dao information-----------------------------------------------------");
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			if (TSSCommonUtils.isGraphSqlDaoNode(node)) {
				
				List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = getSqlTableNodeUnderSqlDaoNode(node);
				
				printSameParentNodeSqlTableNode(getName4FuntionNode(node) , targetNodeList);
				
				int nodeOutgoingEdgeWeight = 0;//getNodeOutgoingEdgeWeight(node);//暂不考虑执行频率
				
				int maxWeightValue = rebuildLinkBettweenSqlTableNode(targetNodeList, nodeOutgoingEdgeWeight, sameSqlDAOBaseWeightValue, EdgeType.EDGE_TYPE_SQL_DAO);
				
				this.sameDomainBaseWeightValue = Math.max(this.sameDomainBaseWeightValue, maxWeightValue);
				
			}
			
		}
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		
	}

	private void printSameParentNodeSqlTableNode(String parentNodeName, List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList) {
		
		System.out.print(parentNodeName + " : ");
		for (DependencyGraphNode<AllocationComponentOperationPair> tmp : targetNodeList) {
			
			System.out.print(getName4SQLTableNode(tmp) + ", ");
			
		}
		System.out.println();
		
	}
	
	private void recalculateEdgeWeight4TopApiNode(List<DependencyGraphNode<AllocationComponentOperationPair>> topApiNodeList) {
		
		System.out.println("---------------------------------Same Top Api Information--------------------------------------");
		for (DependencyGraphNode<AllocationComponentOperationPair> node : topApiNodeList) {
			
			if (TSSCommonUtils.isGraphTopestApiFunctionNode(node)) {
				
				List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = getSqlTableNodeUnderApiNodeBFS(node);
				
				printSameParentNodeSqlTableNode(getName4FuntionNode(node) , targetNodeList);
				
				int nodeOutgoingEdgeWeight = 0;//getNodeOutgoingEdgeWeight(node);
				
				int maxWeightValue = rebuildLinkBettweenSqlTableNode(targetNodeList, nodeOutgoingEdgeWeight, sameTopApiBaseWeightValue, EdgeType.EDGE_TYPE_TRACE);
				
				this.sameSqlDAOBaseWeightValue = Math.max(this.sameSqlDAOBaseWeightValue, maxWeightValue);
				
				// 缓存Table Node节点到TopApi Node节点的Map映射关系，便于计算拆分代价时取得所需拆分的TopApi Node时使用
				for (DependencyGraphNode<AllocationComponentOperationPair> tmp2 : targetNodeList) {
					
					List<DependencyGraphNode<AllocationComponentOperationPair>> nodeList = tableNodeId2TopApiListMap.get(tmp2.getId());
					
					if (nodeList == null) {
						
						nodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
						
					}
					
					if (!nodeList.contains(node)) {
						
						nodeList.add(node);
						
					}
					
					tableNodeId2TopApiListMap.put(tmp2.getId(), nodeList);
					
				}
				
			}
			
		}
		System.out.println("-----------------------------------------------------------------------------------------------");
		
	}
	
	private List<DependencyGraphNode<AllocationComponentOperationPair>> getSqlTableNodeUnderApiNodeBFS(DependencyGraphNode<AllocationComponentOperationPair> apiNode) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		LinkedList<DependencyGraphNode<AllocationComponentOperationPair>> queue = new LinkedList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		queue.offer(apiNode);
		
		while (!queue.isEmpty()) {
			
			DependencyGraphNode<AllocationComponentOperationPair> node = queue.poll();
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node.getOutgoingEdges().iterator();
			
			while (outEdgeIterator.hasNext()) {
				
				DependencyGraphNode<AllocationComponentOperationPair> outNode = outEdgeIterator.next().getTarget();
   			 
				if (outNode.getId() != node.getId()) {
					
					if (TSSCommonUtils.isGraphSqlDaoNode(outNode)) {
						
						addNotExistSqlTableNode(targetNodeList, getSqlTableNodeUnderSqlDaoNode(outNode));
						
					} else {
						
						queue.offer(outNode);
						
					}
					
				}
   			 
   		 	}
			
		}
		
		return targetNodeList;
		
	}


	private void addNotExistSqlTableNode(List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList,
												List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			if (!targetNodeList.contains(node)) {
				
				targetNodeList.add(node);
				
			}
			
		}
		
	}

	private List<DependencyGraphNode<AllocationComponentOperationPair>> getSqlTableNodeUnderSqlDaoNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node.getOutgoingEdges().iterator();
		
		while (outEdgeIterator.hasNext()) {
			
			DependencyGraphNode<AllocationComponentOperationPair> outNode = outEdgeIterator.next().getTarget();
			
			if (TSSCommonUtils.isGraphSQLTableNode(outNode) && outNode.getId() != node.getId()) {
				
				targetNodeList.add(outNode);
				
			}
			
		}
		
		return targetNodeList;
		
	}
	
	private int rebuildLinkBettweenSqlTableNode(List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList, 
													int operateFrequency, int weightIncreaseStep, EdgeType edgeType) {
		
		int maxWeightValue = 0;
		
		Collections.sort(targetNodeList, new Comparator<DependencyGraphNode<AllocationComponentOperationPair>>() {

			@Override
			public int compare(DependencyGraphNode<AllocationComponentOperationPair> o1,
					DependencyGraphNode<AllocationComponentOperationPair> o2) {
				return o1.getId() >= o2.getId() ? 1 : -1;
			}
			
		});
		
		for (int i = 0; i < targetNodeList.size(); i++) {
			
			DependencyGraphNode<AllocationComponentOperationPair> node1 = targetNodeList.get(i);
			
			for (int j = i + 1; j < targetNodeList.size(); j++) {
				
				DependencyGraphNode<AllocationComponentOperationPair> node2 = targetNodeList.get(j);
				
				if (node1.getId() != node2.getId()) {
					
					int previousWeightValue = getPreviousEdgeWeightValue(node1, node2);
					
					int newWeightValue = calculateNewWeightValue(node1, node2, previousWeightValue, operateFrequency, weightIncreaseStep);
					
					maxWeightValue = Math.max(maxWeightValue, newWeightValue);
					
					if (previousWeightValue > 0) {
						
						updateEdgeWeightValue(node1, node2, newWeightValue, edgeType);
						
					} else if (previousWeightValue < 0) {
						
						updateEdgeWeightValue(node2, node1, newWeightValue, edgeType);
						
					} else {
						
						addDependencyLinkBetweenTwoNode(node1, node2, newWeightValue, edgeType);
						
					}
					
				}
				
			}
			
		}
		
		return maxWeightValue;
			
	}

	/**
	 * 跨域domain边的权值为0，微服务划分，优先按照同域
	 * @param node1
	 * @param node2
	 * @param previousWeightValue
	 * @param operateFrequency
	 * @param weightIncreaseStep
	 * @return
	 */
	private int calculateNewWeightValue(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2, 
			int previousWeightValue, int operateFrequency, int weightIncreaseStep) {
		
		String node1Domain = nodeToDomainMap.get(node1.getId());
		
		String node2Domain = nodeToDomainMap.get(node2.getId());
		
		if (node1Domain != null && node2Domain != null && node1Domain.equalsIgnoreCase(node2Domain)) {
			
			return weightIncreaseStep + operateFrequency + Math.abs(previousWeightValue);
			
		} else {
			
			return 0;
			
		}
		
	}

	private void addDependencyLinkBetweenTwoNode(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2, 
													int newWeightValue, EdgeType edgeType) {
		
		if (node1.getId() < node2.getId()) {
			
			node1.addOutgoingDependencyByFudan(node2, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue, edgeType);
			
			node2.addIncomingDependencyByFudan(node1, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue, edgeType);
			
		} else {
			
			node2.addOutgoingDependencyByFudan(node1, isDependencyAssumed(node2, node1), null, NoOriginRetentionPolicy.createInstance(), newWeightValue, edgeType);
			
			node1.addIncomingDependencyByFudan(node2, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue, edgeType);
			
		}
		
	}
	
	private void updateEdgeWeightValue(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2, 
										int newWeightValue, EdgeType edgeType) {
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node1.getOutgoingDependencies().iterator();
		
		while (outEdgeIterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgeIterator.next();
			
			if (outEdge.getTarget().getId() == node2.getId()) {
				
				outEdge.getTargetWeight().set(newWeightValue);
				
				// 设置边类型和次数，用户统计拆分代价，即需要拆分多少个SQL、多少个DAO、多少个Trace Method
				if (edgeType != null) {
					
					if (outEdge.getEdgeTypeComponent().getEdgeType() == null) {
						
						outEdge.getEdgeTypeComponent().setEdgeType(edgeType);
						
						outEdge.getEdgeTypeComponent().getConnectTimes().set(1);
						
					} else if (outEdge.getEdgeTypeComponent().getEdgeType().compareTo(edgeType) < 0) {
						
						outEdge.getEdgeTypeComponent().setEdgeType(edgeType);
						
						outEdge.getEdgeTypeComponent().getConnectTimes().set(1);
						
					} else if (outEdge.getEdgeTypeComponent().getEdgeType().compareTo(edgeType) == 0) {
						
						outEdge.getEdgeTypeComponent().getConnectTimes().incrementAndGet();
						
					}
					
				}
				
				return;
				
			}
			
		}
		
		TSSCommonUtils.printlnRuntimeWarnMessage("Failed to update edge weight value, please check it.");
		
	}

	private int getPreviousEdgeWeightValue(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2) {
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node1.getOutgoingEdges().iterator();
		
		while (outEdgeIterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgeIterator.next();
			
			if (outEdge.getTarget().getId() == node2.getId()) {
				
				return outEdge.getTargetWeight().get();
				
			}
			
		}
		
		outEdgeIterator = node2.getOutgoingEdges().iterator();
		
		while (outEdgeIterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgeIterator.next();
			
			if (outEdge.getTarget().getId() == node1.getId()) {
				
				TSSCommonUtils.printlnRuntimeWarnMessage("There is a link in the oppsite direction, please check it: node1=" + node1.getId() + ", node2=" + node2.getId());
				
				return Math.negateExact(outEdge.getTargetWeight().get());
				
			}
			
		}
		
		return 0;
	
	}
	
	private int getNodeOutgoingEdgeWeight(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> edges = node.getOutgoingEdges().iterator();
		
		while (edges.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge = edges.next();
			
			DependencyGraphNode<AllocationComponentOperationPair> outNode = edge.getTarget();
			 
			if (node.getId() != outNode.getId()) {
				
				return edge.getTargetWeight().get();
				
			}
			
		}
		
		return 0;
		
	}

	protected boolean isDependencyAssumed(final DependencyGraphNode<?> source, final DependencyGraphNode<?> target) {
		
		return source.isAssumed() || target.isAssumed();
		
	}
	
	public void divideDependencyGraphByAlgorithm(List<DependencyGraphNode<AllocationComponentOperationPair>> allNodeList, 
		 List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		AbstractGraphDivisionAlgorithmExecutor graphDivisionExecutor = GraphDivisionAlgorithmExecutorFactory.getGraphDivisionAlgorithmExecutor();
		
		if (graphDivisionExecutor != null) {
			
			Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsListMap = new HashMap<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>>();
			
			graphDivisionExecutor.execute(allNodeList, sqlTableNodeList, divideRsListMap);
			
			TSSCommonUtils.printlnRuntimeWarnMessage("----------------------------Microservice Split Result:-------------------------");
			
			Iterator<Integer> iterator = divideRsListMap.keySet().iterator();
			
			while (iterator.hasNext()) {
				
				int subGraghNo = iterator.next();
				
				List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList = divideRsListMap.get(subGraghNo);
				
				updateGraphNodeColorAndNodeQualifiedName(subNodeList, subGraghNo);
				
				printMicroServiceInformation(subNodeList, subGraghNo);
				
			}
			
			System.out.println();
			
			statisticDivisionCost(sqlTableNodeList, divideRsListMap);
			
			TSSCommonUtils.printlnRuntimeWarnMessage("----------------------------Microservice Split Cost Information:-------------------------");
			
			printDivisionCost();
			
		}
		 
	 }

	private void printDivisionCost() {
		
		int splitSqlDaoNodeCount = 0;
		
		int splitTopApiNodeCount = 0;
		
		int splitSameSqlEdgeCount = 0;
		
		int splitSameTraceEdgeCount = 0;
		
		for(DependencyGraphNode<AllocationComponentOperationPair> sqlDaoNode : splitSqlDaoNodeList) {
			
			splitSqlDaoNodeCount++;
			
			String node1Name = getName4FuntionNode(sqlDaoNode);
			
			TSSCommonUtils.printlnRuntimeWarnMessage("Split Dao(Sql) Node Quantity : " + node1Name);
			
		}
		
		for(DependencyGraphNode<AllocationComponentOperationPair> sqlDaoNode : splitTopApiNodeList) {
			
			splitTopApiNodeCount++;
			
			String node1Name = getName4FuntionNode(sqlDaoNode);
			
			TSSCommonUtils.printlnRuntimeWarnMessage("Split TopApi Node Quantity : " + node1Name);
			
		}
		
		for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge : splitEdgeList) {
			
			if (edge.getEdgeTypeComponent().getEdgeType() != null) {
				
				if (EdgeType.EDGE_TYPE_TRACE.compareTo(edge.getEdgeTypeComponent().getEdgeType()) == 0) {
					
					splitSameTraceEdgeCount++;
					
				} else if (EdgeType.EDGE_TYPE_SQL_DAO.compareTo(edge.getEdgeTypeComponent().getEdgeType()) == 0) {
					
					splitSameSqlEdgeCount++;
					
				}
				
			}
			
		}
		
		TSSCommonUtils.printlnRuntimeWarnMessage("Microservice split cost statistic :   Split_Sql_Dao_Node_Quantity=" + splitSqlDaoNodeCount + ", Split_Top_Api_Node_Quantity=" + splitTopApiNodeCount + ", Split_Same_Sql_Edge_Quantity: " + splitSameSqlEdgeCount + ", Split_Same_Trace_Edge_Quantity=" + splitSameTraceEdgeCount);
		
	}

	private void statisticDivisionCost(List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList, Map<Integer, List<DependencyGraphNode<AllocationComponentOperationPair>>> divideRsListMap) {
		
		Map<Integer, Integer> nodeId2GroupIdMap = new HashMap<Integer, Integer>();
		
		Iterator<Integer> iterator = divideRsListMap.keySet().iterator();
		
		while (iterator.hasNext()) {
			
			int subGraghNo = iterator.next();
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList = divideRsListMap.get(subGraghNo);
			
			for (DependencyGraphNode<AllocationComponentOperationPair> node : subNodeList) {
				
				nodeId2GroupIdMap.put(node.getId(), subGraghNo);
				
			}
			
		}
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> edges = node.getOutgoingEdges().iterator();
			
			while (edges.hasNext()) {
				
				WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge = edges.next();
				
				DependencyGraphNode<AllocationComponentOperationPair> outNode = edge.getTarget();
				
				// 所属不同分组，被切割的边，计入代价
				if (nodeId2GroupIdMap.get(node.getId()) != nodeId2GroupIdMap.get(outNode.getId())) {
					
					splitEdgeList.add(edge);
					
					EdgeType edgeType = edge.getEdgeTypeComponent().getEdgeType();
					
					if (edgeType != null) {
						
						// 待拆除的DAO边
						if (edgeType.compareTo(EdgeType.EDGE_TYPE_SQL_DAO) == 0) {
							
							List<DependencyGraphNode<AllocationComponentOperationPair>> parentDaoNodeList = getParentSqlDaoNode(edge);
							
							for (DependencyGraphNode<AllocationComponentOperationPair> parentNode : parentDaoNodeList) {
								
								if (!splitSqlDaoNodeList.contains(parentNode)) {
									
									splitSqlDaoNodeList.add(parentNode);
									
								}
								
							}
							
						// 同Trace边
						} else if (edgeType.compareTo(EdgeType.EDGE_TYPE_TRACE) == 0) {
							
							List<DependencyGraphNode<AllocationComponentOperationPair>> parentTopApiNodeList = getParentTopApiFunctionNode(edge);
							
							for (DependencyGraphNode<AllocationComponentOperationPair> parentNode : parentTopApiNodeList) {
								
								if (!splitTopApiNodeList.contains(parentNode)) {
									
									splitTopApiNodeList.add(parentNode);
									
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}

	/**
	 * 获取边的公共Function DAO节点
	 * @param edge
	 * @return
	 */
	private List<DependencyGraphNode<AllocationComponentOperationPair>> getParentSqlDaoNode(WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> rsNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		List<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> targetNodeInEdgeList = new ArrayList<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>>();
		List<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> sourceNodeInEdgeList = new ArrayList<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>>();
		
		targetNodeInEdgeList.addAll(edge.getTarget().getIncomingDependencies());
		sourceNodeInEdgeList.addAll(edge.getSource().getIncomingDependencies());
		
		for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> tEdge : targetNodeInEdgeList) {
			
			if (!TSSCommonUtils.isGraphSqlDaoNode(tEdge.getTarget())) {
				
				continue;
				
			}
			
			int node1Id = tEdge.getTarget().getId();
			
			for (WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> sEdge : sourceNodeInEdgeList) {
				
				if (!TSSCommonUtils.isGraphSqlDaoNode(sEdge.getTarget())) {
					
					continue;
					
				}
				
				int node2Id = sEdge.getTarget().getId();
				
				if (node1Id == node2Id) {
					
					rsNodeList.add(tEdge.getTarget());
					
				}
				
			}
			
		}
		
		return rsNodeList;
		
	}
	
	/**
	 * 获取边的公共顶层API节点
	 * @param edge
	 * @return
	 */
	private List<DependencyGraphNode<AllocationComponentOperationPair>> getParentTopApiFunctionNode(WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> rsNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeTopApiList = tableNodeId2TopApiListMap.get(edge.getTarget().getId());
		List<DependencyGraphNode<AllocationComponentOperationPair>> sourceNodeTopApiList = tableNodeId2TopApiListMap.get(edge.getSource().getId());
		
		for (DependencyGraphNode<AllocationComponentOperationPair> tNode : targetNodeTopApiList) {
			
			if (!TSSCommonUtils.isGraphTopestApiFunctionNode(tNode)) {
				
				continue;
				
			}
			
			int node1Id = tNode.getId();
			
			for (DependencyGraphNode<AllocationComponentOperationPair> sNode : sourceNodeTopApiList) {
				
				if (!TSSCommonUtils.isGraphTopestApiFunctionNode(sNode)) {
					
					continue;
					
				}
				
				int node2Id = sNode.getId();
				
				if (node1Id == node2Id) {
					
					rsNodeList.add(tNode);
					
				}
				
			}
			
		}
		
		return rsNodeList;
		
	}

	private void printMicroServiceInformation(List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList, int microServiceNo) {
		
		TSSCommonUtils.printRuntimeWarnMessage("微系统" + microServiceNo + " : ");
		
		int totalWeightForMicroService = 0;
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : subNodeList) {
			
			String signature = node.getEntity().getOperation().getSignature().toString();
			
			int sumWeightToAllOtherNodes = node.getEntity().getOperation().getSignature().getSumWeightToAllOtherNodes();
			
			totalWeightForMicroService += sumWeightToAllOtherNodes;
			
	   		TSSCommonUtils.printRuntimeWarnMessage(signature.substring(0, signature.lastIndexOf("(")) + "(" + sumWeightToAllOtherNodes + ")" + ", ");
	   		
		}
		
		TSSCommonUtils.printlnRuntimeWarnMessage(totalWeightForMicroService + ", ");
		
	}
	
	private String getName4FuntionNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		String signature = node.getEntity().getOperation().getSignature().getName();
		
		String className = node.getEntity().getOperation().getComponentType().getTypeName();
		
		return className + "." + signature;
		
	}

	private void updateGraphNodeColorAndNodeQualifiedName(List<DependencyGraphNode<AllocationComponentOperationPair>> subNodeList, int microServiceNo) {
		
    	Color tmpAColor = TSSCommonUtils.generateRandomColor();
    	 
    	for (DependencyGraphNode<AllocationComponentOperationPair> node : subNodeList) {
    		 
    		node.setColor(tmpAColor);
    		 
    		updateGraphNodeInDependencyNodeColor(node, tmpAColor);
    		 
    		updateGrapNodeQualifiedName(microServiceNo, node);
    		 
    	}
    	 
	}

	private void updateGrapNodeQualifiedName(int microServiceNo, DependencyGraphNode<AllocationComponentOperationPair> node) {
		ComponentType oldComponentType = node.getEntity().getAllocationComponent().getAssemblyComponent().getType();
		
		AssemblyComponent oldAssemblyComponent = node.getEntity().getAllocationComponent().getAssemblyComponent();
		
		AllocationComponent oldAllocationComponent = node.getEntity().getAllocationComponent();
		
		String fullQualifiedName = TSSCommonUtils.getSqlTableNodePackageNode() + "." + microServiceNo;
		
		ComponentType newComponentType = new ComponentType(oldComponentType.getId(), fullQualifiedName, oldAssemblyComponent.getType().getModuleName());
		
		AssemblyComponent newAssemblyComponent = new AssemblyComponent(oldAssemblyComponent.getId(), oldAssemblyComponent.getName(), newComponentType);
		
		AllocationComponent newAllocationComponent = new AllocationComponent(oldAllocationComponent.getId() + 10000 + microServiceNo, newAssemblyComponent, oldAllocationComponent.getExecutionContainer());
		
		node.getEntity().setAllocationComponent(newAllocationComponent);
		
	}
	
	private void updateGraphNodeInDependencyNodeColor(DependencyGraphNode<AllocationComponentOperationPair> node, Color tmpAColor) {
		
		if (node == null || node.getIncomingDependencies() == null || node.getIncomingDependencies().size() < 1) {
			
			return;
			
		}
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> inEdgesIterator = node.getIncomingDependencies().iterator();
		
		while (inEdgesIterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> inEdge = inEdgesIterator.next();
			
			if (inEdge.getSource().getId() == inEdge.getTarget().getId() || TSSCommonUtils.isGraphSQLTableNode(inEdge.getTarget())) {
				
				continue;
				
			}
			
			inEdge.setColor(tmpAColor);
			
			if (inEdge.getTarget() != null) {
				
				inEdge.getTarget().setColor(tmpAColor);
				
			}
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgesIterator = inEdge.getTarget().getOutgoingEdges().iterator();
			
			while (outEdgesIterator.hasNext()) {
				
				WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgesIterator.next();
				
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
	

	private void clearGraphSqlTableNodeOutgoingLinks(List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			node.getOutgoingDependencies().clear();
			
			node.getAssumedOutgoingDependencies().clear();
			
		}
	}
	
}
