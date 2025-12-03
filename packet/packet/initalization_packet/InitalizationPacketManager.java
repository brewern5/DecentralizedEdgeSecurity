/*
 *      Author: Nathaniel Brewer
 * 
 */
package packet.initalization_packet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import packet.AbstractPacket;
import packet.AbstractPacketManager;

import packet.response_packet.ErrorResponse;
import packet.response_packet.InitializationResponse;

import packet.exception.InvalidFormatException;

public class InitalizationPacketManager extends AbstractPacketManager {

    private static final Logger logger = LogManager.getLogger(InitalizationPacketManager.class);

    public InitalizationPacketManager(String instantiatorId, String clusterId, String recipientId) {
        super(instantiatorId, clusterId, recipientId);
    }

    @Override
    public AbstractPacket createPacket() {

        packet = new InitalizationPacket(senderId, clusterId, recipientId);

        return packet;
    }

    @Override
    public AbstractPacket createGoodResponsePacket() {

        responsePacket = new InitializationResponse(senderId, clusterId, recipientId);

        return responsePacket;
    }

    @Override
    public AbstractPacket createBadResponsePacket() {

        responsePacket = new ErrorResponse(senderId, clusterId, recipientId);

        return responsePacket;
    }
    

    /**
     * Handle the incoming packet and generate a resposne packet based on what packet was recieved
     * 
     * @return the generated response packet
     */
    public AbstractPacket processRecievedPacket() throws InvalidFormatException {

        try{

            // Get all the values from the payload
            String[] values = packet.getAllPayloadValues();

            // If there are either less or more than 1 value, throw error - we need only one value
            if(values.length < 1 || values.length > 1) {
                throw new InvalidFormatException("Expected Payload length of 1. Recieved length of " + values.length);
            }

        } catch(InvalidFormatException ife) {
            logger.error("Invalid format in processed packet! {}", ife);
        }

        return responsePacket;
    }

    private boolean validatePayload() {

        // TODO;

        return false;
    }

}
