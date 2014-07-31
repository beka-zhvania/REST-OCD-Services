package i5.las2peer.services.servicePackage.algorithms;


import i5.las2peer.services.servicePackage.algorithms.utils.OcdAlgorithmException;
import i5.las2peer.services.servicePackage.graph.Cover;
import i5.las2peer.services.servicePackage.graph.CustomGraph;

/**
 * The common interface for all Overlapping Community Detection Algorithms.
 * @author Sebastian
 *
 */
public interface OcdAlgorithm {
	
	/**
	 * Executes the algorithm on a connected graph.
	 * @param graph An at least weakly connected graph whose community structure will be detected.
	 * @return A normalized cover for the input graph containing the community structure.
	 * Normalized here means that for each node the belonging factors sum up to one.
	 * @throws OcdAlgorithmException
	 */	
	public Cover detectOverlappingCommunities(CustomGraph graph) throws OcdAlgorithmException;
	
	/**
	 * Returns a log representing the concrete algorithm execution.
	 * @return The log.
	 */
	public AlgorithmLog getAlgorithmLog();

}
