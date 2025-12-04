/*
 *      Author: Nathaniel Brewer
 *
 *      Packet class that easily allows for creation of packets that are in the Json Format for
 *      digestability and ease of use.
 * 
 *      I chose the Json format for easy formatting purposes and the preservation of variables
 * 
 *      Gson Docs: https://github.com/google/gson/blob/main/UserGuide.md
 * 
 *      This is an abstract class that's child classes will handle unique logic. Such as heartbeat having
 *      the need for a timer.
 * 
 */

package packet;

import exception.InvalidFormatException;
import exception.UnknownPacketException;

public abstract class AbstractPacketManager {
    
    protected AbstractPacket outgoingPacket; // The packet we are using to send
    protected AbstractPacket incomingPacket; // The packet we recieved and need to handle
    protected AbstractPacket responsePacket; // The response to the incoming packet
    
    protected String senderId;
    protected String clusterId;
    protected String recipientId;

    protected String role;

    /**
     * 
     * @param senderId This instance's ID
     * @param clusterId The cluster ID 
     * @param recipientId The ID of the recieving instance
     * @param role What this particular instance role is. I.e. "Node", "Server", "Coordinator"
     */
    protected AbstractPacketManager(String senderId, String clusterId, String recipientId, String role) {
        this.senderId = senderId;
        this.clusterId = clusterId;
        this.recipientId = recipientId;
        this.role = role;
    }

    /*
     *      Abstract Methods
     */
    
    public abstract AbstractPacket createOutgoingPacket();
    
    public abstract AbstractPacket createGoodResponsePacket();

    public abstract AbstractPacket createBadResponsePacket();

    public abstract void recreateIncomingPacket(AbstractPacket incomingPacket);

    public abstract AbstractPacket processIncomingPacket();

    /**
     * 
     * @return 'True' if the payload is verified as okay, 'False' if there is issues
     * @exception 
     */
    protected abstract boolean validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException;

    /*  
     *      End Abstraction
     */


}
