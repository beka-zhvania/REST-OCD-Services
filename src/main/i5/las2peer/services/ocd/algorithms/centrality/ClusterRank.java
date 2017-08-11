package i5.las2peer.services.ocd.algorithms.centrality;

import java.util.HashSet;
import java.util.Set;

import i5.las2peer.services.ocd.graphs.CentralityCreationLog;
import i5.las2peer.services.ocd.graphs.CentralityCreationType;
import i5.las2peer.services.ocd.graphs.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

public class ClusterRank implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) {
		NodeCursor nc = graph.nodes();
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityCreationType.CLUSTER_RANK, this.compatibleGraphTypes()));	
		
		while(nc.ok()) {
			Node node = nc.node();
			
			//Calculate clustering coefficient
			int maxEdges = node.successors().size() * (node.successors().size() - 1);
			int edgeCount = 0;
			double clusteringCoefficient = 0;
			
			if(maxEdges == 0)
				clusteringCoefficient = 0;
			
			else {
				Set<Node> outNeighborSet = new HashSet<Node>();
				NodeCursor successors = node.successors();
				
				
				while(successors.ok()) {
					outNeighborSet.add(successors.node());
					successors.next();
				}
				
				for(Node j : outNeighborSet) {
					EdgeCursor edges = j.outEdges();
					while(edges.ok()) {
						Node k = edges.edge().target();
						if(outNeighborSet.contains(k))
							edgeCount++;
						edges.next();
					}
				}
				
				clusteringCoefficient = (double) edgeCount/maxEdges;
			}
			
			//Calculate sum of neighbors out-degrees (+1)
			int degreeSum = 0;
			
			NodeCursor neighbors = node.successors();
			
			while(neighbors.ok()) {
				Node neighbor = neighbors.node();
				degreeSum += neighbor.outDegree() + 1;
				neighbors.next();
			}
			
			res.setNodeValue(node, f(clusteringCoefficient)*degreeSum);
			nc.next();
		}
		return res;
	}
	
	/**
	 * Function to describe the (negative) influence of the clustering coefficient on the centrality value.
	 * Chosen according to the paper "Identifying Influential Nodes in Large-Scale Directed Networks: The Role of Clustering"
	 * @param c
	 * @return
	 */
	private double f(double c) {
		return Math.pow(10, -c);
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		return compatibleTypes;
	}

	@Override
	public CentralityCreationType getAlgorithmType() {
		return CentralityCreationType.CLUSTER_RANK;
	}
}