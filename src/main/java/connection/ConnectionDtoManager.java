/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the manager for the Connection info DTO
 * 
 *      All will have expiry times in which either a connection will dropped or a KeepAlive will be sent from the manager to
 *      the connection. This will be dependent on the status of the connection. For example, one with a CRITICAL status will 
 *      try to be kept alive
 */

package connection;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sender.AbstractSender;
import sender.PacketSender;

import packet.AbstractPacket;
import packet.PacketType;

public class ConnectionDtoManager {
    
    private static final Logger logger = LogManager.getLogger(ConnectionDtoManager.class);
    
    private ConnectionDto connectionInfo;

    private String assignedId;

    private AbstractSender sender;
    private Boolean hasSender = false;

    public ConnectionDtoManager(ConnectionDto connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    /*
     *      Main Methods
     */
    
    public boolean isExpired() {
        return connectionInfo.getLastActivity().plusSeconds(connectionInfo.getKeepAliveTimeout())
            .isBefore(LocalDateTime.now());
    }

    public void createSender() {

        if(connectionInfo.getPort() == 0) {
            logger.error("Port is not set for connection " + connectionInfo.getId() +"!");
            return;
        }
        // Create sender for this node
        try {
            this.sender = new PacketSender(connectionInfo.getIp(), connectionInfo.getPort());
            hasSender = true;
            logger.info("Sender Created for connection: " + connectionInfo.getId() + " - " + connectionInfo.getIp() +":" + connectionInfo.getPort());
        } catch (Exception e) {
            logger.error("Failed to create sender for connection " + connectionInfo.getId() + ":" + connectionInfo.getPort() + "\n" + e);
        }
    }

    public boolean send(AbstractPacket packet) {
        if (sender == null || !hasSender) {
            // if no sender, create one
            createSender();
            if (sender == null || !hasSender) {
                logger.error("Sender was not created for Connection: {}! Cannot send packet", connectionInfo.getId());
                return false;
            }
        }
        
        logger.debug("Attempting to send {} packet to connection: {}", packet.getPacketType(), connectionInfo.getId());
        boolean sent = sender.send(packet);
        
        if(!sent) {
            logger.warn("Initial send failed for connection: {}, attempting retry...", connectionInfo.getId());
            boolean retry = sender.retry(packet);
            if(!retry){
                logger.error("Retry failed for connection: {} - packet was NOT sent or ACK was NOT received", 
                           connectionInfo.getId());
                return false;
            } else {
                logger.info("Retry succeeded for connection: {} - packet sent and ACK received", connectionInfo.getId());
                if(packet.getPacketType() == PacketType.INITIALIZATION) {
                    assignedId = sender.getAssignedId();
                }
                return true;
            }
        } else {
            logger.debug("Successfully sent {} packet to connection: {} and received ACK", 
                        packet.getPacketType(), connectionInfo.getId());
            if(packet.getPacketType() == PacketType.INITIALIZATION) {
                assignedId = sender.getAssignedId();
            }
            return true;
        }
    }

    public String getAssignedId() { return assignedId; }

}
