/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the DTO for any individual connections that this layer may have. This will be the object that gets created
 *      any time a new connection is made. All these objects will be stored in memory since permanence is not a concern here.
 *      
 *      All will have expiry times in which either a connection will dropped or a KeepAlive will be sent from the manager to
 *      the connection. This will be dependent on the status of the connection. For example, one with a CRITICAL status will 
 *      try to be kept alive.
 * 
 */
package server.server_connections;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import server.server_sender.ServerPacketSender;

import server.server_packet.*;

public class ServerConnectionInfo {

    private String id;
    
    private String ip;
    private int port;

    private Boolean hasSender = false;

    // This is when the expiration happens, which is twice the send time of each KeepAlive packet.
    private int keepAliveTimeoutSeconds = 60;

    private ServerPriority priority;

    private LocalDateTime lastActivity;
    private LocalDateTime initalConnectionTime;

    // Gives each node connection it's own sender that will be created whenever 
    private ServerPacketSender sender; 

    private static final Logger logger = LogManager.getLogger(ServerConnectionInfo.class);

    // Constructor
    public ServerConnectionInfo(String id, String ip, int port, ServerPriority priority) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.priority = priority;

        initalConnectionTime = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }

    /*
     *      Main Methods
     */
    
    public boolean isExpired() {
        return lastActivity.plusSeconds(keepAliveTimeoutSeconds)
            .isBefore(LocalDateTime.now());
    }

    public void createSender() {

        if(port == 0) {
            logger.error("Port is not set for: " + id +"!");
            return;
        }
        // Create sender for this node
        try {
            this.sender = new ServerPacketSender(ip, port);
            hasSender = true;
            logger.info("Sender Created for: " + id + " - at:  " + ip +":" + port);
        } catch (Exception e) {
            logger.error("Failed to create sender for: " + id + ":" + port + "\n" + e);
        }
    }

    public boolean send(ServerPacket packet) {
        if (sender != null) {
            boolean sent = sender.send(packet);
            if(!sent) {
                boolean retry = sender.retry(packet);
                if(!retry){
                    return false;
                }

            }
            return true;
        } 
        logger.error("Sender was not created for Node: {}! Cannot send packet", id);
            // TODO: Exception handling
        return false;
        
    }

    /*
     *      Getters
     */
    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public ServerPriority getPriority() {
        return priority;
    }

    public boolean getSenderStatus() {
        return hasSender;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeoutSeconds;
    }

    public LocalDateTime getInitialConnectionTime() {
        return initalConnectionTime;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    /*
     * 
     *      Setters
     * 
     */
    public void setId(String id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setKeepAliveTimeout(int keepAliveTimeoutSeconds) {
        this.keepAliveTimeoutSeconds = keepAliveTimeoutSeconds;
    }

    public void setPriority(ServerPriority priority) {
        this.priority = priority;
    }

    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    /*
     *      Stringify
     */

    public String asString() {
        String asString;

        asString= "ID - "+id+" | IP - "+ip+":"+port+"";

        return asString;
    }
}