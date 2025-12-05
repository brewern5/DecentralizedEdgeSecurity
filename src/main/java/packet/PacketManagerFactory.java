/*
 *      Author: Nathaniel Brewer
 *
 *      Factory class for creating the appropriate PacketManager based on the received packet type.
 *      This uses the PacketType from a deserialized AbstractPacket to instantiate the correct manager.
 * 
 *      Usage:
 *          // Deserialize packet using Gson with PacketTypeAdapterFactory
 *          AbstractPacket receivedPacket = gson.fromJson(json, AbstractPacket.class);
 *          
 *          // Create appropriate manager based on packet type
 *          AbstractPacketManager manager = PacketManagerFactory.createManager(
 *              receivedPacket, 
 *              myId, 
 *              clusterId, 
 *              role,
 *              connectionManager,  // Optional, only needed for INITIALIZATION
 *              senderIpAddress     // Optional, only needed for INITIALIZATION
 *          );
 *          
 *          // Process the packet
 *          manager.recreateIncomingPacket(receivedPacket);
 *          AbstractPacket response = manager.processIncomingPacket();
 */

package packet;

import connection.ConnectionManager;
import packet.initalization.InitalizationPacketManager;
import packet.keep_alive.KeepAliveManager;
import packet.peerlist_packet.PeerListPacketManager;

public class PacketManagerFactory {

    /**
     * Creates the appropriate PacketManager based on the packet type.
     * The manager will be pre-configured with the received packet.
     * 
     * @param receivedPacket The deserialized packet received from the network
     * @param responderId The ID of the instance creating this manager (to send responses)
     * @param clusterId The cluster ID
     * @param role The role of this instance ("Node", "Server", "Coordinator")
     * @return The appropriate AbstractPacketManager subclass
     * @throws IllegalArgumentException if packet type is unknown or unsupported
     */
    public static AbstractPacketManager createManager(
            AbstractPacket receivedPacket,
            String responderId,
            String clusterId,
            String role) {
        
        return createManager(receivedPacket, responderId, clusterId, role, null, null);
    }

    /**
     * Creates the appropriate PacketManager based on the packet type.
     * This overload includes additional parameters needed for certain packet types.
     * 
     * @param receivedPacket The deserialized packet received from the network
     * @param responderId The ID of the instance creating this manager (to send responses)
     * @param clusterId The cluster ID
     * @param role The role of this instance ("Node", "Server", "Coordinator")
     * @param connectionManager ConnectionManager instance (required for INITIALIZATION packets)
     * @param senderIpAddress IP address from socket connection (required for INITIALIZATION packets)
     * @return The appropriate AbstractPacketManager subclass
     * @throws IllegalArgumentException if packet type is unknown or required parameters are missing
     */
    public static AbstractPacketManager createManager(
            AbstractPacket receivedPacket,
            String responderId,
            String clusterId,
            String role,
            ConnectionManager connectionManager,
            String senderIpAddress) {
        
        if (receivedPacket == null || receivedPacket.getPacketType() == null) {
            throw new IllegalArgumentException("Received packet or packet type cannot be null");
        }

        PacketType packetType = receivedPacket.getPacketType();
        String senderId = receivedPacket.getSenderId();
        
        AbstractPacketManager manager;

        /*
            I Used a switch here because of memory efficiency, which is important in highly distributed systems.
        */
        
        switch (packetType) {
            case INITIALIZATION:
                if (connectionManager == null || senderIpAddress == null) {
                    throw new IllegalArgumentException(
                        "ConnectionManager and senderIpAddress are required for INITIALIZATION packets"
                    );
                }
                manager = new InitalizationPacketManager(
                    responderId,
                    clusterId,
                    senderId,  // recipient is the sender of the incoming packet
                    role,
                    connectionManager,
                    senderIpAddress
                );
                break;
                
            case KEEP_ALIVE:
                manager = new KeepAliveManager(
                    responderId,
                    clusterId,
                    senderId,  // recipient is the sender of the incoming packet
                    role
                );
                break;
                
            case PEER_LIST_REQ:
                manager = new PeerListPacketManager(
                    responderId,
                    clusterId,
                    senderId,  // recipient is the sender of the incoming packet
                    role
                );
                break;
                
            case MESSAGE:
                // TODO: Implement MessageManager when created
                throw new UnsupportedOperationException(
                    "MESSAGE packet type not yet implemented in factory"
                );
                
            case INITIALIZATION_RES:
            case ACK:
            case ERROR:
            case PEER_LIST_RES:
                // These are response packets, typically don't need managers
                throw new IllegalArgumentException(
                    "Response packet types should not be used to create managers: " + packetType
                );
                
            default:
                throw new IllegalArgumentException("Unknown packet type: " + packetType);
        }
        
        // Automatically set the incoming packet in the manager
        manager.recreateIncomingPacket(receivedPacket);
        
        return manager;
    }
    
    /**
     * Checks if a packet type requires a manager.
     * Response packets typically don't need managers.
     * 
     * @param packetType The packet type to check
     * @return true if this packet type should have a manager
     */
    public static boolean requiresManager(PacketType packetType) {
        switch (packetType) {
            case INITIALIZATION:
                return true;
            case KEEP_ALIVE:
            case PEER_LIST_REQ:
                return true;
            case MESSAGE:
                return true;
                
            case INITIALIZATION_RES:
            case ACK:
                return false;
            case ERROR:
            case PEER_LIST_RES:
                return false;
                
            default:
                return false;
        }
    }
}
