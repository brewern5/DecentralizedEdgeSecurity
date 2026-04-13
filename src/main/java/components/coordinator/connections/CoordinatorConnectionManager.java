package components.coordinator.connections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.connection.ConnectionDtoManager;
import core.connection.ConnectionManager;
import core.connection.Priority;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

import core.exception.KeepAliveException;

import core.packet.AbstractPacket;
import core.packet.keep_alive.KeepAliveManager;

public class CoordinatorConnectionManager extends ConnectionManager {

    private static final Logger logger = LogManager.getLogger(CoordinatorConnectionManager.class);

    private CoordinatorConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        super(membershipState, instantiatorRole);
    }

    private static volatile CoordinatorConnectionManager instance;

    /** 
     * 
     * @param membershipState The runtime membership state for this instance
     * @param instantiatorRole The Role of the instantiator (In this case: "COORDINATOR")
     * @return a singleton instance of the connectionManager
     */
    public static CoordinatorConnectionManager getInstance(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        return getOrCreateInstance(instance, CoordinatorConnectionManager.class, 
            () -> instance = new CoordinatorConnectionManager(membershipState, instantiatorRole));
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(membershipState, instantiatorRole) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static CoordinatorConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(membershipState, instantiatorRole) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive() throws KeepAliveException {
    
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
