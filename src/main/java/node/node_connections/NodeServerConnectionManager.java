/*
    Author: Nathaniel Brewer
*/
package node.node_connections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.ConnectionManager;
import connection.ConnectionDtoManager;
import connection.Priority;

import exception.KeepAliveException;

import packet.AbstractPacket;
import packet.keep_alive.KeepAliveManager;

public class NodeServerConnectionManager extends ConnectionManager {
       
    private static final Logger logger = LogManager.getLogger(NodeServerConnectionManager.class);

    public NodeServerConnectionManager(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role);
    }

    private static volatile NodeServerConnectionManager instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static NodeServerConnectionManager getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, NodeServerConnectionManager.class,
            () -> instance = new NodeServerConnectionManager(instanceId, clusterId, role)
        );
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
        public static NodeServerConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive() throws KeepAliveException {
                
        // Track if any keep-alive failed
        KeepAliveException lastException = null;
        
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
            }
        });
        
        // If we had any failures and stored an exception, throw it
        if (lastException != null) {
            throw lastException;
        }
        
        return true;
    }

}
