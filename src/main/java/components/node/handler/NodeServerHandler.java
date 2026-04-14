package components.node.handler;

import java.net.Socket;

import components.node.connections.NodeServerConnectionManager;

import core.connection.ConnectionManager;
import core.handler.AbstractSocketPacketHandler;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Socket handler for packets received by a node from a server.
 */
public class NodeServerHandler extends AbstractSocketPacketHandler {

    private static final Logger logger = LogManager.getLogger(NodeServerHandler.class);

    public NodeServerHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        super(socket, identity, membershipState);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ConnectionManager getInitializationConnectionManager() {
        return NodeServerConnectionManager.getInstance();
    }

    @Override
    protected String remoteTypeLabel() {
        return "Server";
    }
}