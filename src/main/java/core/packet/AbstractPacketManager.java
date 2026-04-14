package core.packet;

import core.identity.RuntimeMembershipState;
import core.identity.TierRole;

import core.exception.InvalidFormatException;
import core.exception.UnknownPacketException;

/**
 * Base contract for packet processing logic.
 *
 * <p>A concrete manager owns one packet type's behavior for outgoing packet creation,
 * incoming validation, and response generation.
 *
 * <p>Implementation guidance for extensibility:
 * <ul>
 *   <li>Keep transport concerns out of managers. Socket I/O belongs to handlers.</li>
 *   <li>Validate payload shape in validatePayload before processing business logic.</li>
 *   <li>Return a non-null response packet when a request expects acknowledgement.</li>
 * </ul>
 */
public abstract class AbstractPacketManager {
    
    protected AbstractPacket outgoingPacket; 
    protected AbstractPacket incomingPacket; 
    protected AbstractPacket responsePacket; 
    
    protected RuntimeMembershipState membershipState;
    protected String recipientId;

    protected TierRole instantiatorRole;

    /**
     * @param membershipState runtime membership state of current instance
     * @param recipientId recipient for outgoing or response packet construction
     * @param instantiatorRole role of current runtime instance
     */
    protected AbstractPacketManager(RuntimeMembershipState membershipState, String recipientId, TierRole instantiatorRole) {
        this.membershipState = membershipState;
        this.recipientId = recipientId;
        this.instantiatorRole = instantiatorRole;
    }

    /**
     * Builds an outgoing request packet.
     */
    public abstract AbstractPacket createOutgoingPacket();

    /**
     * Builds a successful response packet.
     */
    public abstract AbstractPacket createGoodResponsePacket();

    /**
     * Builds an error response packet.
     */
    public abstract AbstractPacket createBadResponsePacket();

    /**
     * Rehydrates manager state from an incoming packet payload.
     */
    public abstract void recreateIncomingPacket(AbstractPacket incomingPacket);

    /**
     * Processes the incoming packet and returns the response packet.
     */
    public abstract AbstractPacket processIncomingPacket();

    /**
     * Validates incoming payload values before business logic is executed.
     *
     * @param values payload values extracted from incoming packet
     * @throws InvalidFormatException if payload structure is invalid
     * @throws UnknownPacketException if packet type is not supported by this manager
     */
    protected abstract void validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException;

}
