/*
 *      Author: Nathaniel Brewer
 * 
 *      Since The server is handling information from both the Coordinator and the Nodes
 *      It needs to have two differnet storages for connections. This is the simpler way 
 *      than refactoring the Superclass of the handler
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

public class ServerNodeConnectionManager extends ConnectionManager {

    private static final Logger logger = LogManager.getLogger(ServerNodeConnectionManager.class);
    
    private ServerNodeConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        super(membershipState, instantiatorRole);
    }
 
    private static volatile ServerNodeConnectionManager instance;

    /** 
     * 
     * @param membershipState The DTO for the membership of this device 
     * @param instantiatorRole The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static ServerNodeConnectionManager getInstance(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        return getOrCreateInstance(instance, ServerNodeConnectionManager.class, 
            () -> instance = new ServerNodeConnectionManager(membershipState, instantiatorRole));
    }

        /**
     * Gets the existing singleton instance. Must call getInstance(membershipState, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static ServerNodeConnectionManager getInstance() {
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
                    logger.warn("Connection: \"{}\" with criticality status: \"{}\" could not be kept alive!", 
                               connectionId, connection.getPriority());
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
