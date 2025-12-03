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
 *      This is an abstract class that's child classes will handle unique logic. Such as Keep Alive having
 *      the need for a timer.
 * 
 */
package packet;

import java.util.LinkedHashMap;
import java.util.UUID;

import com.google.gson.Gson;    // external library that allows for jsonify of java objects. Located in root/lib 

public abstract class AbstractPacket {

    protected int payloadPairCounter = 0;

    protected String senderId = null;
    protected String clusterId = null;
    protected String recipientId = null;

    protected String packetId = null;

    protected PacketType packetType;     // Enum for easy constant assignment

    protected String timeStamp;

    protected LinkedHashMap<String, String> payload = new LinkedHashMap<>();  
    
    protected AbstractPacket(String instantiatorId, PacketType packetType, String clusterId, String recipientId) {
        this.senderId = instantiatorId;
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

    public String getInstantiatorId() { return senderId; }

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

    public void setInstantiatorId(String senderId) { this.senderId = senderId; }

    public void setClusterId(String clusterId) { this.clusterId = clusterId; }

    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public void setPacketId(String packetId) { this.packetId = packetId; }

    public void setTimeStamp() { this.timeStamp = String.valueOf(System.currentTimeMillis()); }

    public void setPayloadPairCounter(int payloadPairCounter) { this.payloadPairCounter = payloadPairCounter; }

    public void addKeyValueToPayload(String key, String value) { payload.put(key, value); payloadPairCounter++; }

    /*
     *      Stringify Methods
     */

    // converts the packet to a key/value String
    public String toJson() { return new Gson().toJson(this); }

    // adds a clear end of message line that will be handled 
    public String toDelimitedString() { return new Gson().toJson(this) + "||END||"; }
}
