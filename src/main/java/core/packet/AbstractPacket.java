package core.packet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.google.gson.Gson;

/**
 * Base packet model shared by all request and response packet types.
 *
 * <p>All packets are serialized as JSON and terminated with the {@code ||END||}
 * delimiter when sent over sockets. Subclasses should only add payload semantics,
 * not transport logic.
 *
 * <p>Identity fields:
 * <ul>
 *   <li>{@code instanceId}: sender instance ID at send time</li>
 *   <li>{@code recipientId}: intended target instance ID</li>
 *   <li>{@code clusterId}: cluster scope associated with sender</li>
 *   <li>{@code packetId}: unique packet UUID generated on construction</li>
 * </ul>
 *
 * <p>Payload is represented as ordered key-value pairs to preserve write order.
 */
public abstract class AbstractPacket {

    protected int payloadPairCounter = 0;

    protected String instanceId = null;
    protected String clusterId = null;
    protected String recipientId = null;

    protected String packetId = null;

    protected PacketType packetType; 

    protected String timeStamp;

    // Payload should contain message data only; transport and identity fields are separate members.
    protected LinkedHashMap<String, String> payload = new LinkedHashMap<>();  

    /**
     * Constructs a packet with identity metadata and an auto-generated packet ID.
     *
     * @param instanceId sender instance ID
     * @param packetType packet type discriminator
     * @param clusterId cluster ID for sender context
     * @param recipientId intended recipient instance ID
     */
    protected AbstractPacket(String instanceId, PacketType packetType, String clusterId, String recipientId) {
        this.instanceId = instanceId;
        this.packetType = packetType;
        this.clusterId = clusterId;
        this.recipientId = recipientId;

        this.packetId = UUID.randomUUID().toString();

        this.setTimeStamp();
        
    }

    /**
     * @return packet type discriminator.
     */
    public PacketType getPacketType() { return packetType; }

    /**
     * @return sender instance ID.
     */
    public String getInstanceId() { return instanceId; }

    /**
     * @return sender cluster ID.
     */
    public String getClusterId() { return clusterId; }

    /**
     * @return intended recipient instance ID.
     */
    public String getRecipientId() { return recipientId; }

    /**
     * @return globally unique packet ID.
     */
    public String getPacketId() { return packetId; }

    /**
     * @return Unix epoch milliseconds (string) when packet was created.
     */
    public String getTimeStamp() { return timeStamp; }
    
    /**
     * @return ordered payload key-value map.
     */
    public LinkedHashMap<String, String> getPayload() { return payload; }
    
    /**
     * @return number of payload entries added through helper methods.
     */
    public int getPayloadPairCounter() { return payloadPairCounter; }
    
    /**
     * Returns payload values in insertion order.
     *
     * <p>Use this for manager validation when only values are relevant.
     * Prefer getPayload() when keys matter semantically.
     */
    public String[] getAllPayloadValues() {

        Object[] objValues;
        String[] values;

        objValues = payload.values().toArray();

        values = Arrays.copyOf(objValues, objValues.length, String[].class);

        return values;
    } 
    
    public String getValueByKey(String key) {
        return payload.get(key);
    }

    public void setPacketType(PacketType packetType) { this.packetType = packetType; }

    public void setSenderId(String senderId) { this.instanceId = senderId; }

    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public void setPacketId(String packetId) { this.packetId = packetId; }

    public void setTimeStamp() { this.timeStamp = String.valueOf(System.currentTimeMillis()); }

    public void setPayloadPairCounter(int payloadPairCounter) { this.payloadPairCounter = payloadPairCounter; }

    /**
     * Adds one payload entry and increments payload counter.
     */
    public void addKeyValueToPayload(String key, String value) { payload.put(key, value); payloadPairCounter++; }

    /**
     * Adds values under generated Message keys in sequence.
     */
    public void addStringValue(String... value) {
        for(String val : value) {
            addKeyValueToPayload("Message" + payloadPairCounter, val);
        }
    }    

    public void addPayload(LinkedHashMap<String, String> payload) { this.payload = payload; }

    /**
     * @return JSON representation of this packet.
     */
    public String toJson() { return new Gson().toJson(this); }

    /**
     * @return JSON packet terminated with network delimiter.
     */
    public String toDelimitedString() { return new Gson().toJson(this) + "||END||"; }

}
