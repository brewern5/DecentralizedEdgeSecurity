package coordinator.coordinator_connections;

import connection.ConnectionDtoManager;
import connection.ConnectionManager;
import connection.Priority;
import exception.KeepAliveException;
import packet.keep_alive.KeepAliveManager;
import packet.AbstractPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoordinatorConnectionManager extends ConnectionManager {

    private static final Logger logger = LogManager.getLogger(CoordinatorConnectionManager.class);

    private CoordinatorConnectionManager(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role);
    }

    private static volatile CoordinatorConnectionManager instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static CoordinatorConnectionManager getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, CoordinatorConnectionManager.class, 
            () -> instance = new CoordinatorConnectionManager(instanceId, clusterId, role));
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static CoordinatorConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive() throws KeepAliveException {
    
        activeConnections.forEach((connectionId, connection) -> {

            try {
                AbstractPacket packet = new KeepAliveManager(
                    instanceId, 
                    clusterId, 
                    connectionId, 
                    role
                ).createOutgoingPacket();

                boolean keptAlive = new ConnectionDtoManager(connection).send(packet);

                if (!keptAlive) {
                    throw new KeepAliveException(
                        KeepAliveException.FailureStage.SEND_FAILED,
                        connectionId,
                        instanceId,
                        "Failed to send keep-alive packet or receive ACK"
                    );
                }
                
                logger.debug("Keep-alive sent successfully to connection: {}", connectionId);

            } catch (KeepAliveException e) {
                logger.error("Keep-alive failed for connection: {}", connectionId, e);
                
                if(connection.getPriority() != Priority.CRITICAL) {
                    logger.warn("Connection: \"{}\" with criticality status: \"{}\" was terminated!", 
                               connectionId, connection.getPriority());
                    terminateConnection(connectionId);
                } else {
                    logger.error("CRITICAL connection: \"{}\" failed keep-alive! Reason: {}", 
                                connectionId, e.getStage().getDescription());
                    terminateConnection(connectionId);
                }
            }

        });

        return true;
    }

}
