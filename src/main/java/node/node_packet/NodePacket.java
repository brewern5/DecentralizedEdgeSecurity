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
package node.node_packet;

import java.util.LinkedHashMap;
import java.util.Arrays;

import com.google.gson.Gson;    // external library that allows for jsonify of java objects. Located in root/lib 

public abstract class NodePacket {

    protected int payloadPairCounter = 0;

    protected NodePacketType packetType;     // Enum for easy constant assignment
    protected String sender;
    protected LinkedHashMap<String, String> payload;    

    public NodePacket() {} // No-args constructor

    /*          Accessor/setter methods         */

    public NodePacketType getPacketType() {
        return packetType;
    }
    public void setPacketType(NodePacketType packetType) {
        this.packetType = packetType;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public LinkedHashMap<String, String> getPayload() {
        return payload;
    }
    public void setPayload(LinkedHashMap<String, String> payload) {
        this.payload = payload;
    }

    public void addStringValue(String... value) {
        for(String val : value) {
            payload.put("Message" + payloadPairCounter, val);
            payloadPairCounter++;
        }
    }   

    // converts the packet to a key/value String
    public String toJson() {
        return new Gson().toJson(this);
    }

    // adds a clear end of message line that will be handled 
    public String toDelimitedString() {
        return new Gson().toJson(this) + "||END||";
    }

    public void addKeyValueToPayload(String key, String value) {
        payload.put(key, value);
    }

    public String[] getAllPayloadKeys() {

        Object[] objKeys;
        String[] keys;

        // Since LinkedHashMap .keySet returns an array of Objects, we need to convert it to an array of strings
        objKeys = payload.keySet().toArray();

        // Copies the obj array to string array
        keys = Arrays.copyOf(objKeys, objKeys.length, String[].class);

        return keys;
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
}
