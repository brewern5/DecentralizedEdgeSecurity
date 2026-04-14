package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.response_packet.ErrorResponse;

public class ErrorResponseSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.ERROR;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return ErrorResponse.class;
    }
}
