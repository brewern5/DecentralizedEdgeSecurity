/*
        Author: Nathaniel Brewer


        This manager will handle both the response and the request for the peer list. 
*/
package packet.peerlist_packet;

import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.response_packet.ErrorResponse;

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
    public AbstractPacket createGoodResponsePacket() {
        responsePacket = new PeerListResPacket(senderId, clusterId, recipientId);

        return responsePacket;
    }
    
    @Override
    public AbstractPacket createBadResponsePacket() {

        packet = new ErrorResponse(senderId, clusterId, recipientId);

        return packet;
    }

}
