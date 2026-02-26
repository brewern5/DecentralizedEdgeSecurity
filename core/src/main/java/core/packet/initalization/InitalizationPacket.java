/*
 *      Author: Nathaniel Brewer
 * 
 */
package core.packet.initalization;

import core.packet.AbstractPacket;
import core.packet.PacketType;

public class InitalizationPacket extends AbstractPacket{

    public InitalizationPacket(String senderId, String clusterId, String recipientId) {
        super(senderId, PacketType.INITIALIZATION, clusterId, recipientId);
    }
}
