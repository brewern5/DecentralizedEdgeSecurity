/*
        Author: Nathaniel Brewer

        This will contain the response of a peerlist from the server to the Node
*/

package packet.peerlist_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class PeerListResPacket extends AbstractPacket {
    
    public PeerListResPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.PEER_LIST_RES, clusterId, recipientId);

        // TODO: Populate with the reponse
    }

}
