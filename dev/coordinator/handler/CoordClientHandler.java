package handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import config.CoordConfig;       // The configuration file for the entire network

public class CoordClientHandler implements Runnable {
    
    private Socket clientSocket;

    private CoordConfig config = new CoordConfig();

    public CoordClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run(){

        System.out.println("Client connected: " + clientSocket.getInetAddress().toString() + "\n\tFrom port: " + clientSocket.getLocalPort());

        String clientIP = clientSocket.getInetAddress().toString();

        clientIP = clientIP.substring(1); // Removes the forward slash

        config.writeToConfig("edgeServer.IP", clientIP);
        config.writeToConfig("edgeServer.port", Integer.toString(clientSocket.getLocalPort()));

        // Handle client events
        try{
                // Thread for client 
                // Send ack back on that same connection line
                // THREADS!
                // Define packet structure
                // Create seperate class for the packet
                    // Instantiate a new packet class and will populate that instance
                    // Create a packet DTO
                    // DTO (Look into this)
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;

                while((line = reader.readLine()) != null) {
                    System.out.println("Client: \n\t");
                    System.out.println(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            
    }
}
