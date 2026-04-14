package components.server.handler;

import java.net.Socket;

import components.server.connections.ServerNodeConnectionManager;

import core.connection.ConnectionManager;
import core.handler.AbstractSocketPacketHandler;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Socket handler for packets received by a server from nodes.
 */
public class ServerNodeHandler extends AbstractSocketPacketHandler {
 
    private static final Logger logger = LogManager.getLogger(ServerNodeHandler.class);
 
    public ServerNodeHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        super(socket, identity, membershipState);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ConnectionManager getInitializationConnectionManager() {
        return ServerNodeConnectionManager.getInstance();
    }

    @Override
    protected String remoteTypeLabel() {
        return "Node";
    }
}