/*
 *      Author: Nathaniel Brewer
 * 
 */
package src.initalization_packet;

import src.AbstractPacket;
import src.AbstractPacketManager;
import src.response_packet.InitializationResponse;

public class InitalizationPacketManager extends AbstractPacketManager {

    public InitalizationPacketManager(String senderId, String clusterId, String recipientId) {
        super(senderId, clusterId, recipientId);
    }

    @Override
    public AbstractPacket createPacket() {

        packet = new InitalizationPacket(senderId, clusterId, recipientId);

        return packet;
    }

    @Override
    public AbstractPacket createResponsePacket() {

        responsePacket = new InitializationResponse(senderId, clusterId, recipientId);

        return responsePacket;
    }

}
