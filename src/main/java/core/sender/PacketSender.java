/*
    Author: Nathaniel Brewer

    Extends the abstract sender - defining the retry method. 
*/
package core.sender;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.identity.RuntimeMembershipState;
import core.packet.AbstractPacket;

public class PacketSender extends AbstractSender{
    
    protected static final Logger logger = LogManager.getLogger(PacketSender.class);
    @Override
    protected Logger getLogger() { return logger; }

    private int maxRetries = 3;
    private int attempts = 0;
    private boolean ackRecieved;

    public PacketSender(String ip, int sendingPort) {
        this(ip, sendingPort, null);
    }

    public PacketSender(String ip, int sendingPort, RuntimeMembershipState membershipState) {
        this.ip = ip;
        this.sendingPort = sendingPort;
        this.membershipState = membershipState;
    }

    @Override
    public boolean retry(AbstractPacket packet) {
        ackRecieved = false;
        while (!ackRecieved){

            if (attempts == maxRetries) {
                logger.error("Attempt limit reached trying to recieve ACK!");
                attempts = 0;
                return ackRecieved;
            } else if (attempts < maxRetries && !ackRecieved) {
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                logger.warn("Failed to recieve ACK - retrying...");      
                ackRecieved = send(packet);
            }
            attempts++;
        }
        attempts = 0;
        return ackRecieved;
    }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

}
