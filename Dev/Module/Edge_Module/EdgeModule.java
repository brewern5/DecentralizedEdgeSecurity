// 

package Edge_Module;

import java.io.*;
import java.net.*;
import java.util.*;

import config.Config;       // The configuration file for the entire network

public class EdgeModule {

    private static String IP;
    private static int port;

    private static ServerSocket listening;      // The socket that will be listening to requests from the Edge server.
    private static Socket sending;              // The socket that will send messages to edge server

    private static Socket clientSocket;

    /*
     *  Initalizes the Node Module - This will (probably) be the first thing that runs when the Node Module is started up
     * 
     */

    public static void init() {

        Config config = new Config();

        // try/catch to generate the IP from ./Config.java - Throws UnknownHostException if it cannot determine the IP
        try{
            IP = config.grabIP();
        } catch (UnknownHostException e) {
            System.err.println("Error: Unable to determine local host IP address.");
            e.printStackTrace();
        }

        port = config.generatePort("edgeModule.port");

        // Try to create a serverSocket to listen to requests 
        try {
            listening = new ServerSocket(port);
            System.out.println("Listening on port " + port);
        } catch (Exception e) {
            System.err.println("Error creating Listening Socket on port " + port);
            e.printStackTrace();
            // TODO: try to grab new port if this one is unavailable
        }
        // The sending port. Will come back to this
        /* 
        try (Socket socket = new Socket(IP, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String message = "Hello from Edge Module!";
            output.println(message);
            System.out.println("Edge Module sent: " + message);

            // If server replies
            // String response = input.readLine();
            // System.out.println("Edge Module received: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
    public static void main(String[] args) {

        init();         // Begins the initalization process 

        while(true){

            try{
            clientSocket = listening.accept();
            } catch (IOException e){
                e.printStackTrace();
            }
            
            System.out.println("Client connected: " + clientSocket.getInetAddress());

        }
        
    }
}