package kieker.tools.traceAnalysis.filter.visualization.graph;

import java.util.concurrent.atomic.AtomicInteger;

public class EdgeTypeComponent {
	
	private AtomicInteger connectTimes = new AtomicInteger();
	
	private EdgeType edgeType;

	public AtomicInteger getConnectTimes() {
		return connectTimes;
	}

	public void setConnectTimes(AtomicInteger connectTimes) {
		this.connectTimes = connectTimes;
	}

	public EdgeType getEdgeType() {
		return edgeType;
	}

	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}
	
}
