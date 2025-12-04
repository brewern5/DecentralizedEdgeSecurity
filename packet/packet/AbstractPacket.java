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
package packet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.google.gson.Gson;

public abstract class AbstractPacket {

    protected int payloadPairCounter = 0;

    protected String senderId = null;
    protected String clusterId = null;
    protected String recipientId = null;

    protected String packetId = null;

    protected PacketType packetType;     // Enum for easy constant assignment

    protected String timeStamp;

    // This is where all information will be stored that is either in the request or response format.
    // NO IDs should be stored here, only information to be handled
    protected LinkedHashMap<String, String> payload = new LinkedHashMap<>();  
    
    protected AbstractPacket(String senderId, PacketType packetType, String clusterId, String recipientId) {
        this.senderId = senderId;
        this.packetType = packetType;
        this.clusterId = clusterId;
        this.recipientId = clusterId;

        // Create packetId for backtracking
        this.packetId = UUID.randomUUID().toString();

        // Create Time stamp
        this.setTimeStamp();
        
    }

    /*
     *      Getters
     */

    public PacketType getPacketType() { return packetType; }

    public String getSenderId() { return senderId; }

    public String getClusterId() { return clusterId; }

    public String getRecipientId() { return recipientId; }

    public String getPacketId() { return packetId; }

    public String getTimeStamp() { return timeStamp; }
    
    public LinkedHashMap<String, String> getPayload() { return payload; }

    public int getPayloadPairCounter() { return payloadPairCounter; }

    
    /*
     *      Mutators
     */

    public void setPacketType(PacketType packetType) { this.packetType = packetType; }

    public void setSenderId(String senderId) { this.senderId = senderId; }

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

    public String[] getAllPayloadValues() {

        Object[] objValues;
        String[] values;

        // Since LinkedHashMap .values returns an array of Objects, we need to convert it to an array of strings
        objValues = payload.values().toArray();

        // Copies the obj array to string array
        values = Arrays.copyOf(objValues, objValues.length, String[].class);

        return values;
    } 
    
    public String getValueByKey(String key) {
        return payload.get(key);
    }

    /*
        Stringify Methods
    */

    // converts the packet to a key/value String
    public String toJson() { return new Gson().toJson(this); }

    // adds a clear end of message line that will be handled 
    public String toDelimitedString() { return new Gson().toJson(this) + "||END||"; }

    
}
