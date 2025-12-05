package sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import packet.AbstractPacket;

public class PacketSender extends AbstractSender{
    
    protected static final Logger logger = LogManager.getLogger(PacketSender.class);

    protected int maxRetries = 3;
    protected int attempts = 0;
    protected boolean ackRecieved;

    public PacketSender(String ip, int sendingPort) {
        this.ip = ip;
        this.sendingPort = sendingPort;
    }

    @Override
    public boolean retry(AbstractPacket packet) {
        ackRecieved = false;
        while (!ackRecieved){
            // If the attempt limit is reached the server will shutdown
            if (attempts == maxRetries) {
                logger.error("Attempt limit reached trying to recieve ACK!");
                attempts = 0;
                return ackRecieved;
            }
            // Retry the connection - must reopen the socket to create a new connection
            else if (attempts < maxRetries && !ackRecieved) {
                // Wait to retry and increment attempts after 1 second
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                logger.warn("Failed to recieve ACK - retrying...");      
                ackRecieved = send(packet);
            }
            // Inc attemps
            attempts++;
        }
        // reset attempts for reuses (if applicable)
        attempts = 0;
        return ackRecieved;
    }
}
