/*
 *      Author: Nathaniel Brewer
 * 
 */
package packet.initalization_packet;

import packet.AbstractPacket;
import packet.PacketType;

public class InitalizationPacket extends AbstractPacket{

    public InitalizationPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION, clusterId, recipientId);
    }
}
