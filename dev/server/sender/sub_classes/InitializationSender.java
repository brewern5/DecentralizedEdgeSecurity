package sender.sub_classes;
import sender.PacketSender;
import packet.*;

import java.net.*;


public class InitializationSender extends PacketSender{
    
     /* 
        Create the inital connection with the parent.
        In this it will try and create an Initalization packet, which will send it's preffered receiving port
        to the Coordinator in order to store that into it's config file
    */

    private ServerPacket packet = new ServerPacket(
        ServerPacketType.INITIALIZATION,       // Packet type
        "EdgeServer",                   // Sender
        "server.listeningPort:5003"    // Payload
    );


    public InitializationSender(){} // No args-constructor

    @Override
    public ServerPacket getSendPacket(){
        return this.packet;
    }

    @Override
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
            }
            // Inc attemps
            attempts++;
        }
    }
}
