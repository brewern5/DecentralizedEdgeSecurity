package sender;
import packet.*;

import java.net.*;

public class PacketSender extends Sender{

    protected int maxRetries = 3;
    protected int attempts = 0;
    protected boolean ackRecieved = false;
    
    public PacketSender() {} // No-args constructor
    
    /*
     *      
     *          Sudo-Abstract methods
     * 
     */

    // do nothing here as the children will override this method
    @Override
    public ServerPacket getSendPacket(){
        return null;
    }


    /*  
     *  The default method for retrying packet sending, if a failure occurs then retry will be invoked. 
     *  This can and more than likely will be overwritten with certain PacketTypes that may not want to retry.
     * 
    */
    public void retry() {
         while (!ackRecieved){
            // If the attempt limit is reached the server will shutdown
            if (attempts == maxRetries) {
                System.err.println("\n\nAttempt limit reached trying to recieve ACK");
                return;
            }
            // Retry the connection - must reopen the socket to create a new connection
            else if (attempts < maxRetries && !ackRecieved) {
                System.err.println("\nFailed to recieve ACK - retrying...\n\n");
                // Wait to retry and increment attempts after 1 second
                scheduler.schedule(() -> {
                    System.out.println("\nFailed to recieve ACK - retrying...\n\n");
                    }, 1, java.util.concurrent.TimeUnit.SECONDS
                );
                //  Since 'GetSendPacket' is an abstract class - this method know it needs it,
                //  but it's children will define it based on their needs.       
                send();
            }
            // Inc attemps
            attempts++;
        }
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }
    
}
