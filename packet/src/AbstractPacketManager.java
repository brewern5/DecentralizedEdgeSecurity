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

package src;

import java.util.LinkedHashMap;
import java.util.Arrays;

public abstract class AbstractPacketManager {
    
    protected AbstractPacket packet;
    protected AbstractPacket responsePacket;
    
    protected String senderId;
    protected String clusterId;
    protected String recipientId;
    
    protected AbstractPacketManager(String senderId, String clusterId, String recipientId) {
        this.senderId = senderId;
        this.clusterId = clusterId;
        this.recipientId = recipientId;
    }

    /*
     *      Abstract Methods
     */
    public abstract AbstractPacket createResponsePacket();
    
    public abstract AbstractPacket createPacket();

    /*  
     *      End Abstraction
     */

    public void addStringValue(String... value) {
        int payloadCounter = packet.getPayloadPairCounter();
        for(String val : value) {
            packet.addKeyValueToPayload("Message" + packet.getPayloadPairCounter(), val);
            payloadCounter++;
        }
    }   

    public String[] getAllPayloadKeys() {

        Object[] objKeys;
        String[] keys;

        LinkedHashMap<String, String> payload = packet.getPayload();

        // Since LinkedHashMap .keySet returns an array of Objects, we need to convert it to an array of strings
        objKeys = payload.keySet().toArray();

        // Copies the obj array to string array
        keys = Arrays.copyOf(objKeys, objKeys.length, String[].class);

        return keys;
    }

    public String[] getAllPayloadValues() {

        Object[] objValues;
        String[] values;

        LinkedHashMap<String, String> payload = packet.getPayload();

        // Since LinkedHashMap .values returns an array of Objects, we need to convert it to an array of strings
        objValues = payload.values().toArray();

        // Copies the obj array to string array
        values = Arrays.copyOf(objValues, objValues.length, String[].class);

        return values;
    } 

    public String getValueByKey(String key) {
        LinkedHashMap<String, String> payload = packet.getPayload();

        return payload.get(key);
    }

    // TODO: Packet Recreation

}
