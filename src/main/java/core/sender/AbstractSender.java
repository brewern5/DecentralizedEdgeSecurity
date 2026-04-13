/*
    Author: Nathaniel Brewer

    Abstract sender for all packets and their managers. Generic sending rules apply to all 3 tiers. retry needs to be defined for each of the 3-tier
*/
package core.sender;

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

import core.external.PacketTypeAdapterFactory;
import core.packet.AbstractPacket;
import core.packet.PacketType;

import org.apache.logging.log4j.Logger;
public abstract class AbstractSender {

    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected Socket socket;

    protected String ip;
    protected int sendingPort;

    protected String assignedId;

    protected static final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(PacketTypeAdapterFactory.create())
        .create();

    /*
            Abstract Methods
    */
    public abstract boolean retry(AbstractPacket packet);
    protected abstract Logger getLogger();
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

    public boolean send(AbstractPacket packet){

        try{
            startSocket();  

            boolean ackReceived = false;

            String json = packet.toDelimitedString();     

            AbstractPacket responsePacket;

            PrintWriter output = new PrintWriter(
                this.socket.getOutputStream(), 
                true
            );

            output.println(json);
            output.flush();
            getLogger().info("Sending packet of type: {}", packet.getPacketType());

            BufferedReader input = new BufferedReader(
                new InputStreamReader(this.socket.getInputStream())
            );

            String response = input.readLine();

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

                response = response.substring(
                    0, 
                    response.length() - "||END||".length()
                );

                JsonObject jsonObj = JsonParser.parseString(response).getAsJsonObject();
                String responseJson = jsonObj.toString();

                responsePacket = deserializePacket(responseJson);

                getLogger().info(
                    "Response Recieved:"
                    + "\n\tSender ID:\t" + responsePacket.getSenderId() 
                    + "\n\tPacket Type:\t" + responsePacket.getPacketType() 
                    + "\n\tAssigned ID:\t" + responsePacket.getRecipientId()
                );
                /* Deserialize the JSON response into the appropriate packet type
                responsePacket = (AbstractPacket) deserializePacket(response);
                            EdgeServer.setServerId(v);
                        }
                    });
                */

                if(responsePacket.getPacketType() == PacketType.INITIALIZATION_RES) {
                    String assignedId = responsePacket.getRecipientId();
                    this.assignedId = assignedId; 
                    getLogger().info("Received assigned ID: {}", assignedId);
                    ackReceived = true;
                }
                else if (responsePacket.getPacketType() != PacketType.ACK) {
                    throw new IllegalStateException("Expected ACK or INITIALIZATION_RES packet, but received: " + responsePacket.getPacketType());
                } else {
                    ackReceived = true;
                }
            } catch(IllegalArgumentException illegalArg) {
                getLogger().error("Error: " + illegalArg);
            } catch(IllegalStateException illegalState) {
                getLogger().error("Error: " + illegalState);
            } finally {
            
                output.close();          
                input.close();          
                this.socket.close();   
            }
            return ackReceived;
                
        } catch(SocketTimeoutException e) {
            getLogger().error("Failed waiting on a response from coordinator at " + this.ip + ":" + this.sendingPort + "\n" + e);
        } catch(IOException e) {
            getLogger().error("Failed to connect to coordinator at " + this.ip + ":" + this.sendingPort + "\n" + e);
        } catch (Exception e) {
            getLogger().error("Unknown Error! " + e);
        }

        return false;
    }

    public String getAssignedId() { return assignedId; }

}
