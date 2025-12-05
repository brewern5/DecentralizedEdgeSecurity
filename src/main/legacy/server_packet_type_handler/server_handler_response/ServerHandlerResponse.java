/*      
 *      Author: Nathaniel Brewer     
 *      
 *      This object will define the success/failure message of handling a packet
 *      of any type. 
 * 
 *      For example
 *          If a packetType of INITALIZATION sends a preferred port and no errors
 *          occur in handling the payload on the receiving end, then "success" = True
 *          and the "message" will be something along the lines of "Good request"
 * 
 *          If a packetType of INITALIZATION does not send a preferred port or an
 *          error occurs, then "success" = False and the message will provide adequate
 *          details on what went wrong alongside what exception that may have risen from    
 *          the potential error
 *          
 */

package server.server_handler.server_packet_type_handler.server_handler_response;

import java.util.LinkedHashMap;

public class ServerHandlerResponse extends ServerAbstractResponse {

    public ServerHandlerResponse(boolean success) {
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); 
        this.messages = new LinkedHashMap<>();
    }

    // Since there may be multiple messages, I am using Varargs since we do not know how many messages may be sent (if there is multiple)
    public ServerHandlerResponse(boolean success, String... message){
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); 
        this.messages = new LinkedHashMap<>();
        storePayload(message);
    }

    // Overloaded constructor in the case there are exceptions thrown.
    public ServerHandlerResponse(boolean success, Exception exception, String... message){
        this.success = success;
        this.exceptions = new LinkedHashMap<>(); 
        this.messages = new LinkedHashMap<>();
        addException(exception);
        storePayload(message);
    }

}