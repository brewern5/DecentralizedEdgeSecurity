/*
        Author: Nathaniel Brewer


        This manager will handle both the response and the request for the peer list. 
*/
package packet.peerlist_packet;

import java.util.LinkedHashMap;

import exception.InvalidFormatException;
import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.response_packet.ErrorResponse;

public class PeerListPacketManager extends AbstractPacketManager {
    
    public PeerListPacketManager(String senderId, String clusterId, String recipientId, String role) {
        super(senderId, clusterId, recipientId, role);
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

    @Override
    public AbstractPacket processRecievedPacket() throws InvalidFormatException{
        
        return packet;
    }

    protected Boolean validatePayload(LinkedHashMap<String, String> payload) {
        // TODO: Implement validation for peer list packets
        return true;
    }

    protected boolean validatePayload() {

        return false;
    }

}
