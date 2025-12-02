/*
 *      Author: Nathaniel Brewer
 * 
 */
package src.initalization_packet;

import src.AbstractPacket;
import src.PacketType;

public class InitalizationPacket extends AbstractPacket{

    public InitalizationPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION, clusterId, recipientId);
    }
}
