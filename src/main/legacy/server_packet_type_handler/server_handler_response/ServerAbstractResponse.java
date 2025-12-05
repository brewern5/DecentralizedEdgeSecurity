/*
 *      Author - Nathaiel Brewer
 * 
 */

package server.server_handler.server_packet_type_handler.server_handler_response;

import java.util.LinkedHashMap;


public class ServerAbstractResponse {
    
    // If successful, this will be True. If unsuccessful, then False
    protected boolean success;

    // The message will provide details on what went wrong if anything at all
    protected LinkedHashMap<String, String> messages;

    // If there is an exception, it will be stored here and sent along side the message
    protected LinkedHashMap<String, String> exceptions;

    // This is the counter that gets appended to the messages (e.g. Message + 1 = Message1)
    // We are storing this globally here. Since more messages can get added after, we want to save the #
    protected int messageCounter = 0;
    protected int exceptionCounter = 0;


    // Used for errors - will print to the console to describe issues
    public void printMessages(){
        // Loops through all the stored messages and will print them to the main terminal
        messages.forEach( (key, value) -> {
            System.out.println("Key: " + key + " - Value: " + value);
        });
    }

        // Changes the success message to indicate a positive/negative success factor, meaning the 
    public void setSuccess(boolean success){
        this.success = success;
    }

    /*
     * 
     *      Adders
     * 
     */

    // If any new messages arrive in the stack, before this is sent back to the original sender, they will be added here
    public void addMessage(String... message){
        storePayload(message);
    }

    // Stores a string-ified version of the exeption inside the response message
    public void addException(Exception... exception) {
        for (Exception exc : exception) {
            this.exceptions.put("Exception" + exceptionCounter, exc.toString());
            exceptionCounter++;
        }
    }

    public void addCustomKeyValuePair(String key, String value) {
        messages.put(key, value);
    }

    // Used to send this to the payload and make it Json-ified to be sent
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        // Loop through each individual message in the 'message' array
        this.messages.forEach( (key, value) -> {
             sb.append(key).append("; ").append(value);    // Add ; between the each key/value pair

            // TODO: Check this output for seperation of each Key Value Pair
        });
        if (!exceptions.isEmpty()) {
            sb.append(':');
            this.exceptions.forEach( (key, value) -> {
                sb.append("Exception - ").append(key + ": ").append(value.toString());
            });
        }
        return sb.toString().trim();
    }

    // Converts all the messages to the arraylist for storage
    protected void storePayload(String... message) {
        for (String msg : message) {
            this.messages.put("Message" + messageCounter, msg);
            messageCounter++;
        }
    }

    // Store exceptions in the message map if available
    public LinkedHashMap<String, String> combineMaps() {

        // Will check for empty exceptions map, if not empty then it will add to 
        if(!exceptions.isEmpty()){
            exceptions.forEach( (key, value) -> {
                messages.put(key, value);
            });
        }
        return messages;
    }

        /*
     * 
     *      Getters
     *  
     */
    // Standard get methods
    public boolean getSuccess(){ return success; }   

    // Returns the list of messages
    public LinkedHashMap<String, String> getMessageMap(){ return messages; }

    public LinkedHashMap<String, String> getExceptionMap(){ return exceptions; }

}
