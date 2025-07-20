/*      Author: Nathaniel Brewer
 * 
 *      THis is an abstract class created to be the catalyst for handling
 *      all the packet types. This allows for overwritting the 
 *      handling method.
 *      
 *      Since this class will only ever instantiated once inside any handler
 *      Payloads will be stored inside the "PayloadKeyValuePairs" Variable to 
 *      easily be instantiated from the 'handle' method which will /Usually/ 
 *      not have to be overwritten. 
 */

package node_handler.node_packet_handler;

import node_packet.NodePacket;

import java.util.*;

public abstract class NodePacketHandler {

    // Stores the recieved payload into a map (key Value) so the 
    protected Map<String, String> PayloadKeyValuePairs = new HashMap<>(); // Protected means any class inside this package can access this variable

    // This is the object that will be instantiated if the packet is handled succesfuly or an error gets thrown
    protected NodeHandlerResponse packetResponse;
    
    // Tears the packet apart and seperates the head from the body
    public NodeHandlerResponse handle(NodePacket recievedPacket){

        // Grab the payload from the packet
        String payload = recievedPacket.getPayload();

        // If the payload is empty , then an error will be sent back to the original packet sender
        if(payload.trim().isEmpty()){
            return new NodeHandlerResponse(
                false, 
                new Exception("No payload Sent."),
                 "Error handling payload for PacketType " + recievedPacket.getPacketType()
            );
        }

        // Splits apart each set of key/value pair
        String[] pairs = payload.split(";");

        // Seperates the Key Value pairs into a pair then puts the pairs into the PayloadKeyValuePair HashMap
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0];
            String value = keyValue.length > 1 ? keyValue[1] : ""; // makes sure the value is always a valid string

            // Stores the key Value pair into the hashMap
            PayloadKeyValuePairs.put(key, value);
        }
        return process();
    }

    /*
     * 
     *      Abstract methods    -    these methods will need to be overwritten by subclasses
     * 
     */

    // Will process the data and handle it according to what packet type is overwritting this method
    public abstract NodeHandlerResponse process();

}
