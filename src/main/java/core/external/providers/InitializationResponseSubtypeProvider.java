package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.response_packet.InitializationResponse;

public class InitializationResponseSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.INITIALIZATION_RES;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return InitializationResponse.class;
    }
}
