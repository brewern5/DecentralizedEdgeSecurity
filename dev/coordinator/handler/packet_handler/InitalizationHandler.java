/*
 * 
 *      This is the handler for the Packet Type INITALIZATION. This extends the Abstract
 *      class 'Packet handler'
 * 
 *      The handle method splits the recieved packet's Payload's Json into a Java HashMap
 *      variable called "PayloadKeyValuePair" (HashMap is still Key Value pair just makes it
 *      accessable). This variable is declared in the SuperClass 'PacketHandler'
 * 
 *      The recieved packet will be instansiated inside the CoordinatorServerHandler and 
 *      then sent here when the payload will be handled accordingly
 * 
 *      TODO: figure out response packet handling
 * 
 *      Response packet will be generated in the CoordinatorServerHandler
 */
package handler.packet_handler;

import config.CoordinatorConfig;

public class InitalizationHandler extends PacketHandler{

    // Allows writting to the config file
    private CoordinatorConfig config = new CoordinatorConfig();

    @Override
    public HandlerResponse process() {

        try{
            // Lambda function - HashMap has a ForEach function that receives the all the keys(k) and their corresponding values(v) and will loop through each one individually and send it to the config
            PayloadKeyValuePairs.forEach( (k, v) -> { config.writeToConfig(k, v); });

            packetResponse = new HandlerResponse(true, "Preferred Port Recieved");

        } catch(Exception e) {
            System.err.println("Error Handling packet");
            packetResponse = new HandlerResponse(false, e, "Error Handling Packet.");
        }
        return packetResponse;
    }
}