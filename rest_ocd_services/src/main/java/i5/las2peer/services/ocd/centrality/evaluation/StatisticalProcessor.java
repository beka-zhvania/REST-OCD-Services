package i5.las2peer.services.ocd.centrality.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import i5.las2peer.services.ocd.centrality.data.CentralityMap;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

/**
 * Calculates statistical measures on centrality maps.
 * @author Tobias
 *
 */
public class StatisticalProcessor {
	
	/**
	 * Create a new centrality map containing the average values of the centrality maps in the given list.
	 * @param graph The graph the centrality measures are based on.
	 * @param maps The list of centrality maps.
	 * @return The resulting average centrality map.
	 */
	public static CentralityMap getAverageMap(CustomGraph graph, List<CentralityMap> maps) {
		CentralityMap resultMap = new CentralityMap(graph);
		int mapListSize = maps.size();
		
		Iterator<Node> nc = graph.iterator();
		while(nc.hasNext()) {
			Node currentNode = nc.next();
			double currentNodeAverage = 0.0;
			for(CentralityMap currentMap : maps) {
				currentNodeAverage += currentMap.getNodeValue(graph.getNodeName(currentNode));
			}
			resultMap.setNodeValue(currentNode, currentNodeAverage/mapListSize);
		}
		return resultMap;
	}
	
	/**
	 * Calculates a correlation matrix for the centrality maps in the given list.
	 * @param graph The graph the centrality measures are based on.
	 * @param maps The list of centrality maps.
	 * @param coefficient The correlation coefficient that is used.
	 * @return The correlation matrix as a RealMatrix.
	 */
	public static RealMatrix getCorrelation(CustomGraph graph, List<CentralityMap> maps, CorrelationCoefficient coefficient) {
		RealMatrix result = null;
		switch(coefficient) {
		case PEARSON:
			result = pearsonCorrelation(graph, maps);
			break;
		case SPEARMAN:
			result = spearmanCorrelation(graph, maps);
			break;
		case KENDALL:
			result = kendallCorrelation(graph, maps);
			break;
		}
		return result;
	}
	
	/**
	 * Calculates the Pearson correlation matrix for the centrality maps in the given list.
	 * @param graph The graph the centrality measures are based on.
	 * @param maps The list of centrality maps.
	 * @return The correlation matrix as a RealMatrix.
	 */
	public static RealMatrix pearsonCorrelation(CustomGraph graph, List<CentralityMap> maps) {
		double[][] mapsValues = getCentralityValuesMatrix(graph, maps);	
		PearsonsCorrelation correlation = new PearsonsCorrelation();
		RealMatrix result = correlation.computeCorrelationMatrix(mapsValues);	
		return result;
	}
	
	/**
	 * Calculates the Spearman correlation matrix for the centrality maps in the given list.
	 * @param graph The graph the centrality measures are based on.
	 * @param maps The list of centrality maps.
	 * @return The correlation matrix as a RealMatrix.
	 */
	public static RealMatrix spearmanCorrelation(CustomGraph graph, List<CentralityMap> maps) {
		double[][] mapsValues = getCentralityValuesMatrix(graph, maps);	
		SpearmansCorrelation correlation = new SpearmansCorrelation();
		RealMatrix result = correlation.computeCorrelationMatrix(mapsValues);	
		return result;
	}
	
	/**
	 * Calculates the Kendall correlation matrix for the centrality maps in the given list.
	 * @param graph The graph the centrality measures are based on.
	 * @param maps The list of centrality maps.
	 * @return The correlation matrix as a RealMatrix.
	 */
	public static RealMatrix kendallCorrelation(CustomGraph graph, List<CentralityMap> maps) {
		double[][] mapsValues = getCentralityValuesMatrix(graph, maps);		
		KendallsCorrelation correlation = new KendallsCorrelation();
		RealMatrix result = correlation.computeCorrelationMatrix(mapsValues);	
		return result;
	}
	
	private static double[][] getCentralityValuesMatrix(CustomGraph graph, List<CentralityMap> maps) {
		int n = graph.getNodeCount();
		int m = maps.size();
		double[][] mapsValues = new double[n][m];
		Iterator<Node> nc = graph.iterator();
		int i = 0;
		while(nc.hasNext()) {
			Node currentNode = nc.next();
			for(int j = 0; j < m; j++) {
				// Round to 8 decimal places so nodes with marginally different values are not put into different "classes"
				Double complete = maps.get(j).getNodeValue(graph.getNodeName(currentNode));
				Double rounded = BigDecimal.valueOf(complete).setScale(8, RoundingMode.HALF_UP).doubleValue();
				mapsValues[i][j] = rounded;
			}
			i++;
		}
		return mapsValues;
	}
	
	/**
	 * Calculates the precision at k of the given centrality maps in relating to the ground truth map.
	 * 
	 * @param graph The graph the centrality maps are based on.
	 * @param groundTruthMap The ground truth map that determines the correct ranking.
	 * @param maps The centrality maps whose precision is calculated.
	 * @param k The number k of top nodes that are considered.
	 * @return A double array containing the precision values.
	 */
	public static double[] getPrecision(CustomGraph graph, CentralityMap groundTruthMap, List<CentralityMap> maps, int k) {
		double[] precisionVector = new double[maps.size()];
		List<String> groundTruthRanking = groundTruthMap.getTopNodes(k);
		for(int i = 0; i < maps.size(); i++) {
			CentralityMap map = maps.get(i);
			List<String> centralityRanking = map.getTopNodes(k);
			int agreementCount = 0;
			for(int j = 0; j < k; j++) {
				if(centralityRanking.contains(groundTruthRanking.get(j)))
					agreementCount++;
			}
			precisionVector[i] = (double)agreementCount/k;
		}
		return precisionVector;
	}
}
