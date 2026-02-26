/*
        Author: Nathaniel Brewer

*/
package core.packet.keep_alive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.exception.InvalidFormatException;
import core.exception.UnknownPacketException;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketType;
import core.packet.response_packet.AckResponse;
import core.packet.response_packet.ErrorResponse;

public class KeepAliveManager extends AbstractPacketManager { 
    
    private static final Logger logger = LogManager.getLogger(KeepAliveManager.class);

    private boolean terminate = true;

    /**
     * 
     * @param instantiatorId The id of the node that created this instance
     * @param clusterId The id of the cluster the instantiator is part of
     * @param recipientId The id of the intended reciepient (if there is one)
     * @param role What class (Node, Server, Coordinator) created this instance
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

            responsePacket = createGoodResponsePacket();

        } catch(InvalidFormatException ife) {
            logger.error("Invalid format in processed packet! {}", ife);
            responsePacket = createBadResponsePacket();
        } catch(UnknownPacketException upe) {
            logger.error("Unexpected Packet type! {}", upe);
            responsePacket = createBadResponsePacket();
        } catch(Exception e) {
            logger.error("Unchecked Exception! {}", e);
            responsePacket = createBadResponsePacket();
        }

        return responsePacket;
    }

    @Override
    protected void validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException {

        PacketType incomingPacketType = incomingPacket.getPacketType();

        // See if there is a termination string then handle accordingly
        if(incomingPacketType == PacketType.KEEP_ALIVE) {
            
            // TODO: check termination status and then respond with keep alive packet
                
            

        } else {
            throw new UnknownPacketException("Expected packet type of KEEP_ALIVE. Recieved: " + incomingPacketType.toString());
        }
    }

    public void setTerminationStatus(boolean terminate) {
        this.terminate = terminate;
    }

}
