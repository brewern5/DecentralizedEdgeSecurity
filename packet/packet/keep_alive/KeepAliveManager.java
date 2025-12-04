/*
        Author: Nathaniel Brewer

*/
package packet.keep_alive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exception.InvalidFormatException;
import packet.AbstractPacket;
import packet.AbstractPacketManager;

import packet.response_packet.AckResponse;
import packet.response_packet.ErrorResponse;

public class KeepAliveManager extends AbstractPacketManager { 
    
    private static final Logger logger = LogManager.getLogger(KeepAliveManager.class);

    private boolean terminate = true;

    /**
     * 
     * @param instantiatorId The id of the node that created this instance
     * @param clusterId The id of the cluster the instantiator is part of
     * @param recipientId The id of the intended reciepient (if there is one)
     * @param insantiatorType What class (Node, Server, Coordinator) created this instance
     */
    public KeepAliveManager(String instantiatorId, String clusterId, String recipientId, String role) {
        super(instantiatorId, clusterId, recipientId, role);
    }

    @Override
    public AbstractPacket createOutgoingPacket() {

        if(terminate) {
            outgoingPacket = new KeepAlivePacket(senderId, clusterId, recipientId, true);
        } else{
            outgoingPacket = new KeepAlivePacket(senderId, clusterId, recipientId, false);
        }

        return outgoingPacket;
    }

    @Override
    public AbstractPacket createGoodResponsePacket() {

        responsePacket = new AckResponse(senderId, clusterId, recipientId);

        return responsePacket;
    }

    @Override
    public AbstractPacket createBadResponsePacket() {

        responsePacket = new ErrorResponse(senderId, clusterId, recipientId);

        return responsePacket;
    }
    
    @Override
    public void recreateIncomingPacket(AbstractPacket incomingPacket) {
        this.incomingPacket = incomingPacket;
    }

        /**
     * Handle the incoming packet and generate a resposne packet based on what packet was recieved
     * 
     * @return the generated response packet
     */
    @Override
    public AbstractPacket processIncomingPacket() {

        try{

            // Get all the values from the payload
            String[] values = incomingPacket.getAllPayloadValues();

            validatePayload(values);

        } catch(InvalidFormatException ife) {
            logger.error("Invalid format in processed packet! {}", ife);
            responsePacket = createBadResponsePacket();
        } catch(Exception e) {
            logger.error("Unchecked Exception! {}", e);
            responsePacket = createBadResponsePacket();
        }

        return responsePacket;
    }

    @Override
    protected boolean validatePayload(String[] values) throws InvalidFormatException {


        return false;
    }

    public void setTerminationStatus(boolean terminate) {
        this.terminate = terminate;
    }

}
