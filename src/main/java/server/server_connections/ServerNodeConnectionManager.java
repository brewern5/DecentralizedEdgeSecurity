/*
 *      Author: Nathaniel Brewer
 * 
 *      Since The server is handling information from both the Coordinator and the Nodes
 *      It needs to have two differnet storages for connections. This is the simpler way 
 *      than refactoring the Superclass of the handler
 */
package server.server_connections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import connection.ConnectionDtoManager;
import connection.ConnectionManager;
import connection.Priority;

import packet.AbstractPacket;
import packet.keep_alive.KeepAliveManager;

public class ServerNodeConnectionManager extends ConnectionManager {

    private static final Logger logger = LogManager.getLogger(ServerNodeConnectionManager.class);
    
    private ServerNodeConnectionManager(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role);
    }
 
    private static volatile ServerNodeConnectionManager instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static ServerNodeConnectionManager getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, ServerNodeConnectionManager.class, 
            () -> instance = new ServerNodeConnectionManager(instanceId, clusterId, role));
    }

        /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
    public static ServerNodeConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    public boolean sendKeepAlive() {
        
        activeConnections.forEach((connectionId, connection) -> {

            boolean keptAlive = false;

            AbstractPacket packet = new KeepAliveManager(
                instanceId, 
                clusterId, 
                connectionId, 
                role
            ).createOutgoingPacket();

            keptAlive = new ConnectionDtoManager(connection).send(packet);

            if(connection.getPriority() != Priority.CRITICAL) {
                logger.warn("Connection: \"{}\" with criticality status : \"{}\" was terminated! ", connectionId, connection.getPriority());
                terminateConnection(connectionId);
            } else{
                // Failed to contact connection 
                if(!keptAlive) {
                    terminateConnection(connectionId); 
                    logger.warn("Connection: \"{}\" with criticality status : \"{}\" could not be kept alive! ", connectionId, connection.getPriority());
                }
            }
        });
        return false;
    }

}
