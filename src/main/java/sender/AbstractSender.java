package sender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.SocketTimeoutException;

// This will allow for Jsonification of packets before sending
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import external.PacketTypeAdapterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import packet.AbstractPacket;
import packet.PacketType;
public abstract class AbstractSender {
    
    protected static final Logger logger = LogManager.getLogger(AbstractSender.class);

    // A timer for retry and for other time based sending
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected Socket socket;    // Socket that sends the packet - will be instantiated on initalization

    // Stores the ip and the sending port to recreate the socket
    protected String ip;
    protected int sendingPort;

    // Gson instance configured with packet type adapter for proper serialization/deserialization
    protected static final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
        .create();

    /*
            Abstract Methods
    */
    public abstract boolean retry(AbstractPacket packet);

    /*
            End Abstraction
    */

    public void startSocket() throws IOException, SocketTimeoutException {
        socket = new Socket(
            ip,
            sendingPort
        );
        socket.setSoTimeout(1000);
    }

    /* Deserializes JSON response into the appropriate packet subtype.
     * Uses the centralized PacketTypeAdapterFactory which handles all packet types.
     */
    public AbstractPacket deserializePacket(String json) {
        return gson.fromJson(json, AbstractPacket.class);
    }

    // This will send the completed packet
    public boolean send(AbstractPacket packet){

        try{
            startSocket();  

            //If the acknowledgement is not recieved then it will call upon retry (if the child class uses a retry method)
            boolean ackReceived = false;

            String json = packet.toDelimitedString();     

            // Initalized inside nested control structure
            AbstractPacket responsePacket;

            // Creates the input and the output for the socket.
            PrintWriter output = new PrintWriter(
                this.socket.getOutputStream(), 
                true
            );

            // Sends the packet through the socket to the Coordinator
            output.println(json);
            output.flush();
            logger.info("Sending packet of type: {}", packet.getPacketType());

            // Will be where the response is read from
            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.socket.getInputStream())
            );

            // Retrieves the response packet from the Coordinator
            String response = input.readLine();

            // Checks if the payload is properly terminated. If not, the packet is incomplete or an unsafe packet was sent
            try{
                if(response == null){
                    throw new Exception("No Response Packet Received!");
                }
                else if(!response.endsWith("||END||")){
                    throw new IllegalArgumentException(
                        "Payload not properly terminated. "
                        + "\tPossible Causes:\n\t"
                        + "- Incomplete Packet\n\t"
                        + "- Unsafe Packet"
                    );
                }

                // If true, the packet will remove the delimiter so it can properly deserialize the Json (Since ||END|| is not json)
                response = response.substring(
                    0, 
                    response.length() - "||END||".length()
                );

                // Pre-parses the Json in order to grab the packetType to use for the switch statement
                JsonObject jsonObj = JsonParser.parseString(response).getAsJsonObject();
                String responseJson = jsonObj.toString();

                // Sends the the json and the packetType to be built by Gson dynamically
                responsePacket = deserializePacket(responseJson);

                // Print out the packet 
                logger.info(
                    "Response Recieved:"
                    + "\n\tSender ID:\t" + responsePacket.getSenderId() 
                    + "\n\tPacket Type:\t" + responsePacket.getPacketType() 
                );
                /* Deserialize the JSON response into the appropriate packet type
                responsePacket = (AbstractPacket) deserializePacket(response);
                            EdgeServer.setServerId(v);
                        }
                    });
                */

                // If the packet type is a ACK packet - then it is a good connection made and the coordinator will close this socket.
                if (responsePacket.getPacketType() != PacketType.ACK) {
                    throw new IllegalStateException("Expected ACK packet, but received: " + responsePacket.getPacketType());
                } else {
                    ackReceived = true; // Break out of while loop to contiune initalization
                }
            } catch(IllegalArgumentException illegalArg) {
                // The exception if the packet is empty or has no termination 
                logger.error("Error: " + illegalArg);
            } catch(IllegalStateException illegalState) {
                // The exception if the packet is not of the type ACK
                logger.error("Error: " + illegalState);
            } finally {
                // Always close the ports no matter the success status. 
                output.close();          //
                input.close();          // Close the port, we cannot reuse the same socket connection if a retry is needed
                this.socket.close();   //
            }
                
            // Return the true/false value of the packet being recieved
            return ackReceived;
                
        } catch(SocketTimeoutException e) {
            logger.error("Failed waiting on a response from coordinator at " + this.ip + ":" + this.sendingPort + "\n" + e);
        } catch(IOException e) {
            logger.error("Failed to connect to coordinator at " + this.ip + ":" + this.sendingPort + "\n" + e);
        } catch (Exception e) {
            logger.error("Unknown Error! " + e);
        }

        // Return false as if this section is reached, the packet was not sent properly meaning an error occurred early i
        return false;
    }

}
