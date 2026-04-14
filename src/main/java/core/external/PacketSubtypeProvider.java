package core.external;

import core.packet.AbstractPacket;
import core.packet.PacketType;

/**
 * Service-provider contract for registering packet subtypes with Gson polymorphic adapters.
 */
public interface PacketSubtypeProvider {

    /**
     * @return packet type represented by this subtype.
     */
    PacketType packetType();

    /**
     * @return concrete packet class mapped to packetType.
     */
    Class<? extends AbstractPacket> packetClass();

    /**
     * @return wire label used by RuntimeTypeAdapterFactory; defaults to enum name.
     */
    default String typeLabel() {
        return packetType().name();
    }
}
