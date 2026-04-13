/*
 *      Author: Nathaniel Brewer
 * 
 *      TODO: Add description
 */
package components.node.listener;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.identity.AbstractTierIdentity;

import components.node.handler.NodeServerHandler;

public class NodeListener implements Runnable {

    private static final Logger logger = LogManager.getLogger(NodeListener.class);
    
    private Socket connected;
    private ServerSocket listenerSocket;
    private int port;
    private int timeout;

    private final AbstractTierIdentity identity;

    public NodeListener(int port, int timeout, AbstractTierIdentity identity) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
        this.identity = identity;
    }

    @Override
    public void run(){  

        logger.info("Listening on port {}", port);

        while(!Thread.currentThread().isInterrupted()) {
            if (listenerSocket.isClosed()) {
                logger.info("Listener socket closed on port {}, exiting listener loop", port);
                break;
            }

            try {
                connected = listenerSocket.accept();
                Thread handlerThread = new Thread(new NodeServerHandler(connected, identity));
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

    /*          Changer Methods          */

    public void changePort(int newPort) {
        port = newPort;
        // TODO: Activly change the port in the socket
    }

    public void changeTimeout(int newTimeout) {
        timeout = newTimeout;
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            logger.error("Error setting new timeout on socket: ( {} )\n ", port, e);
        }
    }

    public boolean closeSocket() {
        try{
            listenerSocket.close();
            logger.warn("Listening Socket Closed on port {}!", port);
        } catch(Exception e) {
            logger.error("Error Closing socket on port {}\n", port,  e);
            return false;
        }
        return true;
    }
}