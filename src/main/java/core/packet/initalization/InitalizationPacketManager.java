/*
 *      Author: Nathaniel Brewer
 * 
 */
package core.packet.initalization;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.connection.ConnectionDto;
import core.connection.ConnectionManager;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

import core.exception.InvalidFormatException;
import core.exception.UnknownPacketException;

import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketType;
import core.packet.response_packet.ErrorResponse;
import core.packet.response_packet.InitializationResponse;

public class InitalizationPacketManager extends AbstractPacketManager {

    private static final Logger logger = LogManager.getLogger(InitalizationPacketManager.class);
    private final ConnectionManager connectionManager;
    private final String senderIpAddress;

    /**
     * 
     * @param membershipState The DTO for the membership of this device
     * @param recipientId The id of the intended reciepient (if there is one)
     * @param instantiatorRole What type of class Enum[NODE, SERVER, COORDINATOR] created this instance
     * @param connectionManager The ConnectionManager singleton instance to store connection info
     * @param senderIpAddress The IP address from the socket connection (not from packet payload)
     */
    public InitalizationPacketManager(RuntimeMembershipState membershipState, String recipientId, TierRole instantiatorRole, ConnectionManager connectionManager, String senderIpAddress) {
        super(membershipState, recipientId, instantiatorRole);
        this.connectionManager = connectionManager;
        this.senderIpAddress = senderIpAddress;
    }

    @Override
    public AbstractPacket createOutgoingPacket() {

        outgoingPacket = new InitalizationPacket(membershipState, recipientId);

        return outgoingPacket;
    }

    @Override
    public AbstractPacket createGoodResponsePacket() {
        
        logger.debug("Creating response packet - senderId: {}, clusterId: {}, recipientId: {}", 
                    membershipState, recipientId);

        responsePacket = new InitializationResponse(membershipState, recipientId);

        return responsePacket;
    }

    @Override
    public AbstractPacket createBadResponsePacket() {

        responsePacket = new ErrorResponse(membershipState, recipientId);

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

            // Retrieve the port from the payload
            int port = Integer.parseInt(values[0]);

            // Create ID for the new connection
            recipientId = UUID.randomUUID().toString();
            logger.info("Assigned new connection with ID \" {} \".", recipientId);

            // Create connection DTO and add to the connection manager
            // Use the IP from the socket connection, not from the packet
            ConnectionDto connectionInfo = new ConnectionDto(
                recipientId,
                senderIpAddress,  // From socket connection (trusted source)
                port,
                core.connection.Priority.CRITICAL // Adjust priority as needed
            );
            connectionManager.addConnection(connectionInfo);
            
            logger.info("Added new connection from initialization packet: {}", incomingPacket.getInstanceId());
            
            responsePacket = createGoodResponsePacket();
 
        } catch(InvalidFormatException ife) {
            logger.error("Invalid format in processed packet! {}", ife);
            responsePacket = createBadResponsePacket();
        } catch(UnknownPacketException upe) {
            logger.error("Unexpected Packet Type! {}", upe);
        } catch(Exception e) {
            logger.error("Unchecked Exception! {}", e);
            responsePacket = createBadResponsePacket();
        }

        return responsePacket;
    }

    @Override
    protected void validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException {

        PacketType incomingPacketType = incomingPacket.getPacketType();

        if(incomingPacketType == PacketType.INITIALIZATION) {
            // If there are either less or more than 1 value, throw error - we need only one value
            if(values.length < 1 || values.length > 1) {
                throw new InvalidFormatException("Expected Payload length of 1. Recieved length of " + values.length);
            }
    
            // Check if the payload is an integer, implying it is a port
            try {
                Integer.parseInt(values[0]);
            } catch(NumberFormatException nfe) {
                throw new InvalidFormatException("Expected Integer, recieved value: " + values[0]);
            }
        } else if(incomingPacketType == PacketType.INITIALIZATION_RES) {
            if(values.length < 1 || values.length > 1) {
                throw new InvalidFormatException("Expected Payload length of 0. Recieved length of " + values.length);
            }  
        } else {
            throw new UnknownPacketException("Excpected Packet of type INITALIZATION or INITALIZATION_RES. Recieved: " + incomingPacketType.toString());
        }
    }

    public void addOutgoingPayload(LinkedHashMap<String, String> payload) { outgoingPacket.addPayload(payload); }
}
