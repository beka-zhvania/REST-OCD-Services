package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.graphstream.graph.implementations.MultiNode;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;


/**
 * Implementation of the SALSA authority score.
 * See: Lempel, R. and Moran, S. 2001. SALSA: The Stochastic Approach for Link-structure Analysis.
 * @author Tobias
 *
 */
public class SalsaAuthorityScore implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.SALSA_AUTHORITY_SCORE, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		int n = graph.getNodeCount();	
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			Iterator<Node> nc = graph.iterator();
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, 0);
			}
			return res;
		}

		// Create bipartite graph
		CustomGraph bipartiteGraph = new CustomGraph();
		Node[] nodes = graph.nodes().toArray(Node[]::new);
		Edge[] edges = graph.edges().toArray(Edge[]::new);
		Map<Node, Node> hubNodeMap = new HashMap<Node, Node>();
		Map<Node, Node> authorityNodeMap = new HashMap<Node, Node>();
		Map<Node, Node> reverseAuthorityNodeMap = new HashMap<Node, Node>();
		
		// Create the nodes of the new bipartite graph
		for(Node node : nodes) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			if(node.getOutDegree() > 0) {
				Node hubNode = bipartiteGraph.addNode("hubNode" + node.getId());
				hubNodeMap.put(node, hubNode);
			}
			if(node.getInDegree() > 0) {
				Node authorityNode = bipartiteGraph.addNode("authorityNode" + node.getId());
				authorityNodeMap.put(node, authorityNode);
				reverseAuthorityNodeMap.put(authorityNode, node);
			}
		}
		
		// Add the edges of the new bipartite graph
		for(Edge edge : edges) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node oldSource = edge.getSourceNode();
			Node oldTarget = edge.getTargetNode();
			Node newSource = hubNodeMap.get(oldSource);
			Node newTarget = authorityNodeMap.get(oldTarget);
			Edge newEdge = bipartiteGraph.addEdge(UUID.randomUUID().toString(), newSource, newTarget);
			bipartiteGraph.setEdgeWeight(newEdge, graph.getEdgeWeight(edge));
		}
		
		// Construct matrix
		Matrix authorityMatrix = new CCSMatrix(n, n);	
		for(Node ia : authorityNodeMap.values()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node i = reverseAuthorityNodeMap.get(ia);
			Iterator<Node> stepOne = graph.getPredecessorNeighbours(ia).iterator();
			while(stepOne.hasNext()) {
				Node kh = stepOne.next();	
				Iterator<Node> stepTwo = graph.getSuccessorNeighbours(kh).iterator();
				while(stepTwo.hasNext()) {
					Node ja = stepTwo.next();
					Node j = reverseAuthorityNodeMap.get(ja);			
					double edgeWeightKI = bipartiteGraph.getEdgeWeight(kh.getEdgeToward(ia));
					double edgeWeightKJ = bipartiteGraph.getEdgeWeight(kh.getEdgeToward(ja));
					double weightedInDegreeI = bipartiteGraph.getWeightedInDegree(ia);
					double weightedOutDegreeK = bipartiteGraph.getWeightedOutDegree((MultiNode) kh);
					double oldAij = authorityMatrix.get(i.getIndex(), j.getIndex());
					double newAij = oldAij + (double)edgeWeightKI/weightedInDegreeI * (double)edgeWeightKJ/weightedOutDegreeK;
					authorityMatrix.set(i.getIndex(), j.getIndex(), newAij);
				}
			}
		}	
		// Calculate stationary distribution of authority Markov chain
		Vector authorityVector = MatrixOperations.calculateStationaryDistribution(authorityMatrix);
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			res.setNodeValue(node, authorityVector.get(node.getIndex()));
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		compatibleTypes.add(GraphType.WEIGHTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.SALSA_AUTHORITY_SCORE;
	}
	
	@Override
	public HashMap<String, String> getParameters() {
		return new HashMap<String, String>();
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
}
