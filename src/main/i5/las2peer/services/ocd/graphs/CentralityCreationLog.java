package i5.las2peer.services.ocd.graphs;

import i5.las2peer.services.ocd.utils.ExecutionStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A log representation for a CentralityCreationMethod.
 * @author Tobias
 *
 */
@Entity
public class CentralityCreationLog {
	
	/*
	 * Database column name definitions.
	 */
	private static final String idColumnName = "ID";
	private static final String typeColumnName = "TYPE";
	private static final String statusIdColumnName = "STATUS";
	private static final String executionTimeColumnName = "EXECUTION_TIME";
	
	/*
	 * Field names.
	 */
	public static final String STATUS_ID_FIELD_NAME = "statusId";
	
	/**
	 * System generated persistence id.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = idColumnName)
	private long id;
	/**
	 * Parameters used by the creation method.
	 */
	@ElementCollection
	private Map<String, String> parameters;
	/**
	 * Id of the creation methods corresponding CentralityCreationType.
	 */
	@Column(name = typeColumnName)
	private int typeId;
	/**
	 * The status of the corresponding execution.
	 */
	@Column(name = statusIdColumnName)
	private int statusId = ExecutionStatus.WAITING.getId();
	/**
	 * The execution time of the algorithm
	 */
	@Column(name = executionTimeColumnName)
	private long executionTime;
	/**
	 * The graph types the creation method is compatible with.
	 */
	@ElementCollection
	private Set<Integer> compatibleGraphTypes = new HashSet<Integer>();
	
	/**
	 * Creates a new instance.
	 * Only provided for persistence.
	 */
	protected CentralityCreationLog() {
	}
	
	/**
	 * Creates a new instance.
	 * @param type The type of creation method.
	 * @param parameters The parameters used by the creation method.
	 * @param compatibleGraphTypes The graph types which are compatible with the creation method.
	 */
	public CentralityCreationLog(CentralityCreationType type, Map<String, String> parameters, Set<GraphType> compatibleGraphTypes) {
		if(type != null) {
			this.typeId = type.getId();
		}
		else {
			this.typeId = CentralityCreationType.UNDEFINED.getId();
		}
		if(parameters != null) {
			this.parameters = parameters;
		}
		else {
			this.parameters = new HashMap<String, String>();
		}
		if(compatibleGraphTypes != null) {
			for(GraphType graphType : compatibleGraphTypes) {
				this.compatibleGraphTypes.add(graphType.getId());
			}
		}
	}

	/**
	 * Returns the type of the corresponding creation method.
	 * @return The type.
	 */
	public CentralityCreationType getType() {
		return CentralityCreationType.lookupType(typeId);
	}

	/**
	 * Returns the parameters used by the corresponding creation method.
	 * @return A mapping from each parameter name to the parameter value in String format.
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Returns the log id.
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Returns the graph types the corresponding creation method is compatible with.
	 * @return The graph types.
	 */
	public Set<GraphType> getCompatibleGraphTypes() {
		Set<GraphType> compatibleGraphTypes = new HashSet<GraphType>();
		for(int id : this.compatibleGraphTypes) {
			compatibleGraphTypes.add(GraphType.lookupType(id));
		}
		return compatibleGraphTypes;
	}
	
	/**
	 * Returns the execution status of the corresponding creation method.
	 * @return The status.
	 */
	public ExecutionStatus getStatus() {
		return ExecutionStatus.lookupStatus(statusId);
	}
	
	/**
	 * Sets the execution status of the corresponding creation method.
	 * @param status The status.
	 */
	public void setStatus(ExecutionStatus status) {
		this.statusId = status.getId();
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long time) {
		executionTime = time;
	}
}
