/*
 *  Packet class that easily allows for creation of packets that are in the Json Format for
 *  digestability and ease of use.
 * 
 *  I chose the Json format for easy formatting purposes and the preservation of variables
 *  Gson Docs: https://github.com/google/gson/blob/main/UserGuide.md
 * 
 *  Need to find an approach that will tell the reciever when the end of the packet is reached:
 *          - Delimited approach (adding clear ending to the message such as: ||END||);
 *          - Add character counter, this seems like too much to run    
 * 
 */
package packet;

import java.io.*;

import com.google.gson.Gson;    // external library that allows for jsonify of java objects. Located in root/lib 

public class ServerPacket {

    private ServerPacketType packetType;     // Enum for easy constant assignment
    private String sender;
    private String payload;

    public ServerPacket() {} // No-args constructor

    public ServerPacket(ServerPacketType packetType, String sender, String payload) {
        this.packetType = packetType;
        this.sender = sender;
        this.payload = payload;
    }

    /*          Accessor/setter methods         */

    public ServerPacketType getPacketType() {
        return packetType;
    }
    public void setPacketType(ServerPacketType packetType) {
        this.packetType = packetType;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    // converts the packet to a key/value String
    public String toString() {
        return new Gson().toJson(this);
    }



    
    //
    //
    //      NOT IMPLEMENTED
    //
    //
    // adds a clear end of message line that will be handled 
    public String toDelimitedString() {
        return new Gson().toJson(this) + "||END||";
    }

    
}
