/*
    Author: Nathaniel Brewer

    Extension of package 'connection.ConnectionManager'

    Manages all peer connections with the node
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

public class NodePeerConnectionManager extends ConnectionManager {
    
    private static final Logger logger = LogManager.getLogger(NodePeerConnectionManager.class);

    public NodePeerConnectionManager(String instanceId, String clusterId, String role) {
        super(instanceId, clusterId, role);
    }

    private static volatile NodePeerConnectionManager instance;

    /** 
     * 
     * @param instanceId The ID of the instance that is creating this connection manager
     * @param clusterId The ID of the cluster this instance belongs too, if it belongs to one
     * @param role The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static NodePeerConnectionManager getInstance(String instanceId, String clusterId, String role) {
        return getOrCreateInstance(instance, NodePeerConnectionManager.class,
            () -> instance = new NodePeerConnectionManager(instanceId, clusterId, role)
        );
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(instanceId, clusterId, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
        public static NodePeerConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(instanceId, clusterId, role) first.");
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive() {
        throw new UnsupportedOperationException("This operation for class: " + this.getClass() + "is not supported!");
    }
}
