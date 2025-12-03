/*
 *      Author: Nathaniel Brewer
 *
 *      Enumeration for easily setting the packetType with visualizable header options
 * 
 *      Setup example
 *          EdgePacket packet = new EdgePacket(PacketType.MESSAGE, "Sender", "Payload");
 * 
 *      Will pull these and use a switch statement to handle them accordingly.
 *      For example, AUTH will be sent to whatever auth system we decide to use 
 * 
 */
package packet;

public enum PacketType {
    INITIALIZATION,     // For handshake or setup - Will send the preferred listening port 
    INITIALIZATION_RES, // Response for the initalization packet
    MESSAGE,            // Generic text/data message
    KEEP_ALIVE,         // Keep-alive or ping
    ERROR,              // Error or exception reporting
    ACK,                // Acknowledgement of receipt
    PEER_LIST_REQ,      // Request of peer list from the servers
    PEER_LIST_RES       // Response of the peer list
}