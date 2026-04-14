package core.packet.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.exception.InvalidFormatException;
import core.exception.UnknownPacketException;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.AbstractPacketManager;
import core.packet.PacketType;
import core.packet.response_packet.AckResponse;
import core.packet.response_packet.ErrorResponse;

/**
 * Manager for MESSAGE packet handling.
 *
 * <p>This manager validates incoming payload structure and acknowledges accepted
 * messages with an ACK packet.
 */
public class MessagePacketManager extends AbstractPacketManager {

    private static final Logger logger = LogManager.getLogger(MessagePacketManager.class);

    public MessagePacketManager(RuntimeMembershipState membershipState, String recipientId, TierRole instantiatorRole) {
        super(membershipState, recipientId, instantiatorRole);
    }

    @Override
    public AbstractPacket createOutgoingPacket() {
        outgoingPacket = new MessagePacket(membershipState, recipientId);
        return outgoingPacket;
    }

    @Override
    public AbstractPacket createGoodResponsePacket() {
        responsePacket = new AckResponse(membershipState, recipientId);
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

    @Override
    public AbstractPacket processIncomingPacket() {
        try {
            String[] values = incomingPacket.getAllPayloadValues();
            validatePayload(values);

            logger.info(
                "Received MESSAGE packet from {} to {} with {} payload values",
                incomingPacket.getInstanceId(),
                incomingPacket.getRecipientId(),
                values.length
            );

            responsePacket = createGoodResponsePacket();
        } catch (InvalidFormatException ife) {
            logger.error("Invalid MESSAGE payload format", ife);
            responsePacket = createBadResponsePacket();
        } catch (UnknownPacketException upe) {
            logger.error("Unexpected packet type in MessagePacketManager", upe);
            responsePacket = createBadResponsePacket();
        } catch (Exception e) {
            logger.error("Unhandled exception while processing MESSAGE", e);
            responsePacket = createBadResponsePacket();
        }

        return responsePacket;
    }

    @Override
    protected void validatePayload(String[] values) throws InvalidFormatException, UnknownPacketException {
        PacketType incomingPacketType = incomingPacket.getPacketType();

        if (incomingPacketType != PacketType.MESSAGE) {
            throw new UnknownPacketException(
                "Expected packet type MESSAGE. Received: " + incomingPacketType
            );
        }

        if (values == null || values.length < 1) {
            throw new InvalidFormatException("MESSAGE packet must include at least one payload value");
        }
    }
}
