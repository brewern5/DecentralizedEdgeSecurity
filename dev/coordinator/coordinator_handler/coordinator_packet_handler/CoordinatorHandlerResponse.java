/*      Author: Nathaniel Brewer     
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

package coordinator_handler.coordinator_packet_handler;

import java.util.List;
import java.util.ArrayList;

public class CoordinatorHandlerResponse {

    // If successful, this will be True. If unsuccessful, then False
    private boolean success;

    // The message will provide details on what went wrong if anything at all
    private List<String> message = new ArrayList<>();

    // If there is an exception, it will be stored here and sent along side the message
    private Exception exception;

    // Since there may be multiple messages, I am using Varargs since we do not know how many messages may be sent (if there is multiple)
    public CoordinatorHandlerResponse(boolean success, String... messages){
        this.success = success;
        this.exception = null;
        // Converts all the messages to the arraylist for storage
        for (String msg : messages) {
            this.message.add(msg);
        }
    }

    // Overloaded constructor in the case there are exceptions thrown.
    public CoordinatorHandlerResponse(boolean success, Exception exception, String... messages){
        this.success = success;
        this.exception = exception;
        // Converts all the messages to the arraylist for storage
        for (String msg : messages) {
            this.message.add(msg);
        }
    }

    // Used for errors - will print to the console to describe issues
    public void printMessages(){

        // Loops through all the stored messages and will print them to the main terminal
        for(String msg : message) {
            System.err.println(msg);
        }
    }

    // Used to send this to the payload and make it Json-ified to be sent
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        // Loop through each individual message in the 'message' array
        for (String msg : message) {
            sb.append(msg).append("; ");    // Add ; between the each key/value pair
        }
        if (exception != null) {
            sb.append("Exception: ").append(exception.getMessage());
        }
        return sb.toString().trim();
    }

    public String toDelimitedString(){
        return this.toString() + "||END||";
    }

    // Changes the success message to indicate a positive/negative success factor, meaning the 
    public void setSuccess(boolean success){
        this.success = success;
    }

    // If any new messages arrive in the stack, before this is sent back to the original sender, they will be added here
    public void addMessage(String... message){
        // Loops through each of the sent messages (if there is more than one) and adds the 
        for(String msg : message){
            this.message.add(msg);
        }
    }

    // If an exception arrises, after the construction of the handler response, it will be sent here
    public void setException(Exception exception){
        this.exception = exception;
    }

    // Standard get methods
    public boolean getSuccess(){
        return success;
    }

    // Returns the list of messages
    public List getMessage(){
        return message;
    }

    public Exception getException(){
        return exception;
    }
}