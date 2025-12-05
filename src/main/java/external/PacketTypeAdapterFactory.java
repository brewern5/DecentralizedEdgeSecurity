package external;
/*
 *      Author: Nathaniel Brewer
 *
 *      Factory class for registering all packet types with Gson's RuntimeTypeAdapterFactory.
 *      This ensures proper serialization/deserialization of AbstractPacket subclasses.
 * 
 *      Usage:
 *          Gson gson = new GsonBuilder()
 *              .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
 *              .create();
 */

import com.google.gson.TypeAdapterFactory;

import packet.AbstractPacket;
import packet.initalization.InitalizationPacket;
import packet.response_packet.AckResponse;
import packet.response_packet.ErrorResponse;
import packet.response_packet.InitializationResponse;
import packet.keep_alive.KeepAlivePacket;
import packet.peerlist_packet.PeerListReqPacket;
import packet.peerlist_packet.PeerListResPacket;

public class PacketTypeAdapterFactory {

    /**
     * Creates and configures a RuntimeTypeAdapterFactory for all packet types.
     * The type field "packetType" will be used to determine which packet class to instantiate.
     * 
     * @return TypeAdapterFactory configured with all packet types
     */
    public static TypeAdapterFactory create() {
        return RuntimeTypeAdapterFactory.of(AbstractPacket.class, "packetType")
            .registerSubtype(InitalizationPacket.class, "INITIALIZATION")
            .registerSubtype(InitializationResponse.class, "INITIALIZATION_RES")
            .registerSubtype(KeepAlivePacket.class, "KEEP_ALIVE")
            .registerSubtype(ErrorResponse.class, "ERROR")
            .registerSubtype(AckResponse.class, "ACK")
            .registerSubtype(PeerListReqPacket.class, "PEER_LIST_REQ")
            .registerSubtype(PeerListResPacket.class, "PEER_LIST_RES");
    }

    /**
     * Creates a RuntimeTypeAdapterFactory that maintains the type field in deserialized objects.
     * Useful for debugging or when you need to preserve the type field.
     * 
     * @return TypeAdapterFactory configured with all packet types and type field preservation
     */
    public static TypeAdapterFactory createWithTypeField() {
        return RuntimeTypeAdapterFactory.of(AbstractPacket.class, "packetType", true)
            .registerSubtype(InitalizationPacket.class, "INITIALIZATION")
            .registerSubtype(InitializationResponse.class, "INITIALIZATION_RES")
            .registerSubtype(KeepAlivePacket.class, "KEEP_ALIVE")
            .registerSubtype(ErrorResponse.class, "ERROR")
            .registerSubtype(AckResponse.class, "ACK")
            .registerSubtype(PeerListReqPacket.class, "PEER_LIST_REQ")
            .registerSubtype(PeerListResPacket.class, "PEER_LIST_RES");
    }
}
