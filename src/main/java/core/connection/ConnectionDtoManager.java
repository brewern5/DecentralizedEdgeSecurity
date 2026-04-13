/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the manager for the Connection info DTO
 * 
 *      All will have expiry times in which either a connection will dropped or a KeepAlive will be sent from the manager to
 *      the connection. This will be dependent on the status of the connection. For example, one with a CRITICAL status will 
 *      try to be kept alive
 */

package core.connection;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;

import core.sender.AbstractSender;
import core.sender.PacketSender;

public class ConnectionDtoManager {
    
    private static final Logger logger = LogManager.getLogger(ConnectionDtoManager.class);
    
    private ConnectionDto connectionInfo;
    private final RuntimeMembershipState membershipState;

    private AbstractSender sender;
    private Boolean hasSender = false;

    public ConnectionDtoManager(ConnectionDto connectionInfo) {
        this(connectionInfo, null);
    }

    public ConnectionDtoManager(ConnectionDto connectionInfo, RuntimeMembershipState membershipState) {
        this.connectionInfo = connectionInfo;
        this.membershipState = membershipState;
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
        
        try {
            this.sender = new PacketSender(connectionInfo.getIp(), connectionInfo.getPort(), membershipState);
            hasSender = true;
            logger.info("Sender Created for connection: " + connectionInfo.getId() + " - " + connectionInfo.getIp() +":" + connectionInfo.getPort());
        } catch (Exception e) {
            logger.error("Failed to create sender for connection " + connectionInfo.getId() + ":" + connectionInfo.getPort() + "\n" + e);
        }
    }

    public boolean send(AbstractPacket packet) {
        if (sender == null || !hasSender) {
            
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
                return true;
            }
        } else {
            logger.debug("Successfully sent {} packet to connection: {} and received ACK", 
                        packet.getPacketType(), connectionInfo.getId());
            return true;
        }
    }

}
