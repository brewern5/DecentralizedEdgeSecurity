/*
        Author: Nathaniel Brewer


        This manager will handle both the response and the request for the peer list. 
*/
package core.packet.peerlist_packet;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.response_packet.ErrorResponse;

public class PeerListPacketManager extends AbstractPacketManager {
    
    /**
     * 
     * @param membershipState The DTO for the membership of this device 
     * @param recipientId The id of the intended reciepient (if there is one)
     * @param instantiatorRole What type of class (Node, Server, Coordinator) created this instance
     */
    public PeerListPacketManager(RuntimeMembershipState membershipState, String recipientId, TierRole instantiatorRole) {
        super(membershipState, recipientId, instantiatorRole);
    }

    @Override
    // Creates the Peer List Request packet
    public AbstractPacket createOutgoingPacket() {
        outgoingPacket = new PeerListReqPacket(membershipState, recipientId);
        return outgoingPacket;
    }

    
    @Override
    public AbstractPacket createGoodResponsePacket() {
        responsePacket = new PeerListResPacket(membershipState, recipientId);

        return responsePacket;
    }
    
    @Override
    public AbstractPacket createBadResponsePacket() {

        responsePacket = new ErrorResponse(membershipState, recipientId);

        return responsePacket;
    }

    @Override
    public AbstractPacket processIncomingPacket(){
        
        return responsePacket;
    }

    @Override
    public void recreateIncomingPacket(AbstractPacket incomingPacket) {
        this.incomingPacket = incomingPacket;
    }

    @Override
    protected void validatePayload(String[] values) {
        // TODO: Implement validation for peer list packets
    }

}
