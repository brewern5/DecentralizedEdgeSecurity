package components.node.handler;

import java.net.Socket;

import components.node.connections.NodePeerConnectionManager;

import core.connection.ConnectionManager;
import core.handler.AbstractSocketPacketHandler;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Socket handler for peer-to-peer packet traffic received by a node.
 */
public class NodePeerHandler extends AbstractSocketPacketHandler {

    private static final Logger logger = LogManager.getLogger(NodePeerHandler.class);

    public NodePeerHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        super(socket, identity, membershipState);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ConnectionManager getInitializationConnectionManager() {
        return NodePeerConnectionManager.getInstance();
    }

    @Override
    protected String remoteTypeLabel() {
        return "Peer";
    }
}