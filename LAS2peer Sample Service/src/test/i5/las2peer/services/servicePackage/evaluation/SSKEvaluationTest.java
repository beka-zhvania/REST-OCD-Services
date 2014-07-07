package i5.las2peer.services.servicePackage.evaluation;

import i5.las2peer.services.servicePackage.OverlappingCommunityDetectionAnalyzer;
import i5.las2peer.services.servicePackage.adapters.coverOutput.CoverOutputAdapter;
import i5.las2peer.services.servicePackage.adapters.coverOutput.LabeledMembershipMatrixOutputAdapter;
import i5.las2peer.services.servicePackage.algorithms.OverlappingCommunityDetectionAlgorithm;
import i5.las2peer.services.servicePackage.algorithms.SSKAlgorithm;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;
import i5.las2peer.services.servicePackage.metrics.StatisticalMeasure;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestConstants;
import i5.las2peer.services.servicePackage.testsUtil.OcdTestGraphFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/*
 * Test Class for the Speaker Listener Label Propagation Algorithm
 */
public class SSKEvaluationTest {

	/**
	 * Test the SLPA Algorithm on a simple Graph
	 * @throws IOException 
	 */

	@Test
	public void testSskAlgoOnSiamDm() throws IOException
	{
		System.out.println("Siam Components:");
		CustomGraph graph = OcdTestGraphFactory.getSiamDmGraph();
		OverlappingCommunityDetectionAnalyzer analyzer = new OverlappingCommunityDetectionAnalyzer();
		List<CustomGraph> graphs = new ArrayList<CustomGraph>();
		graphs.add(graph);
		List<OverlappingCommunityDetectionAlgorithm> algorithms = new ArrayList<OverlappingCommunityDetectionAlgorithm>();
		algorithms.add(new SSKAlgorithm());
		List<StatisticalMeasure> statisticalMeasures = new ArrayList<StatisticalMeasure>();
		List<Cover> covers = analyzer.analyze(graphs, algorithms, statisticalMeasures, 8);
		Cover cover = covers.get(0);
		System.out.println(cover.toString());
		CoverOutputAdapter adapter = new LabeledMembershipMatrixOutputAdapter();
		adapter.setFilename(OcdTestConstants.sskSiamDmLabeledMembershipMatrixOutputPath);
		adapter.writeCover(cover);
	}
}
