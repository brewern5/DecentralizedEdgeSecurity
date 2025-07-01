/*
 *      Author: Nate Brewer
 * 
 *      This is the SuperClass any packet sender for the server. Since the server will
 *      be sending packets to the coordinator(up) and to the nodes(down), each one
 *      will have their own packet sender object assigned to them, or their own 
 *      "Sender".
 * 
 *      A child of this class will be the a generic sender that will have grandchildren
 *      of their own. Each grandchild will be a 'Sender' for each individual packet type.
 *      However, this generic sender will hold the socket and act as the main point of
 *      sending. 
 * 
 *      Each of the packet types (maybe excluding the data packet since there could
 *      be different types) will be instantiated during the init process so they can 
 *      easily be accessed, modified, and sent since they will be stored in memory.
 * 
 *      An example of how this will work, we will follow the packet type of INITIALIZE.
 *      When the server starts up, the "PacketSender" will be instantiated(this is 
 *      the child of this class) this will hold the socket and be the main point of
 *      sending as previously stated, then a grandchild will be constructed for the 
 *      INITALIZE that will send the inital connection information. This will inherit
 *      the send command that will send through the socket that is constructed with 
 *      the child of the class you are current reading this in.
 * 
 * 
 *       will be made with the coordinator
 *      (meaning that a init sender)
 */
package sender;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;

import packet.*;

public abstract class Sender {

    // A timer for retry and for other time based sending
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected Socket socket;    // Socket that sends the packet - will be instantiated on initalization

    protected final Gson gson = new Gson();

    // Abstract Constructor - Will instantiate in the child (PacketSender)
    //public Sender(Socket setupSocket){
    //    this.socket = setupSocket;
    //}

    
    /*
     *      
     *          Abstract methods
     * 
     */

    // The packetSender will declare 
    public abstract void retry();

    // Used by the grandchildren of this class (The PacketTypeSender classes)
    public abstract ServerPacket getSendPacket();


    // This will send the completed packet
    public boolean send(){

        try{
            //If the acknowledgement is not recieved then it will call upon retry (if the child class uses a retry method)
            boolean ackReceived = false;

            // This is where the server will wait for a proper Ack from the coordinator - if not received, will retry 3 times
            String json = getSendPacket().toDelimitedString();        // jsonifies the packet to be sent

            // Initalized inside nested control structure
            ServerPacket responsePacket;

                // Creates the input and the output for the socket.
            PrintWriter output = new PrintWriter(
                this.socket.getOutputStream(), 
                true
            );
            // Sends the packet through the socket to the Coordinator
            output.println(json);

            // Will be where the response is read from
            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.socket.getInputStream())
            );

            // Retrieves the response packet from the Coordinator
            String response = input.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            try{
                if(!response.endsWith("||END||") || response == null){
                    throw new IllegalArgumentException("\n\nPayload not properly terminated. \n\tPossible Causes:\n\t\t- Incomplete Packet\n\t\t- Unsafe Packet\n");
                }
                System.out.println("\t\t\tResponse recieved\n");
                // If true, the packet will remove the delimiter so it can properly deserialize the Json (Since ||END|| is not json)
                    response = response.substring(
                0, 
                    response.length() - "||END||".length()
                 );

                // Will handle the response packet and look for errors, if packetType = ACK then good to contiue initalization
                responsePacket = gson.fromJson(response, ServerPacket.class);

                // Print out the packet 
                System.out.println(
                    "Sender: \t" + responsePacket.getSender() 
                    + "\n\nPacket Type: \t" + responsePacket.getPacketType() 
                    + "\n\nPayload: \t" + responsePacket.getPayload()  
                    + "\n\n"
                );

                // If the packet type is a ACK packet - then it is a good connection made and the server will close this socket.
                if (responsePacket.getPacketType() != ServerPacketType.ACK) {
                    throw new IllegalStateException("\n\nExpected ACK packet, but received: " + responsePacket.getPacketType());
                }

                ackReceived = true; // Break out of while loop to contiune initalization
            } catch(IllegalArgumentException illegalArg) {
                // The exception if the packet is empty or has no termination 
                 System.err.println("Error: " + illegalArg.getMessage());
                illegalArg.printStackTrace();
            } catch(IllegalStateException illegalState) {
                // The exception if the packet is not of the type ACK
                System.err.println("Error: " + illegalState.getMessage());
                illegalState.printStackTrace();
            } finally {
                // Always close the ports no matter the success status. 
                output.close();          //
                input.close();          // Close the port, we cannot reuse the same socket connection if a retry is needed
                this.socket.close();   //
            }
                
            // Return the true/false value of the packet being recieved
            return ackReceived;
                
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Log critical error
        }

        // Return false as if this section is reached, the packet was not sent properly meanign an error
        return false;
    }
}
