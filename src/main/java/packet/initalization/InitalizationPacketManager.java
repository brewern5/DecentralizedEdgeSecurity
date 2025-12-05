/*
 *      Author: Nathaniel Brewer
 * 
 */
package packet.initalization;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import exception.InvalidFormatException;
import exception.UnknownPacketException;

import packet.AbstractPacket;
import packet.AbstractPacketManager;
import packet.PacketType;
import packet.response_packet.ErrorResponse;
import packet.response_packet.InitializationResponse;

import connection.ConnectionDto;
import connection.ConnectionManager;

public class InitalizationPacketManager extends AbstractPacketManager {

    private static final Logger logger = LogManager.getLogger(InitalizationPacketManager.class);
    private final ConnectionManager connectionManager;
    private final String senderIpAddress; // IP from the socket connection

    /**
     * 
     * @param instantiatorId The id of the node that created this instance
     * @param clusterId The id of the cluster the instantiator is part of
     * @param recipientId The id of the intended reciepient (if there is one)
     * @param insantiatorType What type of class (Node, Server, Coordinator) created this instance
     * @param connectionManager The ConnectionManager singleton instance to store connection info
     * @param senderIpAddress The IP address from the socket connection (not from packet payload)
     */
    public InitalizationPacketManager(String instantiatorId, String clusterId, String recipientId, String role, ConnectionManager connectionManager, String senderIpAddress) {
        super(instantiatorId, clusterId, recipientId, role);
        this.connectionManager = connectionManager;
        this.senderIpAddress = senderIpAddress;
    }

    @Override
    public AbstractPacket createOutgoingPacket() {

        outgoingPacket = new InitalizationPacket(senderId, clusterId, recipientId);

        return outgoingPacket;
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
                connection.Priority.CRITICAL // Adjust priority as needed
            );
            connectionManager.addConnection(connectionInfo);
            
            logger.info("Added new connection from initialization packet: {}", incomingPacket.getSenderId());
            
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
    protected boolean validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException {

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
        return false;
    }

    public void addOutgoingPayload(LinkedHashMap<String, String> payload) { outgoingPacket.addPayload(payload); }
}
