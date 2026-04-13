/*
 *      Author: Nathaniel Brewer
 *
 *      Packet class that easily allows for creation of packets that will be transformed into Json Format for
 *      digestability and ease of use.
 * 
 *      This is an abstract class that's child classes will handle unique logic. Such as Keep Alive having
 *      the need for a timer.
 * 
 */
package core.packet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.google.gson.Gson;

public abstract class AbstractPacket {

    protected int payloadPairCounter = 0;

    protected String instanceId = null;
    protected String clusterId = null;
    protected String recipientId = null;

    protected String packetId = null;

    protected PacketType packetType; 

    protected String timeStamp;

    // This is where all information will be stored that is either in the request or response format.
    // NO IDs should be stored here, only information to be handled
    protected LinkedHashMap<String, String> payload = new LinkedHashMap<>();  
    /**
     * 
     * @param instanceId The id of the instance that is sending the packet 
     * @param packetType The enum type of the packet
     * @param clusterId The id of the cluster this instance belongs to
     * @param recipientId The id of the intended recipient of the packet
     */
    protected AbstractPacket(String instanceId, PacketType packetType, String clusterId, String recipientId) {
        this.instanceId = instanceId;
        this.packetType = packetType;
        this.clusterId = clusterId;
        this.recipientId = recipientId;

        this.packetId = UUID.randomUUID().toString();

        this.setTimeStamp();
        
    }

    /*
     *      Getters
     */

    public PacketType getPacketType() { return packetType; }

    public String getInstanceId() { return instanceId; }

    public String getClusterId() { return clusterId; }

    public String getRecipientId() { return recipientId; }

    public String getPacketId() { return packetId; }

    public String getTimeStamp() { return timeStamp; }
    
    public LinkedHashMap<String, String> getPayload() { return payload; }
    
    public int getPayloadPairCounter() { return payloadPairCounter; }
    
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
    
    /*
     *      Mutators
     */

    public void setPacketType(PacketType packetType) { this.packetType = packetType; }

    public void setSenderId(String senderId) { this.instanceId = senderId; }

    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public void setPacketId(String packetId) { this.packetId = packetId; }

    public void setTimeStamp() { this.timeStamp = String.valueOf(System.currentTimeMillis()); }

    public void setPayloadPairCounter(int payloadPairCounter) { this.payloadPairCounter = payloadPairCounter; }

    public void addKeyValueToPayload(String key, String value) { payload.put(key, value); payloadPairCounter++; }

    public void addStringValue(String... value) {
        for(String val : value) {
            addKeyValueToPayload("Message" + payloadPairCounter, val);
        }
    }    

    public void addPayload(LinkedHashMap<String, String> payload) { this.payload = payload; }

    /*
        Stringify Methods
    */

    public String toJson() { return new Gson().toJson(this); }

    public String toDelimitedString() { return new Gson().toJson(this) + "||END||"; }

}
