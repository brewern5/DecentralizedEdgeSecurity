/*
 *      Author: Nathaniel Brewer
 *
 *      Packet class that easily allows for creation of packets that are in the Json Format for
 *      digestability and ease of use.
 * 
 *      I chose the Json format for easy formatting purposes and the preservation of variables
 *      Gson Docs: https://github.com/google/gson/blob/main/UserGuide.md
 * 
 *      This is an abstract class that's child classes will handle unique logic. Such as heartbeat having
 *      the need for a timer.
 * 
 */
package components.coordinator.packet;

import java.util.LinkedHashMap;
import java.util.Arrays;

import com.google.gson.Gson;

public abstract class CoordinatorPacket {

    protected int payloadPairCounter = 0;
    
    protected CoordinatorPacketType packetType;
    protected LinkedHashMap<String, String> payload;

    protected String id = null;

    public CoordinatorPacket() {}

    /*
     *      Packet Type Methods 
     */

    public CoordinatorPacketType getPacketType() {
        return packetType;
    }
    public void setPacketType(CoordinatorPacketType packetType) {
        this.packetType = packetType;
    }

    /*
     *      ID Methods
     */

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /*
     *      Payload methods
     */

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

    public void addKeyValueToPayload(String key, String value) {
        payload.put(key, value);
    }

    public String[] getAllPayloadKeys() {

        Object[] objKeys;
        String[] keys;

        objKeys = payload.keySet().toArray();

        keys = Arrays.copyOf(objKeys, objKeys.length, String[].class);

        return keys;
    }

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
     *      Stringify Methods
     */

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String toDelimitedString() {
        return new Gson().toJson(this) + "||END||";
    }
}
