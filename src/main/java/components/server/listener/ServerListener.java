/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the logic for listening for packets from the server, used by the Coordinator.
 *      This is a thread and will be created by the Main coordinator file once initialization
 *      is complete.
 * 
 */
package components.server.listener;

import java.io.IOException;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

import components.server.handler.ServerCoordinatorHandler;
import components.server.handler.ServerNodeHandler;

public class ServerListener implements Runnable {
    private final ExecutorService nodePool;
    private final ExecutorService coordinatorPool;

    private volatile boolean running = true;

    private static final Logger logger = LogManager.getLogger(ServerListener.class);
    
    private ServerSocket listenerSocket;    
    private int port;   
    private int timeout;

    private final AbstractTierIdentity identity;

    private final TierRole type;

    public ServerListener(int port, int timeout, TierRole type, AbstractTierIdentity identity) throws IOException {
        this.port = port;
        this.timeout = timeout;
        this.listenerSocket = new ServerSocket(port);
        this.listenerSocket.setSoTimeout(timeout);
        this.type = type;
        this.identity = identity;

        int cpus = Runtime.getRuntime().availableProcessors();
        // NOTE: These three vars are subject to change based on hardware and how long these threads are ran.
        int nodePoolSize = Math.max(8, cpus * 2);
        int nodeMaxPoolSize = Math.max(16, cpus * 4);
        int nodeWorkQueueCap = 1000;

        this.nodePool = new ThreadPoolExecutor(
            nodePoolSize,
            nodeMaxPoolSize,
            timeout,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(nodeWorkQueueCap),
            namedFactory("node"),
            new ThreadPoolExecutor.AbortPolicy()
        );

        int coordinatorPoolSize = 1;
        int coordinatorMaxPoolSize = 1;

        this.coordinatorPool = new ThreadPoolExecutor(
            coordinatorPoolSize,
            coordinatorMaxPoolSize,
            0, 
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            namedFactory("coordinator"),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    @Override
    public void run(){  

        logger.info("Listening on port {}.", this.port);

        while (running) {
            Socket accepted = null;

            try{
                accepted = listenerSocket.accept();

                if (TierRole.NODE == type) {
                    nodePool.execute(new ServerNodeHandler(accepted, identity));
                    accepted = null;
                } else if(TierRole.COORDINATOR == type) {
                    coordinatorPool.execute(new ServerCoordinatorHandler(accepted, identity));
                    accepted = null;
                } else {
                    logger.error("Unknown connection with type: {}", type);
                }
            } catch (SocketTimeoutException ignored) {

            } catch (RejectedExecutionException ree) {
                logger.error("Handler pool rejected connection for type {}", type, ree);
            } catch (IOException ioe) {
                if (running) {
                    logger.error("IOException in lister loop", ioe);
                }
            } catch (Exception e) {
                logger.error("Unknown exception in listener loop", e);
            } finally {
                if (accepted != null) {
                    try{
                        accepted.close();
                    } catch (IOException closeEx) {
                        logger.warn("Failed to close unhandled socket.", closeEx);
                    }
                    
                }
            }   
        }
    }

    public void stop() {
        running = false;
        closeSocket();
        shutdownPool(nodePool, "node");
        shutdownPool(coordinatorPool, "coordinator");
    }


    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger n = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + "-" + n.getAndIncrement());
            t.setDaemon(false);
            return t;
        };
    }

    private static void shutdownPool(ExecutorService pool, String name) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
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
        timeout = newTimeout;
        try {
            listenerSocket.setSoTimeout(timeout);
        } catch (Exception e) {
            logger.error("Error setting new timeout on socket: ( " + port + " )\n" + e);
        }
    }

    public boolean closeSocket() {
        try{
            logger.info("Closing socket on port: + ( " + port + " )\n");
            listenerSocket.close();
        } catch(Exception e) {
            logger.error("Error Closing socket on port: ( " + port + " )\n" + e);
            return false;
        }
        return true;
    }

}
