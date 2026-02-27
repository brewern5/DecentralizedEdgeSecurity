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

import core.packet.AbstractPacket;
import core.packet.PacketType;
import core.transport.TransportClient;
import core.transport.TransportClientFactory;

public class ConnectionDtoManager {
    
    private static final Logger logger = LogManager.getLogger(ConnectionDtoManager.class);
    
    private ConnectionDto connectionInfo;

    private String assignedId;

    private TransportClient transportClient;
    private Boolean hasTransportClient = false;

    private final TransportClientFactory transportClientFactory = new TransportClientFactory();

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

    public void createTransportClient() {

        if(connectionInfo.getPort() == 0) {
            logger.error("Port is not set for connection " + connectionInfo.getId() +"!");
            return;
        }
        // Create transport client for this connection
        try {
            this.transportClient = transportClientFactory.create(connectionInfo);
            hasTransportClient = true;
            logger.info("Transport client created for connection: " + connectionInfo.getId() + " - " + connectionInfo.getIp() +":" + connectionInfo.getPort());
        } catch (Exception e) {
            logger.error("Failed to create transport client for connection " + connectionInfo.getId() + ":" + connectionInfo.getPort() + "\n" + e);
        }
    }

    public boolean send(AbstractPacket packet) {
        if (transportClient == null || !hasTransportClient) {
            // if no transport client, create one
            createTransportClient();
            if (transportClient == null || !hasTransportClient) {
                logger.error("Transport client was not created for Connection: {}! Cannot send packet", connectionInfo.getId());
                return false;
            }
        }
        
        logger.debug("Attempting to send {} packet to connection: {}", packet.getPacketType(), connectionInfo.getId());
        boolean sent = transportClient.send(packet);
        
        if(!sent) {
            logger.warn("Initial send failed for connection: {}, attempting retry...", connectionInfo.getId());
            boolean retry = transportClient.retry(packet);
            if(!retry){
                logger.error("Retry failed for connection: {} - packet was NOT sent or ACK was NOT received", 
                           connectionInfo.getId());
                return false;
            } else {
                logger.info("Retry succeeded for connection: {} - packet sent and ACK received", connectionInfo.getId());
                if(packet.getPacketType() == PacketType.INITIALIZATION) {
                    assignedId = transportClient.getAssignedId();
                }
                return true;
            }
        } else {
            logger.debug("Successfully sent {} packet to connection: {} and received ACK", 
                        packet.getPacketType(), connectionInfo.getId());
            if(packet.getPacketType() == PacketType.INITIALIZATION) {
                assignedId = transportClient.getAssignedId();
            }
            return true;
        }
    }

    public String getAssignedId() { return assignedId; }

}
