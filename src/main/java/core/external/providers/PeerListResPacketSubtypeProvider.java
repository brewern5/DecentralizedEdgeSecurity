package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.peerlist_packet.PeerListResPacket;

public class PeerListResPacketSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.PEER_LIST_RES;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return PeerListResPacket.class;
    }
}
