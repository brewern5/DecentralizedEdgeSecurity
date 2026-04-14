package core.packet.providers;

import core.packet.AbstractPacketManager;
import core.packet.PacketManagerProvider;
import core.packet.PacketProcessingContext;
import core.packet.PacketType;
import core.packet.initalization.InitalizationPacketManager;

/**
 * Provider for INITIALIZATION packet managers.
 */
public class InitalizationPacketManagerProvider implements PacketManagerProvider {

    @Override
    public PacketType supportsType() {
        return PacketType.INITIALIZATION;
    }

    @Override
    public AbstractPacketManager create(PacketProcessingContext context) {
        if (context.connectionManager() == null) {
            throw new IllegalArgumentException("ConnectionManager is required for INITIALIZATION packets");
        }
        if (context.senderIpAddress() == null || context.senderIpAddress().isBlank()) {
            throw new IllegalArgumentException("senderIpAddress is required for INITIALIZATION packets");
        }

        return new InitalizationPacketManager(
            context.membershipState(),
            context.receivedPacket().getInstanceId(),
            context.instantiatorRole(),
            context.connectionManager(),
            context.senderIpAddress()
        );
    }

    @Override
    public boolean requiresConnectionContext() {
        return true;
    }
}
