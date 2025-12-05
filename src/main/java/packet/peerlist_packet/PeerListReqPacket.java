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

package packet.peerlist_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class PeerListReqPacket extends AbstractPacket {

    public PeerListReqPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.PEER_LIST_REQ, clusterId, recipientId);

        // TODO: Populate the payload with the req
    }

}
