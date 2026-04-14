package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.response_packet.AckResponse;

public class AckResponseSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.ACK;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return AckResponse.class;
    }
}
