/*
 *      Author: Nathaniel Brewer
 */

package components.server.connections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.connection.ConnectionDtoManager;
import core.connection.ConnectionManager;
import core.connection.Priority;
import core.exception.KeepAliveException;
import core.packet.AbstractPacket;
import core.packet.keep_alive.KeepAliveManager;

import java.util.concurrent.atomic.AtomicReference;

public class ServerCoordinatorConnectionManager extends ConnectionManager {
    
    private static final Logger logger = LogManager.getLogger(ServerCoordinatorConnectionManager.class);

    private ServerCoordinatorConnectionManager(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role); 
    }

    private static volatile ServerCoordinatorConnectionManager instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static ServerCoordinatorConnectionManager getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, ServerCoordinatorConnectionManager.class, 
            () -> instance = new ServerCoordinatorConnectionManager(instanceId, clusterId, role));
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static ServerCoordinatorConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive() throws KeepAliveException {
        
        // Track if any keep-alive failed
        AtomicReference<KeepAliveException> lastException = new AtomicReference<>();
        
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
                    // Packet failed to send or ACK not received
                    throw new KeepAliveException(
                        KeepAliveException.FailureStage.SEND_FAILED,
                        connectionId,
                        instanceId,
                        "Failed to send keep-alive packet or receive ACK"
                    );
                }
                
                // TODO: At this point, packet was sent and ACK received, but we need to verify it was handled
                // For now, log success
                logger.debug("Keep-alive sent successfully to connection: {}", connectionId);

            } catch (KeepAliveException e) {
                logger.error("Keep-alive failed for connection: {}", connectionId, e);
                
                // Handle based on priority
                if (connection.getPriority() != Priority.CRITICAL) {
                    logger.warn("Connection: \"{}\" with criticality status: \"{}\" was terminated!", 
                               connectionId, connection.getPriority());
                    terminateConnection(connectionId);
                } else {
                    // For critical connections, log but don't terminate immediately
                    logger.error("CRITICAL connection: \"{}\" failed keep-alive! Reason: {}", 
                                connectionId, e.getStage().getDescription());
                    terminateConnection(connectionId);
                }
                
                // Store the exception to rethrow after forEach (can't throw from lambda)
                // Note: This will only keep the last exception, but indicates there was a failure
                lastException.set(e);
            }
        });
        
        // If we had any failures and stored an exception, throw it
        if (lastException.get() != null) {
            throw lastException.get();
        }
        
        return true;
    }

}
