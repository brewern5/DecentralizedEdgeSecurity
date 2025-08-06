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
package coordinator.coordinator_connections;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import coordinator.coordinator_sender.CoordinatorPacketSender;

import coordinator.coordinator_packet.*;

public class CoordinatorConnectionInfo {

    private String id;
    
    private String ip;
    private int port;

    // This is when the expiration happens, which is twice the send time of each KeepAlive packet.
    private int keepAliveTimeoutSeconds = 60;

    private CoordinatorPriority priority;

    private LocalDateTime lastActivity;
    private LocalDateTime initalConnectionTime;

        // Gives each node connection it's own sender that will be created whenever 
    private CoordinatorPacketSender sender; 

    private static final Logger logger = LogManager.getLogger(CoordinatorConnectionInfo.class);

    // Constructor
    public CoordinatorConnectionInfo(String id, String ip, int port, CoordinatorPriority priority) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.priority = priority;

        initalConnectionTime = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
    }

    /*
     * 
     *      Main Methods
     * 
     */
    
    public boolean isExpired() {
        return lastActivity.plusSeconds(keepAliveTimeoutSeconds)
            .isBefore(LocalDateTime.now());
    }

        public void createSender() {

        if(port == 0) {
            logger.error("Port is not set for Server " + id +"!");
            return;
        }
        // Create sender for this node
        try {
            this.sender = new CoordinatorPacketSender(ip, port);
            logger.info("Sender Created for Server: " + id + " - " + ip +":" + port);
        } catch (Exception e) {
            logger.error("Failed to create sender for Server " + id + ":" + port + "\n" + e);
        }
    }

        public void sendToNode(CoordinatorPacket packet) {
        if (sender != null) {
            sender.send(packet);
        }
    }

    /*
     * 
     *      Getters
     * 
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

    public int getKeepAliveTimeout() {
        return keepAliveTimeoutSeconds;
    }

    public CoordinatorPriority getServerPriortiy() {
        return priority;
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

    public void setPriority(CoordinatorPriority priority) {
        this.priority = priority;
    }

    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

}