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
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

import core.packet.AbstractPacket;
import core.packet.keep_alive.KeepAliveManager;

import java.util.concurrent.atomic.AtomicReference;

public class ServerCoordinatorConnectionManager extends ConnectionManager {
    
    private static final Logger logger = LogManager.getLogger(ServerCoordinatorConnectionManager.class);

    private ServerCoordinatorConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        super(membershipState, instantiatorRole); 
    }

    private static volatile ServerCoordinatorConnectionManager instance;

    /** 
     * 
     * @param membershipState The DTO for the membership of this device 
     * @param instantiatorRole The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static ServerCoordinatorConnectionManager getInstance(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        return getOrCreateInstance(instance, ServerCoordinatorConnectionManager.class, 
            () -> instance = new ServerCoordinatorConnectionManager(membershipState, instantiatorRole)
        );
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
        
        AtomicReference<KeepAliveException> lastException = new AtomicReference<>();
        
        activeConnections.forEach((connectionId, connection) -> {

            try {
                AbstractPacket packet = new KeepAliveManager(
                    membershipState, 
                    connectionId, 
                    instantiatorRole
                ).createOutgoingPacket();

                boolean keptAlive = new ConnectionDtoManager(connection).send(packet);

                if (!keptAlive) {
                    throw new KeepAliveException(
                        KeepAliveException.FailureStage.SEND_FAILED,
                        connectionId,
                        membershipState.assignedId(),
                        "Failed to send keep-alive packet or receive ACK"
                    );
                }
                
                // TODO: At this point, packet was sent and ACK received, but we need to verify it was handled
                logger.debug("Keep-alive sent successfully to connection: {}", connectionId);

            } catch (KeepAliveException e) {
                logger.error("Keep-alive failed for connection: {}", connectionId, e);
                
                if (connection.getPriority() != Priority.CRITICAL) {
                    logger.warn("Connection: \"{}\" with criticality status: \"{}\" was terminated!", 
                               connectionId, connection.getPriority());
                    terminateConnection(connectionId);
                } else {
                    logger.error("CRITICAL connection: \"{}\" failed keep-alive! Reason: {}", 
                                connectionId, e.getStage().getDescription());
                    terminateConnection(connectionId);
                }
                lastException.set(e);
            }
        });
        
        if (lastException.get() != null) {
            throw lastException.get();
        }
        
        return true;
    }

}
