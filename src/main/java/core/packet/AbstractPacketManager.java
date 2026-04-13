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

package core.packet;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

import core.exception.InvalidFormatException;
import core.exception.UnknownPacketException;

public abstract class AbstractPacketManager {
    
    protected AbstractPacket outgoingPacket; 
    protected AbstractPacket incomingPacket; 
    protected AbstractPacket responsePacket; 
    
    protected RuntimeMembershipState membershipState;
    protected String recipientId;

    protected TierRole instantiatorRole;

    /**
     * 
     * @param membershipState The DTO for the membership of this device 
     * @param recipientId The ID of the recieving instance
     * @param instantiatorRole What this particular instance role is. I.e. "NODE", "SERVER", "COORDINATOR"
     */
    protected AbstractPacketManager(RuntimeMembershipState membershipState, String recipientId, TierRole instantiatorRole) {
        this.membershipState = membershipState;
        this.recipientId = recipientId;
        this.instantiatorRole = instantiatorRole;
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
     * @param values - the payload of the incoming packet  
     * @exception InvalidFormatException - Packet has incorrect format such as no delimiter.
     * @exception UnknownPacketException - Packet type is not of a type that can be handled by the concrete manager.
     */
    protected abstract void validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException;

    /*  
     *      End Abstraction
     */


}
