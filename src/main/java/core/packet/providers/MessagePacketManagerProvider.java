package core.packet.providers;

import core.packet.AbstractPacketManager;
import core.packet.PacketManagerProvider;
import core.packet.PacketProcessingContext;
import core.packet.PacketType;
import core.packet.message.MessagePacketManager;

/**
 * Provider for MESSAGE packet managers.
 */
public class MessagePacketManagerProvider implements PacketManagerProvider {

    @Override
    public PacketType supportsType() {
        return PacketType.MESSAGE;
    }

    @Override
    public AbstractPacketManager create(PacketProcessingContext context) {
        return new MessagePacketManager(
            context.membershipState(),
            context.receivedPacket().getInstanceId(),
            context.instantiatorRole()
        );
    }
}
