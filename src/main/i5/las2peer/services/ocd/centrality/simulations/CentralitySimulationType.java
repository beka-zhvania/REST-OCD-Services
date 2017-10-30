package i5.las2peer.services.ocd.centrality.simulations;

import java.security.InvalidParameterException;
import java.util.Locale;

import i5.las2peer.services.ocd.centrality.data.CentralityType;

public enum CentralitySimulationType implements CentralityType {
	
	UNDEFINED("Undefined", CentralitySimulation.class, 0),
	
	/**
	 * Type corresponding to the susceptible-infected-recovered simulation
	 */
	SIR("SIR Simulation", SirSimulation.class, 1),
	
	/**
	 * Type corresponding to a package simulation along shortest paths
	 */
	RANDOM_PACKAGE_TRANSMISSION("Random Package Transmission Simulation", RandomPackageTransmission.class, 2);
	
	/**
	 * The class corresponding to the type
	 */
	private final Class<? extends CentralitySimulation> simulationClass;
	
	/**
	 * For persistence and other purposes.
	 */
	private final int id;
	
	private final String displayName;
	
	/**
	 * Creates a new instance.
	 * @param creationMethodClass Defines the creationMethodClass attribute.
	 * @param id Defines the id attribute.
	 */
	private CentralitySimulationType(String displayName, Class<? extends CentralitySimulation> simulationClass, int id) {
		this.displayName = displayName;
		this.simulationClass = simulationClass;
		this.id = id;
	}
	
	/**
	 * Returns the graph simulation subclass corresponding to the type.
	 * @return The corresponding class.
	 */
	public Class<? extends CentralitySimulation> getSimulationClass() {
		return this.simulationClass;
	}
	
	/**
	 * Returns the unique id of the type.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the display name of the type.
	 * @return The name.
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the type corresponding to an id.
	 * @param id The id.
	 * @return The corresponding type.
	 */
	public static CentralitySimulationType lookupType(int id) {
        for (CentralitySimulationType type : CentralitySimulationType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * Returns the type corresponding to the given display name.
	 * @param displayName The display name.
	 * @return The corresponding type.
	 */
	public static CentralitySimulationType lookupType(String displayName) {
        for (CentralitySimulationType type : CentralitySimulationType.values()) {
            if (displayName.equals(type.getDisplayName())) {
                return type;
            }
        }
        throw new InvalidParameterException();
	}
	
	/**
	 * States whether the corresponding creation method class is actually a CentralityAlgorithm.
	 * @return TRUE if the class is a simulation, otherwise FALSE.
	 */
	public boolean correspondsAlgorithm() {
		if(CentralitySimulation.class.isAssignableFrom(this.getSimulationClass())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns the correct name of the centrality simulation.
	 */
	@Override
	public String toString() {
		String name = name();
		name = name.replace('_', ' ');
		name = name.toLowerCase(Locale.ROOT);
		return name;
	}
}
