package i5.las2peer.services.ocd.simulation;

import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import i5.las2peer.services.ocd.graphs.CentralityMap;
import i5.las2peer.services.ocd.graphs.CentralityMapId;
import i5.las2peer.services.ocd.graphs.CustomGraphId;
import i5.las2peer.services.ocd.utils.ExecutionStatus;
import i5.las2peer.services.ocd.utils.RequestHandler;
import i5.las2peer.services.ocd.utils.ThreadHandler;

public class SimulationRunnable implements Runnable {
	
	/**
	 * The persisted CentralityMap reserved for the algorithm result.
	 */
	private CentralityMap map;
	/**
	 * The algorithm to execute.
	 */
	private GraphSimulation simulation;
	/**
	 * The thread handler in charge of the runnable execution.
	 */
	private ThreadHandler threadHandler;
	
	/**
	 * Creates a new instance.
	 * @param map Sets the CentralityMap.
	 * @param simulation Sets the CentralityAlgorithm.
	 * @param threadHandler Sets the thread handler.
	 */
	public SimulationRunnable(CentralityMap map, GraphSimulation simulation, ThreadHandler threadHandler) {
		this.simulation = simulation;
		this.map = map;
		this.threadHandler = threadHandler;
	}
	
	@Override
	public void run() {
		boolean error = false;
		/*
		 * Set simulation state to running.
		 */
		CustomGraphId graphId = new CustomGraphId(map.getGraph().getId(), map.getGraph().getUserName());
    	CentralityMapId id = new CentralityMapId(map.getId(), graphId);
    	RequestHandler requestHandler = new RequestHandler();
    	EntityManager em = requestHandler.getEntityManager();
    	EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			CentralityMap map = em.find(CentralityMap.class, id);
			if(map == null) {
				/*
				 * Should not happen.
				 */
				requestHandler.log(Level.SEVERE, "Centrality map deleted while simulation running.");
				throw new IllegalStateException();
			}
			map.getCreationMethod().setStatus(ExecutionStatus.RUNNING);
			tx.commit();
		} catch( RuntimeException e ) {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
			}
			error = true;
		}
		em.close();
		/*
		 * Run simulation.
		 */
		CentralityMap resultMap = null;
		if(!error) {
	        SimulationExecutor executor = new SimulationExecutor();
	        try {
	        	resultMap = executor.execute(map.getGraph(), simulation);
	        	if(Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
	        }
	        catch (InterruptedException e) {
	        	return;
	        }
			catch (Exception e) {
				requestHandler.log(Level.SEVERE, "Algorithm Failure.", e);
				error = true;
			}
		}
    	threadHandler.createCentralityMap(resultMap, id, error);
	}

}
