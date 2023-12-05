package i5.las2peer.services.ocd.algorithms;

import i5.las2peer.services.ocd.adapters.AdapterException;
import i5.las2peer.services.ocd.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.metrics.OcdMetricException;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.OCDAParameterTestReq;
import i5.las2peer.services.ocd.ocdatestautomation.test_interfaces.UndirectedGraphTestReq;
import i5.las2peer.services.ocd.testsUtils.OcdTestGraphFactory;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DetectingOverlappingCommunitiesAlgorithmTest implements UndirectedGraphTestReq, OCDAParameterTestReq {

	OcdAlgorithm algo;

	@BeforeEach
	public void setup() {
		algo = new DetectingOverlappingCommunitiesAlgorithm();
	}

	@Override
	public OcdAlgorithm getAlgorithm() {
		return algo;
	}


	@Disabled //TODO:DELETE 333
	@Test
	public void test() throws OcdAlgorithmException, InterruptedException, FileNotFoundException, AdapterException, OcdMetricException {
		OcdAlgorithm algo = new DetectingOverlappingCommunitiesAlgorithm();
		CustomGraph graph = OcdTestGraphFactory.getDocaTestGraph();
		//System.out.println("Nodes " + graph.getNodeCount() + " Edges " + graph.getEdgeCount());
		Cover cover = algo.detectOverlappingCommunities(graph);
		//System.out.println(cover.toString());
	}

}
