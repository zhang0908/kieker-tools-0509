/***************************************************************************
 * Copyright 2017 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.traceAnalysis.filter.visualization;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAllocationDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAllocationDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAssemblyDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ComponentAssemblyDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ContainerDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.ContainerDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.DependencyGraphNode;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAllocationDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAllocationDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAssemblyDependencyGraph;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.OperationAssemblyDependencyGraphFormatter;
import kieker.tools.traceAnalysis.filter.visualization.dependencyGraph.WeightedBidirectionalDependencyGraphEdge;
import kieker.tools.traceAnalysis.filter.visualization.exception.GraphFormattingException;
import kieker.tools.traceAnalysis.filter.visualization.graph.AbstractGraph;
import kieker.tools.traceAnalysis.filter.visualization.graph.NoOriginRetentionPolicy;
import kieker.tools.traceAnalysis.filter.visualization.util.dot.TSSCommonUtils;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

/**
 * Generic graph writer plugin to generate graph specifications and save them to disk. This plugin uses
 * a formatter registry (see {@link #FORMATTER_REGISTRY}) to determine the appropriate formatter for a
 * given graph.
 * 
 * @author Holger Knoche
 * 
 * @since 1.6
 */
@Plugin(name = "Graph writer plugin",
		description = "Generic plugin for writing graphs to files",
		configuration = {
			@Property(name = GraphWriterPlugin.CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS, defaultValue = "true"),
			@Property(name = GraphWriterPlugin.CONFIG_PROPERTY_NAME_SHORTLABELS, defaultValue = "true"),
			@Property(name = GraphWriterPlugin.CONFIG_PROPERTY_NAME_SELFLOOPS, defaultValue = "false")
		})
public class GraphWriterPlugin extends AbstractFilterPlugin {

	/**
	 * Name of the configuration property containing the output file name.
	 */
	public static final String CONFIG_PROPERTY_NAME_OUTPUT_FILE_NAME = "dotOutputFn";
	/**
	 * Name of the configuration property containing the output path name.
	 */
	public static final String CONFIG_PROPERTY_NAME_OUTPUT_PATH_NAME = "outputPath";
	/**
	 * Name of the configuration property indicating that weights should be included.
	 */
	public static final String CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS = "includeWeights";
	/**
	 * Name of the configuration property indicating that short labels should be used.
	 */
	public static final String CONFIG_PROPERTY_NAME_SHORTLABELS = "shortLabels";
	/**
	 * Name of the configuration property indicating that self-loops should be displayed.
	 */
	public static final String CONFIG_PROPERTY_NAME_SELFLOOPS = "selfLoops";
	/**
	 * Name of the plugin's graph input port.
	 */
	public static final String INPUT_PORT_NAME_GRAPHS = "inputGraph";

	private static final String ENCODING = "UTF-8";

	private static final String NO_SUITABLE_FORMATTER_MESSAGE_TEMPLATE = "No formatter type defined for graph type %s.";
	private static final String INSTANTIATION_ERROR_MESSAGE_TEMPLATE = "Could not instantiate formatter type %s for graph type %s.";
	private static final String WRITE_ERROR_MESSAGE_TEMPLATE = "Graph could not be written to file %s.";

	private static final ConcurrentMap<Class<? extends AbstractGraph<?, ?, ?>>, Class<? extends AbstractGraphFormatter<?>>> FORMATTER_REGISTRY =
			new ConcurrentHashMap<Class<? extends AbstractGraph<?, ?, ?>>, Class<? extends AbstractGraphFormatter<?>>>();

	private final String outputPathName;
	private final String outputFileName;
	private final boolean includeWeights;
	private final boolean useShortLabels;
	private final boolean plotLoops;

	static {
		FORMATTER_REGISTRY.put(ComponentAllocationDependencyGraph.class, ComponentAllocationDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(ComponentAssemblyDependencyGraph.class, ComponentAssemblyDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(OperationAllocationDependencyGraph.class, OperationAllocationDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(OperationAssemblyDependencyGraph.class, OperationAssemblyDependencyGraphFormatter.class);
		FORMATTER_REGISTRY.put(ContainerDependencyGraph.class, ContainerDependencyGraphFormatter.class);
	}

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public GraphWriterPlugin(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);

		this.outputPathName = configuration.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_PATH_NAME);
		this.outputFileName = configuration.getPathProperty(CONFIG_PROPERTY_NAME_OUTPUT_FILE_NAME);
		this.includeWeights = configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS);
		this.useShortLabels = configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_SHORTLABELS);
		this.plotLoops = configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_SELFLOOPS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();
		configuration.setProperty(CONFIG_PROPERTY_NAME_OUTPUT_PATH_NAME, this.outputPathName);
		configuration.setProperty(CONFIG_PROPERTY_NAME_OUTPUT_FILE_NAME, this.outputFileName);
		configuration.setProperty(CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS, String.valueOf(this.includeWeights));
		configuration.setProperty(CONFIG_PROPERTY_NAME_SHORTLABELS, String.valueOf(this.useShortLabels));
		configuration.setProperty(CONFIG_PROPERTY_NAME_SELFLOOPS, String.valueOf(this.plotLoops));
		return configuration;
	}

	private static void handleInstantiationException(final Class<?> graphClass, final Class<?> formatterClass, final Exception exception) {
		throw new GraphFormattingException(String.format(INSTANTIATION_ERROR_MESSAGE_TEMPLATE, formatterClass.getName(), graphClass.getName()), exception);
	}

	private static AbstractGraphFormatter<?> createFormatter(final AbstractGraph<?, ?, ?> graph) {
		final Class<? extends AbstractGraphFormatter<?>> formatterClass = FORMATTER_REGISTRY.get(graph.getClass());

		if (formatterClass == null) {
			throw new GraphFormattingException(String.format(NO_SUITABLE_FORMATTER_MESSAGE_TEMPLATE, graph.getClass().getName()));
		}

		try {
			final Constructor<? extends AbstractGraphFormatter<?>> constructor = formatterClass.getConstructor();
			return constructor.newInstance();
		} catch (final SecurityException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final NoSuchMethodException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final IllegalArgumentException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final InstantiationException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final IllegalAccessException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		} catch (final InvocationTargetException e) {
			GraphWriterPlugin.handleInstantiationException(graph.getClass(), formatterClass, e);
		}

		// This should never happen, because all catch clauses indirectly throw exceptions
		return null;
	}

	private String getOutputFileName(final AbstractGraphFormatter<?> formatter) {
		if (this.outputFileName.length() == 0) { // outputFileName cannot be null
			return formatter.getDefaultFileName();
		} else {
			return this.outputFileName;
		}
	}

	/**
	 * Formats a given graph and saves the generated specification to disk. The file name to save the output to is specified by a the configuration options
	 * {@link #CONFIG_PROPERTY_NAME_OUTPUT_PATH_NAME} and {@link #CONFIG_PROPERTY_NAME_OUTPUT_FILE_NAME}.
	 * 
	 * @param graph
	 *            The graph to save
	 */
	@InputPort(name = INPUT_PORT_NAME_GRAPHS, eventTypes = { AbstractGraph.class })
	public void writeGraph(final AbstractGraph<?, ?, ?> graph) {
		final AbstractGraphFormatter<?> graphFormatter = GraphWriterPlugin.createFormatter(graph);
		
		divideGraphForMicoservice(graph);
		
		final String specification = graphFormatter.createFormattedRepresentation(graph, this.includeWeights, this.useShortLabels, this.plotLoops);
		final String fileName = this.outputPathName + this.getOutputFileName(graphFormatter);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), ENCODING));
			writer.write(specification);
			writer.flush();
		} catch (final IOException e) {
			throw new GraphFormattingException(String.format(WRITE_ERROR_MESSAGE_TEMPLATE, fileName), e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					this.log.error(String.format(WRITE_ERROR_MESSAGE_TEMPLATE, fileName), e);
				}
			}
		}
	}
	
	private void divideGraphForMicoservice(final AbstractGraph<?, ?, ?> graph) {
		
		try {
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> allNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> cutNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		     
		     Iterator<DependencyGraphNode<AllocationComponentOperationPair>> iterator = ((OperationAllocationDependencyGraph)graph).getVertices().iterator();
		     
		     iterator.next();// skip the entry node
		     while (iterator.hasNext()) {
		    	 
		    	 DependencyGraphNode<AllocationComponentOperationPair> node = iterator.next();
		    	 
		    	 doWithWeightForGraphNode(node);
		    	 
		    	 if (TSSCommonUtils.isGraphSQLTableNode(node)) {
		    		 
		    		 cutNodeList.add(node);
		    		 
		    	 }
		    	 
		    	 allNodeList.add(node);
		    	 
		     }
			
			new DependencyGraphDivision().divideDependencyGraphForMicoservice(allNodeList, cutNodeList, 
					Integer.parseInt(TSSCommonUtils.getDivideProperties().getProperty(TSSCommonUtils.LABEL_GRAPH_MINCUT_THRESHOLD, TSSCommonUtils.DEFAULT_GRAPH_MINCUT_THRESHOLD)));
			
			for (DependencyGraphNode<AllocationComponentOperationPair> node : allNodeList) {
				
				if (TSSCommonUtils.isGraphSQLTableNode(node) && !TSSCommonUtils.isShowLinkBetweenSqltableNode()) {
					
					node.getOutgoingDependencies().clear();
					
					node.getAssumedOutgoingDependencies().clear();
					
				}
				
			}
				
		} catch (Exception e) {
			
			System.out.println(e);
			
		}
		
	}
	
	private void doWithWeightForGraphNode(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		if (TSSCommonUtils.isGraphSqlDaoNode(node)) {
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = getSQLNodeUnderSQLDAOLevel(node);
			
			int previousWeightValue = getGraphEdgeWeight(node);
			
			buildConnectionBettweenSqlTableNode(targetNodeList, previousWeightValue, TSSCommonUtils.getEdgeWeightValueUnderSameSQLDao());
			
		} else if (TSSCommonUtils.isGraphTopestApiFunctionNode(node)) {
			
			List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = getSQLNodeUnderApiFunctionLevelByBFS(node);
			
			int previousWeightValue = getGraphEdgeWeight(node);
			
			buildConnectionBettweenSqlTableNode(targetNodeList, previousWeightValue, TSSCommonUtils.getEdgeWeightValueUnderSameApiFunction());
			
		}
		
	}
	
	
	
	private List<DependencyGraphNode<AllocationComponentOperationPair>> getSQLNodeUnderApiFunctionLevelByBFS(DependencyGraphNode<AllocationComponentOperationPair> rootNode) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		LinkedList<DependencyGraphNode<AllocationComponentOperationPair>> queue = new LinkedList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		queue.offer(rootNode);
		
		while (!queue.isEmpty()) {
			
			DependencyGraphNode<AllocationComponentOperationPair> node = queue.poll();
			
			Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = node.getOutgoingEdges().iterator();
			
			while (outEdges.hasNext()) {
				
				DependencyGraphNode<AllocationComponentOperationPair> outNode = outEdges.next().getTarget();
   			 
				if (outNode.getId() != node.getId()) {
					
					if (TSSCommonUtils.isGraphSqlDaoNode(outNode)) {
						
						addInExistTargetSqlTableNode(targetNodeList, getSQLNodeUnderSQLDAOLevel(outNode));
						
					} else {
						
						queue.offer(outNode);
						
					}
					
				}
   			 
   		 	}
			
		}
		
		return targetNodeList;
		
	}


	private void addInExistTargetSqlTableNode(List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList,
												List<DependencyGraphNode<AllocationComponentOperationPair>> sqlTableNodeList) {
		
		for (DependencyGraphNode<AllocationComponentOperationPair> node : sqlTableNodeList) {
			
			if (!targetNodeList.contains(node)) {
				
				targetNodeList.add(node);
				
			}
			
		}
		
	}

	private List<DependencyGraphNode<AllocationComponentOperationPair>> getSQLNodeUnderSQLDAOLevel(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
		List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList = new ArrayList<DependencyGraphNode<AllocationComponentOperationPair>>();
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdges = node.getOutgoingEdges().iterator();
		
		while (outEdges.hasNext()) {
			
			DependencyGraphNode<AllocationComponentOperationPair> outNode = outEdges.next().getTarget();
			
			if (TSSCommonUtils.isGraphSQLTableNode(outNode) && outNode.getId() != node.getId()) {
				
				targetNodeList.add(outNode);
				
			}
			
		}
		
		return targetNodeList;
		
	}
	
	private void buildConnectionBettweenSqlTableNode(List<DependencyGraphNode<AllocationComponentOperationPair>> targetNodeList, 
														int operateFrequency, int weightIncreaseStep) {
		
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
					
					int previousWeightValue = getPreviousWeightValue(node1, node2);
					
					int newWeightValue = weightIncreaseStep + operateFrequency + Math.abs(previousWeightValue);
					
					if (previousWeightValue > 0) {
						
						updateEdgeWeightValue(node1, node2, newWeightValue);
						
					} else if (previousWeightValue < 0) {
						
						updateEdgeWeightValue(node2, node1, newWeightValue);
						
					} else {
						
						addDependencyBetweenTwoNode(node1, node2, newWeightValue);
						
					}
					
				}
				
			}
		}
			
	}

	private void addDependencyBetweenTwoNode(DependencyGraphNode<AllocationComponentOperationPair> node1,
			DependencyGraphNode<AllocationComponentOperationPair> node2, int newWeightValue) {
		if (node1.getId() < node2.getId()) {
			
			node1.addOutgoingDependencyByFudan(node2, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue);
			
			node2.addIncomingDependencyByFudan(node1, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue);
			
		} else {
			
			node2.addOutgoingDependencyByFudan(node1, isDependencyAssumed(node2, node1), null, NoOriginRetentionPolicy.createInstance(), newWeightValue);
			
			node1.addIncomingDependencyByFudan(node2, isDependencyAssumed(node1, node2), null, NoOriginRetentionPolicy.createInstance(), newWeightValue);
			
		}
	}
	
	private void updateEdgeWeightValue(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2, int newWeightValue) {
		
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> outEdgeIterator = node1.getOutgoingDependencies().iterator();
		
		while (outEdgeIterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> outEdge = outEdgeIterator.next();
			
			if (outEdge.getTarget().getId() == node2.getId()) {
				
				outEdge.getTargetWeight().set(newWeightValue);
				
				return;
				
			}
			
		}
		
		TSSCommonUtils.printRuntimeWarnMessage("Failed to update edge weight value, please check it.");
		
	}

	private int getPreviousWeightValue(DependencyGraphNode<AllocationComponentOperationPair> node1, DependencyGraphNode<AllocationComponentOperationPair> node2) {
		
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
				
				TSSCommonUtils.printRuntimeWarnMessage("There is a link in the oppsite direction, please check it: node1=" + node1.getId() + ", node2=" + node2.getId());
				
				return Math.negateExact(outEdge.getTargetWeight().get());
				
			}
			
		}
		
		return 0;
	
	}
	
	public static void main(String[] agrs) {
		
		
		int aa = 100;
		
		int bb = Math.negateExact(aa);
		
		System.out.println(aa);
		
		System.out.println(bb);
		
	}

	private void removeSelfNode(DependencyGraphNode<AllocationComponentOperationPair> node1) {
		Iterator<WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair>> iterator = node1.getOutgoingDependencies().iterator();
		
		while (iterator.hasNext()) {
			
			WeightedBidirectionalDependencyGraphEdge<AllocationComponentOperationPair> edge = iterator.next();
			
			if (node1.getId() == edge.getTarget().getId()) {
				
				node1.getOutgoingDependencies().remove(edge);
				
			}
			
		}
	}
	
	private int getGraphEdgeWeight(DependencyGraphNode<AllocationComponentOperationPair> node) {
		
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
	
	
}
