package i5.las2peer.services.servicePackage.graph;

import java.util.HashMap;
import java.util.Map;

import y.algo.GraphConnectivity;
import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;

/**
 * Pre-processes graphs to facilitate community detection.
 * @author Sebastian
 *
 */
public class GraphProcessor {

	/**
	 * Transforms a graph into an undirected Graph.
	 * For each edge a reverse edge leading the opposite way is added, if missing.
	 * The reverse edge is assigned the same weight as the original one. If edges in both
	 * ways do already exist, they both are assigned their average weight.
	 * @param graph - The graph to be transformed.
	 */
	public void makeUndirected(CustomGraph graph) {
		EdgeCursor edges = graph.edges();
		while(edges.ok()) {
			Edge edge = edges.edge();
			double edgeWeight = graph.getEdgeWeight(edge);
			Edge reverseEdge;
			Node target = edge.target();
			Node source = edge.source();
			if(graph.containsEdge(target, source)) {
				reverseEdge = target.getEdgeTo(source);
				edgeWeight += graph.getEdgeWeight(reverseEdge);
				edgeWeight /= 2;
				graph.setEdgeWeight(edge, edgeWeight);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			else {
				reverseEdge = graph.createEdge(target, source);
				graph.setEdgeWeight(reverseEdge, edgeWeight);
			}
			edges.next();
		}
	}
	
	/**
	 * Returns all connected components of a graph.
	 * @param graph - The graph whose connected components are identified.
	 * @return A map containing the connected components and a corresponding mapping
	 * from the new component nodes to the original graph nodes.
	 */
	public Map<CustomGraph, Map<Node, Node>> getConnectedComponents(CustomGraph graph) {
		/*
		 * Iterates over all connected components of the graph creating a copy for each of them.
		 */
		NodeList[] componentsArray = GraphConnectivity.connectedComponents(graph);
		/* TODO
		 * Change variable type. componentsMap must allow duplicate keys.
		 */
		Map<CustomGraph, Map<Node, Node>> componentsMap = new HashMap<CustomGraph, Map<Node, Node>>();
		for(int i=0; i<componentsArray.length; i++) {
			CustomGraph component = new CustomGraph();
			Map<Node, Node> nodeMap = new HashMap<Node, Node>();
			Map<Node, Node> tmpNodeMap = new HashMap<Node, Node>();
			/*
			 * Sets component nodes
			 */
			NodeCursor nodes = componentsArray[i].nodes();
			while(nodes.ok()) {
				Node originalNode = nodes.node();
				Node newNode = component.createNode();
				component.setNodeName(newNode, graph.getNodeName(originalNode));
				nodeMap.put(newNode, originalNode);
				tmpNodeMap.put(originalNode, newNode);
				nodes.next();
			}
			/*
			 * Sets component edges
			 */
			nodes.toFirst();
			while(nodes.ok()) {
				Node node = nodes.node();
				EdgeCursor outEdges = node.outEdges();
				while(outEdges.ok()) {
					Edge outEdge = outEdges.edge();
					Node target = outEdge.target();
					Edge newEdge = component.createEdge(tmpNodeMap.get(node), tmpNodeMap.get(target));
					double edgeWeight = graph.getEdgeWeight(outEdge);
					component.setEdgeWeight(newEdge, edgeWeight);
					outEdges.next();
				}
				nodes.next();
			}
			componentsMap.put(component, nodeMap);
		}
		return componentsMap;
	}
}