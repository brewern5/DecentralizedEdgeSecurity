package core.external.providers;

import core.external.PacketSubtypeProvider;
import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.packet.message.MessagePacket;

public class MessagePacketSubtypeProvider implements PacketSubtypeProvider {

    @Override
    public PacketType packetType() {
        return PacketType.MESSAGE;
    }

    @Override
    public Class<? extends AbstractPacket> packetClass() {
        return MessagePacket.class;
    }
}
