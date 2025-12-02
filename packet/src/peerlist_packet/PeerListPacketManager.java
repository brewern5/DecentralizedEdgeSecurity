/*
        Author: Nathaniel Brewer


        This manager will handle both the response and the request for the peer list. 
*/
package src.peerlist_packet;

import src.AbstractPacket;
import src.AbstractPacketManager;

public class PeerListPacketManager extends AbstractPacketManager {
    
    public PeerListPacketManager(String senderId, String clusterId, String recipientId) {
        super(senderId, clusterId, recipientId);
    }

    @Override
    // Creates the Peer List Request packet
    public AbstractPacket createPacket() {
        packet = new PeerListReqPacket(senderId, clusterId, recipientId);
        return packet;
    }

    @Override
    public AbstractPacket createResponsePacket() {
        responsePacket = new PeerListResPacket(senderId, clusterId, recipientId);
        return responsePacket;
    }

}
