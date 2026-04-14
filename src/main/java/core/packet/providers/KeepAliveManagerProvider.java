package core.packet.providers;

import core.packet.AbstractPacketManager;
import core.packet.PacketManagerProvider;
import core.packet.PacketProcessingContext;
import core.packet.PacketType;
import core.packet.keep_alive.KeepAliveManager;

/**
 * Provider for KEEP_ALIVE packet managers.
 */
public class KeepAliveManagerProvider implements PacketManagerProvider {

    @Override
    public PacketType supportsType() {
        return PacketType.KEEP_ALIVE;
    }

    @Override
    public AbstractPacketManager create(PacketProcessingContext context) {
        return new KeepAliveManager(
            context.membershipState(),
            context.receivedPacket().getInstanceId(),
            context.instantiatorRole()
        );
    }
}
