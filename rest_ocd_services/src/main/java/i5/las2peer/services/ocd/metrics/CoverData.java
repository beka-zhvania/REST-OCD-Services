package i5.las2peer.services.ocd.metrics;



import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

import i5.las2peer.services.ocd.graphs.Cover;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.graphstream.graph.Node;

import java.util.Iterator;

public class CoverData {
	
	public CoverData(){
		
	}
	
	/**
	 * computes the number of overlapping nodes (nodes that belong to more than one community)	
	 * @param cover the cover
	 * @return number of overlapping nodes
	 */
	public double noOverlappNodes(Cover cover){
		double res = 0;
		int j = 0;
		
		Matrix m = cover.getMemberships();
		
		int rows = m.rows();
		
		for(int i = 0; i < rows; i++){
			Vector v = m.getRow(i);
			while(j < v.length()){
				while(v.get(j) == 0 && j < v.length()){
					j++;
				}
				if(v.get(j) < 1 && v.get(j) > 0){
					res++;
					j = 0;
					break;
				}else{
					j = 0;
					break;
				}
			
			}
			
		}
		return res;
	}
	
	public double avgCommunitySize(Cover cover){
		int comSum = 0;
		for(int i = 0; i < cover.communityCount(); i++){
			comSum += cover.getCommunitySize(i);
		}
		
		return comSum/((double)cover.communityCount());
	}
	
	public Integer[] degreeDist(CustomGraph graph){
		Integer[] r = new Integer[graph.getEdgeCount()+1];
		Iterator<Node> nodesIt = graph.iterator();
		while(nodesIt.hasNext()){
			Node n = nodesIt.next();
			if(r[n.getDegree()] == null){
				r[n.getDegree()] = 1;
			}else{
				int deg = r[n.getDegree()];
				deg++;
				r[n.getDegree()] = deg;
			}
		}
		
		/*for(int i = 0; i < res.size(); i++){
			if(res.get(i) == null){
				res.set(i, 0);
			}
		}*/
		
		return r;
	}
	
	public Integer[] communitySizeDist(Cover cover){
		Integer[] res = new Integer[cover.getGraph().getNodeCount()+1];
		for(int i = 0; i < cover.communityCount(); i++){
			if(res[cover.getCommunitySize(i)] == null){
				res[cover.getCommunitySize(i)] = 1;
			}else{
				int sizeCount = res[cover.getCommunitySize(i)];
				sizeCount++;
				res[cover.getCommunitySize(i)] = sizeCount;
			}
			
		}
		
		/*for(int i = 0; i < res.size(); i++){
			if(res.get(i) == null){
				res.set(i, 0);
			}
		}*/
		
		return res;
	}
	
	
}
