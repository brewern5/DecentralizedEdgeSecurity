package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.initalization.InitalizationPacket;

public class InitalizationPacketSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.INITIALIZATION;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return InitalizationPacket.class;
    }
}
