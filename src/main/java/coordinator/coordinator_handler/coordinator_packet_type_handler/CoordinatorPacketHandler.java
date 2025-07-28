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

package coordinator_handler.coordinator_packet_type_handler;

import java.util.*;

import coordinator_packet.CoordinatorPacket;

public abstract class CoordinatorPacketHandler {

    // Stores the recieved payload into a map (key Value) so the 
    protected LinkedHashMap<String, String> PayloadKeyValuePairs = new LinkedHashMap<>(); // Protected means any class inside this package can access this variable

    // This is the object that will be instantiated if the packet is handled succesfuly or an error gets thrown
    protected CoordinatorHandlerResponse packetResponse;
    
    // Tears the packet apart and seperates the head from the body
    public CoordinatorHandlerResponse handle(CoordinatorPacket recievedPacket){

        // Print out the packet 
        System.out.println(
            "Sender: \t" + recievedPacket.getSender() 
            + "\n\nPacket Type: \t" + recievedPacket.getPacketType() 
            + "\n\nPayload: \t" + recievedPacket.getPayload()  
            + "\n\n"
        );

        // Grab the payload from the packet
         LinkedHashMap<String, String> payload = recievedPacket.getPayload();

        // If the payload is empty , then an error will be sent back to the original packet sender
        if(payload.isEmpty()){
            return new CoordinatorHandlerResponse(
                false, 
                new Exception("No payload Sent."),
                 "Error handling payload for PacketType " + recievedPacket.getPacketType()
            );
        }
        return process();
    }

    /*
     * 
     *      Abstract methods    -    these methods will need to be overwritten by subclasses
     * 
     */

    // Will process the data and handle it according to what packet type is overwritting this method
    public abstract CoordinatorHandlerResponse process();

}
