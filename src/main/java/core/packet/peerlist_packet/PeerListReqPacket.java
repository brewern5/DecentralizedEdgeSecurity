/*
        Author: Nathaniel Brewer

        This will hold a new Node's request to the server to send a peer list to it

        will contain the following in the reqeuest:
            SenderId
            ClusterId
            Timestamp
            RequestId
            ReciepientId (to whom we are sending the packet to)
*/

package core.packet.peerlist_packet;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;
import core.packet.PacketType;

public class PeerListReqPacket extends AbstractPacket {

    public PeerListReqPacket(RuntimeMembershipState membershipState, String recipientId) {
        super(membershipState.assignedId(), PacketType.PEER_LIST_REQ, membershipState.clusterId(), recipientId);

        // TODO: Populate the payload with the req
    }

}
