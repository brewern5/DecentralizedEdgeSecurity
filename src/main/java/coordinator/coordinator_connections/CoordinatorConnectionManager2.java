package coordinator.coordinator_connections;

import connection.ConnectionManager;
import packet.keep_alive.KeepAliveManager;
import packet.AbstractPacket;

public class CoordinatorConnectionManager2 extends ConnectionManager {

    private CoordinatorConnectionManager2(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role);
    }

    private static volatile CoordinatorConnectionManager2 instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static CoordinatorConnectionManager2 getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, CoordinatorConnectionManager2.class, 
            () -> instance = new CoordinatorConnectionManager2(instanceId, clusterId, role));
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static CoordinatorConnectionManager2 getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive(AbstractPacket packet) {
        
        return false;
    }

    public boolean retry () {
        
        return false;
    }
}
