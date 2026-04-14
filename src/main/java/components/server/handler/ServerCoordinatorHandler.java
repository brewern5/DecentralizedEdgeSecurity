package components.server.handler;

import java.net.Socket;

import components.server.connections.ServerCoordinatorConnectionManager;

import core.connection.ConnectionManager;
import core.handler.AbstractSocketPacketHandler;
import core.identity.AbstractTierIdentity;
import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Socket handler for packets received by a server from its coordinator.
 */
public class ServerCoordinatorHandler extends AbstractSocketPacketHandler {

    private static final Logger logger = LogManager.getLogger(ServerCoordinatorHandler.class);

    public ServerCoordinatorHandler(Socket socket, AbstractTierIdentity identity, RuntimeMembershipState membershipState) {
        super(socket, identity, membershipState);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected ConnectionManager getInitializationConnectionManager() {
        return ServerCoordinatorConnectionManager.getInstance();
    }

    @Override
    protected String remoteTypeLabel() {
        return "Coordinator";
    }

    @Override
    protected void onResponsePacket(AbstractPacket responsePacket) {
        if (responsePacket.getPacketType() == PacketType.INITIALIZATION_RES) {
            membershipState().assignId(responsePacket.getRecipientId());
            membershipState().assignClusterId(responsePacket.getClusterId());
            logger.info(
                "Runtime membership updated from INITIALIZATION_RES: assignedId={}, clusterId={}",
                membershipState().assignedId(),
                membershipState().clusterId()
            );
        }

        super.onResponsePacket(responsePacket);
    }
}