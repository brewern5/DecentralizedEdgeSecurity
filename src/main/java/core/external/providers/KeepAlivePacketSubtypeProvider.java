package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.keep_alive.KeepAlivePacket;

public class KeepAlivePacketSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.KEEP_ALIVE;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return KeepAlivePacket.class;
    }
}
