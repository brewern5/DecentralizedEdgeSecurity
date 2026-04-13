/*
        Author: Nathaniel Brewer

        This will contain the response of a peerlist from the server to the Node
*/

package core.packet.peerlist_packet;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class PeerListResPacket extends AbstractPacket {
    
    public PeerListResPacket(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.PEER_LIST_RES, membershipState.clusterId(), recipientId);

        // TODO: Populate with the reponse
    }

}
