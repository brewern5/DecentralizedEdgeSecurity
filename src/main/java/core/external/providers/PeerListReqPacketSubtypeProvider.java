package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.peerlist_packet.PeerListReqPacket;

public class PeerListReqPacketSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.PEER_LIST_REQ;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return PeerListReqPacket.class;
    }
}
