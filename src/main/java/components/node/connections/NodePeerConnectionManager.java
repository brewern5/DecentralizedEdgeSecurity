/*
    Author: Nathaniel Brewer

    Extension of package 'connection.ConnectionManager'

    Manages all peer connections with the node
*/
package components.node.connections;

import core.connection.ConnectionManager;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

public class NodePeerConnectionManager extends ConnectionManager {

    private NodePeerConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        super(membershipState, instantiatorRole);
    }

    private static volatile NodePeerConnectionManager instance;

    /** 
     * 
     * @param membershipState The runtime membership state for this instance
     * @param instantiatorRole The Role of the instantiator (In this case: "Coordinator")
     * @return a singleton instance of the connectionManager
     */
    public static NodePeerConnectionManager getInstance(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        return getOrCreateInstance(instance, NodePeerConnectionManager.class,
            () -> instance = new NodePeerConnectionManager(membershipState, instantiatorRole)
        );
    }

    /**
     * Gets the existing singleton instance. Must call getInstance(membershipState, role) first.
     * @return the singleton instance
     * @throws IllegalStateException if getInstance with parameters hasn't been called yet
     */
        public static NodePeerConnectionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConnectionManager not initialized. Call getInstance(membershipState, role) first.");
        }
        return instance;
    }

    @Override
    public boolean sendKeepAlive() {
        throw new UnsupportedOperationException("This operation for class: " + this.getClass() + "is not supported!");
    }
}
