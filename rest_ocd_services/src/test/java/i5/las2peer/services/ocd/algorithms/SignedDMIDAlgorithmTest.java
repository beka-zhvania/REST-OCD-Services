package i5.las2peer.services.ocd.algorithms;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import org.graphstream.graph.Node;


public class SignedDMIDAlgorithmTest {
	@Test
	public void testGetLeadershipVector() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrSixNodesGraph();
		int nodeCount = graph.getNodeCount();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Vector leadershipVector = algo.getLeadershipVector(graph);
		Vector expectedVector = new BasicVector(nodeCount);
		expectedVector.set(0, 0.535714286);
		expectedVector.set(1, 0.48);
		expectedVector.set(2, -0.02);
		expectedVector.set(3, 0.568181818);
		expectedVector.set(4, -0.071428571);
		expectedVector.set(5, 0.428571429);
		double controlSum = 0;
		/*
		 * compute the difference between the known entry and the calculated
		 * entry the cumulative sum of all differences should be near to 0.
		 */
		for (int i = 0; i < nodeCount; i++) {
			controlSum += leadershipVector.get(i) - expectedVector.get(i);
		}
		System.out.println(controlSum);
		assertEquals(0, controlSum, 0.01);
	}

	@Test
	public void testGetLocalLeader() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		int nodeCount = graph.getNodeCount();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Vector leadershipVector = new BasicVector(nodeCount);
		leadershipVector.set(0, 0.465116279);
		leadershipVector.set(1, 0.480769231);
		leadershipVector.set(2, 0.166666667);
		leadershipVector.set(3, 0.573770492);
		leadershipVector.set(4, 0.462962963);
		leadershipVector.set(5, 0.305555556);
		leadershipVector.set(6, 0.352631579);
		leadershipVector.set(7, -0.037037037);
		leadershipVector.set(8, 0.465116279);
		leadershipVector.set(9, 0.441176471);
		leadershipVector.set(10, 0.471698113);
		leadershipVector.set(11, 0.486486486);
		Map<Integer, Integer> expectedMap = new HashMap<Integer, Integer>();
		Map<Node, Integer> followerMap = algo.getLocalLeader(graph, leadershipVector);
		expectedMap.put(3, 5);
		expectedMap.put(11, 3);
		System.out.println("Expected: " + expectedMap.toString());
		System.out.println("Result: " + followerMap.toString());

		assertEquals(expectedMap.entrySet().size(), followerMap.entrySet().size());
		assertTrue(followerMap.keySet().contains(graph.nodes().toArray(Node[]::new)[3]));
		assertTrue(followerMap.keySet().contains(graph.nodes().toArray(Node[]::new)[11]));
		assertEquals(5, (int) followerMap.get(graph.nodes().toArray(Node[]::new)[3]));
		assertEquals(3, (int) followerMap.get(graph.nodes().toArray(Node[]::new)[11]));
	}

	@Test
	public void testGetGlobalLeader() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = new CustomGraph();
		Map<Node, Integer> LeadershipMap = new HashMap<Node, Integer>();
		Node n[] = new Node[5];
		for (int i = 0; i < 5; i++) {
			n[i] = graph.addNode(Integer.toString(i));
			LeadershipMap.put(n[i], i + 1);
		}
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		List<Node> globalLeader = algo.getGlobalLeader(LeadershipMap);
		int leaderCount = globalLeader.size();
		/*
		 * number of global leaders=3
		 */
		assertEquals(3, leaderCount);
		assertTrue(globalLeader.contains(graph.nodes().toArray(Node[]::new)[2]));
		assertTrue(globalLeader.contains(graph.nodes().toArray(Node[]::new)[3]));
		assertTrue(globalLeader.contains(graph.nodes().toArray(Node[]::new)[4]));
		// assertEquals(expectedLeader,globalLeader);
	}

	@Test
	public void testExecuteLabelPropagation() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Vector leadershipVector = new BasicVector(graph.getNodeCount());
		leadershipVector.set(0, 0.465116279);
		leadershipVector.set(1, 0.480769231);
		leadershipVector.set(2, 0.166666667);
		leadershipVector.set(3, 0.573770492);
		leadershipVector.set(4, 0.462962963);
		leadershipVector.set(5, 0.305555556);
		leadershipVector.set(6, 0.352631579);
		leadershipVector.set(7, -0.037037037);
		leadershipVector.set(8, 0.465116279);
		leadershipVector.set(9, 0.441176471);
		leadershipVector.set(10, 0.471698113);
		leadershipVector.set(11, 0.486486486);
		Map<Node, Integer> map = algo.executeLabelPropagation(graph, graph.nodes().toArray(Node[]::new)[3], leadershipVector);
		Map<Node, Integer> expectedMap = new HashMap<Node, Integer>();
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[3], 0);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[0], 1);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[1], 1);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[2], 1);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[9], 1);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[10], 1);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[4], 2);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[5], 2);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[8], 2);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[7], 2);
		// expectedMap.put(graph.nodes().toArray(Node[]::new)[11], 3);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[3], 1);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[0], 2);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[1], 2);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[2], 2);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[9], 2);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[10], 2);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[4], 3);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[5], 3);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[8], 3);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[7], 3);
		expectedMap.put(graph.nodes().toArray(Node[]::new)[11], 4);
		System.out.println(map.toString());
		assertEquals(expectedMap, map);
	}

	@Test
	public void testGetMembershipDegrees() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		int nodeCount = graph.getNodeCount();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Map<Node, Map<Node, Integer>> communities = new HashMap<Node, Map<Node, Integer>>();
		Map<Node, Integer> community = new HashMap<Node, Integer>();
		community.put(graph.nodes().toArray(Node[]::new)[0], 2);
		community.put(graph.nodes().toArray(Node[]::new)[1], 2);
		community.put(graph.nodes().toArray(Node[]::new)[2], 2);
		community.put(graph.nodes().toArray(Node[]::new)[9], 2);
		community.put(graph.nodes().toArray(Node[]::new)[10], 2);
		community.put(graph.nodes().toArray(Node[]::new)[4], 3);
		community.put(graph.nodes().toArray(Node[]::new)[5], 3);
		community.put(graph.nodes().toArray(Node[]::new)[8], 3);
		community.put(graph.nodes().toArray(Node[]::new)[7], 3);
		community.put(graph.nodes().toArray(Node[]::new)[11], 4);
		communities.put(graph.nodes().toArray(Node[]::new)[3], community);
		Map<Node, Integer> communityTwo = new HashMap<Node, Integer>();
		communityTwo.put(graph.nodes().toArray(Node[]::new)[5], 2);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[6], 2);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[8], 2);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[9], 3);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[7], 3);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[1], 3);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[0], 4);
		communityTwo.put(graph.nodes().toArray(Node[]::new)[3], 4);
		communities.put(graph.nodes().toArray(Node[]::new)[11], communityTwo);
		Cover cover = algo.getMembershipDegrees(graph, communities);
		Matrix matrix = cover.getMemberships();
		System.out.println(matrix.toString());
		Matrix expectedMatrix = new Basic2DMatrix(12, 2);
		expectedMatrix.set(0, 0, 0.8);
		expectedMatrix.set(0, 1, 0.2);
		expectedMatrix.set(1, 0, 0.692307692);
		expectedMatrix.set(1, 1, 0.307692308);
		expectedMatrix.set(2, 0, 1.0);
		expectedMatrix.set(2, 1, 0);
		expectedMatrix.set(3, 0, 0.941176471);
		expectedMatrix.set(3, 1, 0.058823529);
		expectedMatrix.set(4, 0, 1.0);
		expectedMatrix.set(4, 1, 0);
		expectedMatrix.set(5, 0, 0.307692308);
		expectedMatrix.set(5, 1, 0.692307692);
		expectedMatrix.set(6, 0, 0);
		expectedMatrix.set(6, 1, 1);
		expectedMatrix.set(7, 0, 0.5);
		expectedMatrix.set(7, 1, 0.5);
		expectedMatrix.set(8, 0, 0.307692308);
		expectedMatrix.set(8, 1, 0.692307692);
		expectedMatrix.set(9, 0, 0.692307692);
		expectedMatrix.set(9, 1, 0.307692308);
		expectedMatrix.set(10, 0, 1);
		expectedMatrix.set(10, 1, 0);
		expectedMatrix.set(11, 0, 0.058823529);
		expectedMatrix.set(11, 1, 0.941176471);
		double controlSum = 0;
		for (int i = 0; i < nodeCount; i++) {
			controlSum += expectedMatrix.get(i, 0) - matrix.get(i, 0) + expectedMatrix.get(i, 1) - matrix.get(i, 1);
		}
		assertEquals(0, controlSum, 0.1);
	}

	@Test
	public void testDetectOverlappingCommunities()
			throws FileNotFoundException, AdapterException, OcdAlgorithmException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		int nodeCount = graph.getNodeCount();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Cover cover = algo.detectOverlappingCommunities(graph);
		Matrix matrix = cover.getMemberships();
		Matrix expectedMatrix = new Basic2DMatrix(12, 2);
		expectedMatrix.set(0, 0, 0.8);
		expectedMatrix.set(0, 1, 0.2);
		expectedMatrix.set(1, 0, 0.692307692);
		expectedMatrix.set(1, 1, 0.307692308);
		expectedMatrix.set(2, 0, 1.0);
		expectedMatrix.set(2, 1, 0);
		expectedMatrix.set(3, 0, 0.941176471);
		expectedMatrix.set(3, 1, 0.058823529);
		expectedMatrix.set(4, 0, 1.0);
		expectedMatrix.set(4, 1, 0);
		expectedMatrix.set(5, 0, 0.307692308);
		expectedMatrix.set(5, 1, 0.692307692);
		expectedMatrix.set(6, 0, 0);
		expectedMatrix.set(6, 1, 1);
		expectedMatrix.set(7, 0, 0.5);
		expectedMatrix.set(7, 1, 0.5);
		expectedMatrix.set(8, 0, 0.307692308);
		expectedMatrix.set(8, 1, 0.692307692);
		expectedMatrix.set(9, 0, 0.692307692);
		expectedMatrix.set(9, 1, 0.307692308);
		expectedMatrix.set(10, 0, 1);
		expectedMatrix.set(10, 1, 0);
		expectedMatrix.set(11, 0, 0.058823529);
		expectedMatrix.set(11, 1, 0.941176471);
		double controlSum = 0;
		for (int i = 0; i < nodeCount; i++) {
			controlSum += expectedMatrix.get(i, 0) - matrix.get(i, 0) + expectedMatrix.get(i, 1) - matrix.get(i, 1);
		}
		assertEquals(graph, cover.getGraph());
		assertEquals(0, controlSum, 0.1);
	}

	@Test
	public void testGetPosNodesWithNewLabel() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Set<Node> nodesWithNewBehavior = new HashSet<Node>();
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[5]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[6]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[8]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[11]);
		int posNeihboursWithNewBehavior = algo.getPosNodesWithNewLabel(graph, graph.nodes().toArray(Node[]::new)[7],
				nodesWithNewBehavior);
		assertEquals(3, posNeihboursWithNewBehavior);
	}

	@Test
	public void testGetNegNodesWithNewLabel() throws FileNotFoundException, AdapterException, InterruptedException {
		CustomGraph graph = OcdTestGraphFactory.getSignedLfrGraph();
		SignedDMIDAlgorithm algo = new SignedDMIDAlgorithm();
		Set<Node> nodesWithNewBehavior = new HashSet<Node>();
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[5]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[6]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[8]);
		nodesWithNewBehavior.add(graph.nodes().toArray(Node[]::new)[11]);
		int negNeihboursWithNewBehavior = algo.getNegNodesWithNewLabel(graph, graph.nodes().toArray(Node[]::new)[4],
				nodesWithNewBehavior);
		assertEquals(2, negNeihboursWithNewBehavior);
	}
}
