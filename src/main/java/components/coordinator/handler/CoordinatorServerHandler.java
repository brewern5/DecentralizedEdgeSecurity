package components.coordinator.handler;

import java.net.Socket;
import components.coordinator.connections.CoordinatorConnectionManager;

import core.connection.ConnectionManager;
import core.handler.AbstractSocketPacketHandler;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
/**
 * Socket handler for packets received by the coordinator from servers.
 */
public class CoordinatorServerHandler extends AbstractSocketPacketHandler {

    private static final Logger logger = LogManager.getLogger(CoordinatorServerHandler.class);

    public CoordinatorServerHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        super(socket, identity, membershipState);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ConnectionManager getInitializationConnectionManager() {
        return CoordinatorConnectionManager.getInstance();
    }

    @Override
    protected String remoteTypeLabel() {
        return "Server";
    }
}