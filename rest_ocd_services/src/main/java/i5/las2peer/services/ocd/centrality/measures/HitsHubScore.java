package i5.las2peer.services.ocd.centrality.measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.utils.MatrixOperations;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Node;


/**
 * Implementation of the HITS hub score.
 * See: Kleinberg, Jon M. 1999. Authoritative Sources in a Hyperlinked Environment. 
 * @author Tobias
 *
 */
public class HitsHubScore implements CentralityAlgorithm {
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.HITS_HUB_SCORE, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		Iterator<Node> nc = graph.iterator();
		int n = graph.getNodeCount();
		// If the graph contains no edges
		if(graph.getEdgeCount() == 0) {
			while(nc.hasNext()) {
				Node node = nc.next();
				res.setNodeValue(node, 0);
				nc.next();
			}
			return res;
		}
		
		Matrix A = graph.getNeighbourhoodMatrix();
		Vector hubWeights = new BasicVector(n);
		Vector authorityWeights = new BasicVector(n);
		
		// Set all weights to 1
		for(int i = 0; i < n; i++) {
			hubWeights.set(i, 1.0);
			authorityWeights.set(i, 1.0);
		}
		
		Vector oldA = new BasicVector(n);
		Vector oldH = new BasicVector(n);
		
		while(!authorityWeights.equals(oldA) || !hubWeights.equals(oldH)) {
			// Copy old values
			for(int i = 0; i < n; i++) {
				oldA.set(i, authorityWeights.get(i));
				oldH.set(i, hubWeights.get(i));
			}
			
			// Update authority weights
			for(int i = 0; i < n; i++) {
				double newValue = 0.0;
				for(int j = 0; j < n; j++) {
					newValue += A.get(j, i) * hubWeights.get(j);
				}
				authorityWeights.set(i, newValue);
			}
			
			// Update hub weights
			for(int i = 0; i < n; i++) {
				double newValue = 0.0;
				for(int j = 0; j < n; j++) {
					newValue += A.get(i, j) * authorityWeights.get(j);
				}
				hubWeights.set(i, newValue);
			}
			
			// Normalize
			double normA = MatrixOperations.norm(authorityWeights);
			authorityWeights = authorityWeights.divide(normA);
			double normH = MatrixOperations.norm(hubWeights);
			hubWeights = hubWeights.divide(normH);
		}
		
		// Set centrality values to the hub weights
		while(nc.hasNext()) {
			res.setNodeValue(nc.next(), hubWeights.get(nc.next().getIndex()));
			nc.next();
		}
		return res;
	}

	@Override
	public Set<GraphType> compatibleGraphTypes() {
		Set<GraphType> compatibleTypes = new HashSet<GraphType>();
		compatibleTypes.add(GraphType.DIRECTED);
		return compatibleTypes;
	}

	@Override
	public CentralityMeasureType getCentralityMeasureType() {
		return CentralityMeasureType.HITS_HUB_SCORE;
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
