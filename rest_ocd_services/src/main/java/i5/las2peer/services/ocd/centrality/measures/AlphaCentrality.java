package i5.las2peer.services.ocd.centrality.measures;

import java.util.*;

import org.la4j.inversion.GaussJordanInverter;
import org.la4j.inversion.MatrixInverter;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.vector.Vector;
import org.la4j.vector.dense.BasicVector;

import i5.las2peer.services.ocd.centrality.data.CentralityCreationLog;
import i5.las2peer.services.ocd.centrality.data.CentralityCreationType;
import i5.las2peer.services.ocd.centrality.data.CentralityMeasureType;
import i5.las2peer.services.ocd.centrality.utils.CentralityAlgorithm;
import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.graphs.GraphType;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Implementation of Alpha Centrality.
 * See: Bonacich, Phillip and Lloyd, Paulette. 2001. Eigenvector-like measures of centrality for asymmetric relations.
 * @author Tobias
 */
public class AlphaCentrality implements CentralityAlgorithm {
	
	private double alpha = 0.1;
	private String e = "";
	/*
	 * PARAMETER NAMES
	 */
	protected static final String ALPHA_NAME = "Alpha";
	protected static final String E_NAME = "External Status";
	
	public CentralityMap getValues(CustomGraph graph) throws InterruptedException {
		CentralityMap res = new CentralityMap(graph);
		res.setCreationMethod(new CentralityCreationLog(CentralityMeasureType.ALPHA_CENTRALITY, CentralityCreationType.CENTRALITY_MEASURE, this.getParameters(), this.compatibleGraphTypes()));
		
		int n = graph.getNodeCount();
		Matrix A_tr = graph.getNeighbourhoodMatrix().transpose();
		
		// Create identity matrix and vector of external status
		Matrix I = new CCSMatrix(n, n);
		Vector eVector = new BasicVector(n);
		for(int i = 0; i < n; i++) {
			I.set(i, i, 1.0);
			eVector.set(i, 1.0);
		}
		if(!e.equals("")) {
			String[] statusArray = e.split(",");
			for(int i = 0; i < statusArray.length; i++) {
				eVector.set(n-1-i, Double.parseDouble(statusArray[i])); // n-1-i because the last entry in the vector corresponds to the first node
			}
		}
		
		Matrix toInvert = I.subtract(A_tr.multiply(alpha));
		MatrixInverter gauss = new GaussJordanInverter(toInvert);
		Matrix inverse = gauss.inverse();
		Vector resultVector = inverse.multiply(eVector);
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			if(Thread.interrupted()) {
				throw new InterruptedException();
			}
			Node node = nc.next();
			res.setNodeValue(node, resultVector.get(node.getIndex()));
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
		return CentralityMeasureType.ALPHA_CENTRALITY;
	}
	
	@Override
	public Map<String, String> getParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(ALPHA_NAME, Double.toString(alpha));
		parameters.put(E_NAME, e);
		return parameters;
	}
	
	@Override
	public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {
		if(parameters.containsKey(ALPHA_NAME)) {
			alpha = Double.parseDouble(parameters.get(ALPHA_NAME));
			parameters.remove(ALPHA_NAME);
		}
		if(parameters.containsKey(E_NAME)) {
			e = parameters.get(E_NAME);
			parameters.remove(E_NAME);
		}
		if(parameters.size() > 0) {
			throw new IllegalArgumentException();
		}
	}
}
