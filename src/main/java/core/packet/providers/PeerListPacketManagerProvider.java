package core.packet.providers;

import core.packet.AbstractPacketManager;
import core.packet.PacketManagerProvider;
import core.packet.PacketProcessingContext;
import core.packet.PacketType;
import core.packet.peerlist_packet.PeerListPacketManager;

/**
 * Provider for PEER_LIST_REQ packet managers.
 */
public class PeerListPacketManagerProvider implements PacketManagerProvider {

    @Override
    public PacketType supportsType() {
        return PacketType.PEER_LIST_REQ;
    }

    @Override
    public AbstractPacketManager create(PacketProcessingContext context) {
        return new PeerListPacketManager(
            context.membershipState(),
            context.receivedPacket().getInstanceId(),
            context.instantiatorRole()
        );
    }
}
