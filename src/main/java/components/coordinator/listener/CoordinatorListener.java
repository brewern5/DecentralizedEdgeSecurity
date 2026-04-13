/*
 *      Author: Nathaniel Brewer
 * 
 *      This is a listener that will be instantiated to await packets coming from an Edge Server.
 *      This will be a thread so the main program (in this instance the Coordinator) can still 
 *      send packets. (Such as heartbeat packets) to it's children(Server).
 * 
 *      Once a packet is recieved, the listener will create a new thread to handle the packet,
 *      this is so that thread can process and ID the packet while the listener can return to
 *      listening for other new packets.
 */

package components.coordinator.listener;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import components.coordinator.handler.CoordinatorServerHandler;
import core.identity.AbstractTierIdentity;

public class CoordinatorListener implements Runnable {

    private static final Logger logger = LogManager.getLogger(CoordinatorListener.class);

    private Socket connected;
    private ServerSocket listenerSocket;   
    private int timeout;
    private int port;

    private final AbstractTierIdentity identity;

    public CoordinatorListener(int port, int timeout, AbstractTierIdentity identity) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.identity = identity;

        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
    }

    @Override
    public void run(){  

        logger.info("Listening on port {}", port);

        while(!Thread.currentThread().isInterrupted()){

            if (listenerSocket.isClosed()) {
                logger.info("Listener socket closed on port {}, exiting listener loop", port);
                break;
            }

            try {
                connected = listenerSocket.accept();       
                Thread handlerThread = new Thread(new CoordinatorServerHandler(connected, identity));
                handlerThread.start();
            } catch (SocketTimeoutException sto) {  
                // Timeout is expected;
            } catch (IOException ioe) {     
                if (listenerSocket.isClosed()) {
                    logger.info("Listener socket closed on port {}, stopping listener", port);
                    break;
                }
            }
        }
    }

    /*          Accessor methods        */
    public int getActivePort() {
        return port;
    }

    public int getActiveTimeout() {
        return timeout;
    }

    /*          Changer methods        */

    public void changePort(int newPort) {
        port = newPort;
        // TODO: Activly change the port in the socket
    }

    public void changeTimeout(int newTimeout) {
        int oldTimeout = timeout;
        timeout = newTimeout;   
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            timeout = oldTimeout;
            logger.error("Error setting new timeout on socket: ( {} )\n", port, e);
        }
    }

    public boolean closeSocket() {
        try{
            logger.info("Closing socket on port: ( {} ) ", port);
            listenerSocket.close();   
        } catch(Exception e) {
            logger.error("Error Closing socket on port: ( {} )\n", port, e);
            return false;
        }
        return true;
    }
}
