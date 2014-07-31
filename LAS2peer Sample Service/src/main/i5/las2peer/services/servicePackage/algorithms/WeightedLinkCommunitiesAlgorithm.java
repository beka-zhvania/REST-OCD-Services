package i5.las2peer.services.servicePackage.algorithms;

import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.graph.GraphType;
import i5.las2peer.services.servicePackage.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.Vectors;
import org.la4j.vector.sparse.CompressedVector;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeCursor;

/**
 * The original weighted version of the Link Communities Algorithm.
 * Handles weighted graphs.
 */
public class WeightedLinkCommunitiesAlgorithm implements
		OcdAlgorithm {
	
	/**
	 * Creates an instance of the algorithm.
	 */
	public WeightedLinkCommunitiesAlgorithm() {
	}
	
	@Override
	public AlgorithmLog getAlgorithmLog() {
		return new AlgorithmLog(AlgorithmType.WEIGHTED_LINK_COMMUNITIES_ALGORITHM, new HashMap<String, String>(), compatibleGraphTypes());
	}
	
	private Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibilities = new HashSet<GraphType>();
		compatibilities.add(GraphType.WEIGHTED);
		return compatibilities;
	}

	@Override
	public Cover detectOverlappingCommunities(CustomGraph graph) {
		/*
		 * Initializes the variables.
		 */
		List<Set<Edge>> communityEdges = new ArrayList<Set<Edge>>();
		List<Set<Node>> communityNodes = new ArrayList<Set<Node>>();
		List<Double> communityLinkDensities = new ArrayList<Double>();
		List<Pair<Integer, Integer>> mostSimilarPairs;
		Pair<Integer, Integer> mostSimilarPair;
		Set<Edge> firstCommunityEdges;
		Set<Edge> secondCommunityEdges;
		double currentPartitionDensity = 0;
		double maxPartitionDensity = Double.NEGATIVE_INFINITY;
		double firstLinkDensity;
		double secondLinkDensity;
		double newLinkDensity;
		int firstCommunity;
		int secondCommunity;
		List<Set<Edge>> densestPartition = new ArrayList<Set<Edge>>();
		/*
		 * Initializes the dendrogram construction
		 */
		List<Vector> linkageDegrees = calculateLinkageDegrees(graph);
		Matrix similarities = calculateEdgeSimilarities(graph, linkageDegrees);
		initDendrogramCreation(graph, communityEdges, communityNodes, communityLinkDensities);
		/*
		 * Constructs the dendrogram and determines the edge partition
		 * with the highest partition density.
		 */
		while(similarities.columns() > 1) {
			mostSimilarPairs = determineMostSimilarCommunityPairs(similarities);
			for(int i=0; i<mostSimilarPairs.size(); i++) {
				mostSimilarPair = mostSimilarPairs.get(i);
				firstCommunity = mostSimilarPair.getFirst();
				secondCommunity = mostSimilarPair.getSecond();
				similarities = updateSimilarities(similarities, mostSimilarPair);
				firstCommunityEdges = communityEdges.get(firstCommunity);
				secondCommunityEdges = communityEdges.get(secondCommunity);
				firstCommunityEdges.addAll(secondCommunityEdges);
				Set<Node> firstCommunityNodes = communityNodes.get(firstCommunity);
				Set<Node> secondCommunityNodes = communityNodes.get(secondCommunity);
				firstCommunityNodes.addAll(secondCommunityNodes);
				firstLinkDensity = communityLinkDensities.get(firstCommunity);
				secondLinkDensity = communityLinkDensities.get(secondCommunity);
				newLinkDensity = calculateLinkDensity(firstCommunityEdges.size(), firstCommunityNodes.size());
				communityLinkDensities.set(firstCommunity, newLinkDensity);
				currentPartitionDensity += newLinkDensity - firstLinkDensity - secondLinkDensity;
			}
			for(int i=0; i<mostSimilarPairs.size(); i++) {
				secondCommunity = mostSimilarPairs.get(i).getSecond();
				communityEdges.remove(secondCommunity);
				communityNodes.remove(secondCommunity);
				communityLinkDensities.remove(secondCommunity);
			}
			if(currentPartitionDensity >= maxPartitionDensity) {
				maxPartitionDensity = currentPartitionDensity;
				densestPartition = new ArrayList<Set<Edge>>(communityEdges);
			}
		}
		return calculatePartitionCover(graph, densestPartition);
	}
	
	/*
	 * Calculates the edge similarity matrix.
	 * This is a lower triangle matrix containing a similarity value for each edge pair. 
	 * @param linkageDegrees The linkage degree vectors calculated for each node.
	 * @return A similarity matrix S containing the edge similarity for two edges e_i, e_j with indices i and j and i>j in 
	 * the entry S_ij.
	 */
	private Matrix calculateEdgeSimilarities(CustomGraph graph, List<Vector> linkageDegrees) {
		Matrix similarities = new CCSMatrix(graph.edgeCount(), graph.edgeCount());
		List<Double> squaredNorms = new ArrayList<Double>();
		double norm;
		Vector vec;
		for(int i=0; i<linkageDegrees.size(); i++) {
			vec = linkageDegrees.get(i);
			norm = vec.fold(Vectors.mkEuclideanNormAccumulator());
			squaredNorms.add(Math.pow(norm, 2));
		}
		EdgeCursor rowEdges = graph.edges();
		Edge rowEdge;
		Node source;
		Node target;
		Vector rowEdgeVec;
		EdgeCursor columnEdges;
		Edge columnEdge;
		Edge reverseRowEdge;
		Edge reverseColumnEdge;
		List<Integer> edgeIndices = new ArrayList<Integer>();
		Vector columnEdgeVec;
		double innerProduct;
		double similarity;
		int columnEdgeNodeIndex;
		while(rowEdges.ok()) {
			rowEdge = rowEdges.edge();
			source = rowEdge.source();
			target = rowEdge.target();
			rowEdgeVec = linkageDegrees.get(source.index());
			reverseRowEdge = target.getEdgeTo(source);
			/*
			 * Sets similarities only if they have not been set already for the reverse Edge.
			 */
			if(reverseRowEdge == null || rowEdge.index() < reverseRowEdge.index()) {
				/*
				 * Sets similarities for in and out edges of the row edge target node.
				 */
				edgeIndices.add(rowEdge.index());
				columnEdges = target.edges();
				while(columnEdges.ok()) {
					columnEdge = columnEdges.edge();
					if(columnEdge.index() < rowEdge.index()) {
						reverseColumnEdge = columnEdge.target().getEdgeTo(columnEdge.source());
						if(reverseColumnEdge == null || columnEdge.index() < reverseColumnEdge.index()) {
							columnEdgeNodeIndex = columnEdge.opposite(target).index();
							columnEdgeVec = linkageDegrees.get(columnEdgeNodeIndex);
							innerProduct = rowEdgeVec.innerProduct(columnEdgeVec);
							similarity = innerProduct / (squaredNorms.get(source.index()) 
									+ squaredNorms.get(columnEdgeNodeIndex) - innerProduct);
							similarities.set(rowEdge.index(), columnEdge.index(), similarity);
						}
					}
					columnEdges.next();
				}
				/*
				 * Sets similarities for in edges of the row edge source node.
				 * If a reverse edge of the row edge exists, it is set for the out edges also.
				 */
				columnEdges = source.edges();
				while(columnEdges.ok()) {
					columnEdge = columnEdges.edge();
					if(columnEdge.index() < rowEdge.index() && columnEdge.source() != target) {
						reverseColumnEdge = columnEdge.target().getEdgeTo(columnEdge.source());
						if(reverseColumnEdge == null || columnEdge.index() < reverseColumnEdge.index()) {
							columnEdgeNodeIndex = columnEdge.opposite(source).index();
							columnEdgeVec = linkageDegrees.get(columnEdgeNodeIndex);
							innerProduct = rowEdgeVec.innerProduct(columnEdgeVec);
							similarity = innerProduct / (squaredNorms.get(target.index())
									+ squaredNorms.get(columnEdgeNodeIndex) - innerProduct);
							similarities.set(rowEdge.index(), columnEdge.index(), similarity);
						}
					}
					columnEdges.next();
				}
			}
			rowEdges.next();
		}
		int[] indices = new int[edgeIndices.size()];
		for(int i=0; i<edgeIndices.size(); i++) {
			indices[i] = edgeIndices.get(i);
		}
		if(indices.length > 0) {
			return similarities.select(indices, indices);
		}
		else {
			return new CCSMatrix(0, 0);
		}
	}
	
	
	/*
	 * Calculates the linkage degree vectors for all nodes.
	 * The linkage degrees are required for the calculation of edge similarity
	 * and originally referred to as the a vectors.
	 * @param graph The graph being analyzed.
	 * @return The linkage degree vector of each node, accessible via the list index that
	 * corresponds to the node index.
	 */
	private List<Vector> calculateLinkageDegrees(CustomGraph graph) {
		List<Vector> linkageDegrees = new ArrayList<Vector>();
		NodeCursor nodes = graph.nodes();
		Vector degreeVector;
		Node node;
		Node neighbor;
		EdgeCursor edges;
		Edge edge;
		double linkageDegree;
		double neutral;
		double averageWeight;
		while(nodes.ok()) {
			degreeVector = new CompressedVector(graph.nodeCount());
			node = nodes.node();
			edges = node.edges();
			while(edges.ok()) {
				edge = edges.edge();
				neighbor = edges.edge().opposite(node);
				linkageDegree = degreeVector.get(neighbor.index());
				linkageDegree += graph.getEdgeWeight(edge) / (double)node.degree();
				degreeVector.set(neighbor.index(), linkageDegree);
				edges.next();
			}
			/*
			 * Calculates the entry corresponding the node index as the average weight
			 * of all incident edges.
			 */
			neutral = 0;
			averageWeight = degreeVector.fold(Vectors.asSumAccumulator(neutral));
			degreeVector.set(node.index(), averageWeight);
			linkageDegrees.add(degreeVector);
			nodes.next();
		}
		return linkageDegrees;
	}
	
	/*
	 * Identifies the edge community pairs with maximum similarity. 
	 * @param similarities The similarity matrix.
	 * @return A list of pairs with the indices of the identified edge communities. If several pairs are
	 * joined to a bigger community simultaneously, the first index of each pair will
	 * be the lowest index of the corresponding old communities. I.e. all old communities will be projected
	 * on the same new one with the lowest community index.
	 */
	private List<Pair<Integer, Integer>> determineMostSimilarCommunityPairs(Matrix similarities) {
		double maxSimilarity = Double.NEGATIVE_INFINITY;
		double currentSimilarity;
		TreeMap<Integer, Integer> mergedCommunities = new TreeMap<Integer, Integer>();
		Set<Integer> updatedCommunities = new HashSet<Integer>();
		int oldCommunity;
		int newCommunity;
		for(int j=0; j<similarities.columns() - 1; j++) {
			for(int i=j+1; i<similarities.rows(); i++) {
				currentSimilarity = similarities.get(i, j);
				if(currentSimilarity >= maxSimilarity) {
					if(currentSimilarity > maxSimilarity) {
						mergedCommunities.clear();
						maxSimilarity = currentSimilarity;
					}
					newCommunity = j;
					if(mergedCommunities.containsKey(j)) {
						oldCommunity = mergedCommunities.get(j);
						if(oldCommunity <= newCommunity) {
							newCommunity = oldCommunity;
						}
						else {
							updatedCommunities.add(oldCommunity);
						}
					}
					if(mergedCommunities.containsKey(i)) {
						oldCommunity = mergedCommunities.get(i);
						if(oldCommunity <= newCommunity) {
							newCommunity = oldCommunity;
						}
						else {
							updatedCommunities.add(oldCommunity);
						}
					}
					if(updatedCommunities.size() > 0) {
						for(Entry<Integer, Integer> entry : mergedCommunities.entrySet()) {
							if(updatedCommunities.contains(entry.getValue())) {
								entry.setValue(newCommunity);
							}
						}
					}
					mergedCommunities.put(j, newCommunity);
					mergedCommunities.put(i, newCommunity);
				}
			}
		}
		List<Pair<Integer, Integer>> mostSimilarPairs = new ArrayList<Pair<Integer, Integer>>();
		Entry<Integer, Integer> lastPair;
		while(mergedCommunities.size() > 0) {
			lastPair = mergedCommunities.lastEntry();
			if(lastPair.getKey() != lastPair.getValue()) {
				mostSimilarPairs.add(new Pair<Integer, Integer>(lastPair.getValue(), lastPair.getKey()));
			}
			mergedCommunities.remove(lastPair.getKey());
		}
		return mostSimilarPairs;
	}

	/*
	 * Updates the similarity matrix when the two edge communities given by mostSimilarPair are merged. 
	 * @param similarities The similarity matrix.
	 * @param mostSimilarPair A pair containing the indices of the communities that are merged.
	 * @return The updated similarity matrix.
	 */
	private Matrix updateSimilarities(Matrix similarities, Pair<Integer, Integer> mostSimilarPair) {
		int first = mostSimilarPair.getFirst();
		int second = mostSimilarPair.getSecond();
		int[] newIndices = new int[similarities.rows() - 1];
		double maxSimilarity;
		for(int i=0; i<similarities.columns(); i++) {
			if(i != second) {
				if(i <= first) {
					if(i < first) {
						maxSimilarity = Math.max(similarities.get(first, i), similarities.get(second, i));
						similarities.set(first, i, maxSimilarity);
					}
					newIndices[i] = i;
				}
				else {
					if(i < second) {
						maxSimilarity = Math.max(similarities.get(i, first), similarities.get(second, i));
						newIndices[i] = i;
					}
					else {
						maxSimilarity = Math.max(similarities.get(i, first), similarities.get(i, second));
						newIndices[i-1] = i;
					}
					similarities.set(i, first, maxSimilarity);
				}
			}

		}
		return similarities.select(newIndices, newIndices);
	}
	
	/*
	 * Initializes variables for the dendrogram creation.
	 * @param graph The graph being analyzed.
	 * @param communityEdges An edge partition indicating the edge communities.
	 * @param communityNodes A node cover derived from the edge partition.
	 * @param communityLinkDensities The link densities of all edge communities.
	 */
	private void initDendrogramCreation(CustomGraph graph, List<Set<Edge>> communityEdges,
			List<Set<Node>> communityNodes, List<Double> communityLinkDensities) {
		EdgeCursor edges = graph.edges();
		Set<Edge> initEdgeSet;
		Set<Node> initNodeSet;
		Edge edge;
		Edge reverseEdge;
		while(edges.ok()) {
			edge = edges.edge();
			reverseEdge = edge.target().getEdgeTo(edge.source());
			if(reverseEdge == null || edge.index() < reverseEdge.index()) {
				initEdgeSet = new HashSet<Edge>();
				initEdgeSet.add(edge);
				if(reverseEdge != null) {
					initEdgeSet.add(reverseEdge);
				}
				communityEdges.add(initEdgeSet);
				initNodeSet = new HashSet<Node>();
				initNodeSet.add(edge.source());
				initNodeSet.add(edge.target());
				communityNodes.add(initNodeSet);
				communityLinkDensities.add(0d);
			}
			edges.next();
		}
	}
	
	/*
	 * Calculates the weighted link density of a community. 
	 * @param edgeCount The number of community edges.
	 * @param nodeCount The number of community nodes.
	 * @return The weighted link density.
	 */
	private double calculateLinkDensity(int edgeCount, int nodeCount) {
		int denominator = (nodeCount - 2) * (nodeCount - 1);
		return (double)(edgeCount * (edgeCount - (nodeCount - 1))) / (double)denominator;
	}

	/*
	 * Derives a cover from an edge partition.
	 * @param graph The graph being analyzed.
	 * @param partition The edge partition from which the cover will be derived.
	 * @return A normalized cover of the graph.
	 */
	private Cover calculatePartitionCover(CustomGraph graph, List<Set<Edge>> partition) {
		Matrix memberships = new CCSMatrix(graph.nodeCount(), partition.size());
		double belongingFactor;
		double edgeWeight;
		for(int i=0; i<partition.size(); i++) {
			for(Edge edge : partition.get(i)) {
				edgeWeight = graph.getEdgeWeight(edge);
				belongingFactor = memberships.get(edge.target().index(), i) + edgeWeight;
				memberships.set(edge.target().index(), i, belongingFactor);
				belongingFactor = memberships.get(edge.source().index(), i) + edgeWeight;
				memberships.set(edge.source().index(), i, belongingFactor);
			}
		}
		Cover cover = new Cover(graph, memberships, getAlgorithmLog());
		cover.normalizeMemberships();
		return cover;
	}
	
}
