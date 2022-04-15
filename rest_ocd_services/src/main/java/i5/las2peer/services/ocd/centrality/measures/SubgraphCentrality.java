package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;


/**
 * Implementation of Subgraph Centrality.
 * See: Estrada, Ernesto and Rodriguez-Velazquez, Juan A. 2005. Subgraph centrality in complex networks.
 * @author Tobias
 *
 */
public class SubgraphCentrality implements CentralityAlgorithm {
	// The number of iterations which determines the maximum power of the adjacency matrix that is calculated
	private static final int ITERATIONS = 20;
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.SUBGRAPH_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), 0);
			nc.next();
		}
		
		Matrix A = graph.getNeighbourhoodMatrix();
		for(int p = 1; p <= ITERATIONS; p++) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Matrix powerOfA = A.power(p);
			long weight = factorial(p);
			nc.toFirst();
			while(nc.hasNext()) {
				Node node = nc.next();	
				double weightedCycles = powerOfA.get(node.getIndex(), node.getIndex())/weight;
				res.setNodeValue(node, res.getNodeValue(node) + weightedCycles);
				nc.next();
			}
		}
		return res;
	}
	
	private long factorial(long i) {
		if(i <= 1)
			return 1;
		else
			return i * factorial(i-1);
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.SUBGRAPH_CENTRALITY;
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
