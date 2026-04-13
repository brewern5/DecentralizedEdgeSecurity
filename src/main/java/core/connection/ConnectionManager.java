/*
 *      Author: Nathaniel Brewer
 * 
 *      This is where all the ConnectionInfo objects will be stored and where the storage wise checks will be had for
 *      expired keepAlive.
 * 
 *      This is desined as a Singleton Pattern, to prevent multiple instances of this connection map.
 *      
 */

package core.connection;

import java.util.Iterator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.exception.KeepAliveException;
import core.identity.RuntimeMembershipState;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.keep_alive.KeepAliveManager;

public abstract class ConnectionManager {

    protected final ConcurrentHashMap<String, ConnectionDto> activeConnections = new ConcurrentHashMap<>();
    protected RuntimeMembershipState membershipState;
    protected TierRole instantiatorRole; 

    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    /** 
     *  Subclass will NEED to implement a static getInstance() Method with sunchronized initialization
     * 
     * @param membershipState The DTO for the membership of the cluster 
     * @param instantiatorRole What role instantiated this manager (i.e. "Server", "Node", "Coordinator")
     */
    protected ConnectionManager(RuntimeMembershipState membershipState, TierRole instantiatorRole) {
        this.membershipState = membershipState;
        this.instantiatorRole = instantiatorRole;
    }

    /*
            Singleton Helper - Thread-safe instance management
     */

    /**
     * Helper method for subclasses to implement thread-safe singleton pattern.
     * Each subclass should use this pattern:
     * <pre>
     * private static volatile YourConnectionManager instance;
     * 
     * public static YourConnectionManager getInstance(String instanceId, String clusterId, String role) {
     *     return getOrCreateInstance(instance, YourConnectionManager.class, 
     *         () -> instance = new YourConnectionManager(instanceId, clusterId, role));
     * }
     * </pre>
     */
    protected static <T extends ConnectionManager> T getOrCreateInstance(
            T currentInstance, 
            Class<?> lockClass, 
            java.util.function.Supplier<T> creator) {
        
        if (currentInstance == null) {
            synchronized (lockClass) {
                if (currentInstance == null) {
                    return creator.get();
                }
            }
        }
        return currentInstance;
    }

    /*
            Abstract Methods
     */

    public abstract boolean sendKeepAlive() throws KeepAliveException;

    /*
            End abstraction
    */

    public void sendToConnection(String connectionId, AbstractPacket packet) {
        ConnectionDtoManager dtoManager = new ConnectionDtoManager(activeConnections.get(connectionId), membershipState);
        dtoManager.send(packet);
    }
    
    public void checkExpiredConnections() {
        Iterator<Map.Entry<String, ConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()) {
            Map.Entry<String, ConnectionDto> entry = iterator.next();
            if(new ConnectionDtoManager(entry.getValue()).isExpired()){

                KeepAliveManager manager = new KeepAliveManager(membershipState, entry.getValue().getId(), null);
                
                if(entry.getValue().getPriority() == Priority.CRITICAL){

                    AbstractPacket keepAliveProbe =  manager.createOutgoingPacket();

                    boolean sendSuccess = new ConnectionDtoManager(entry.getValue()).send( keepAliveProbe );
                    if(!sendSuccess) {
                        logger.warn("Connection: \"{}\" has been terminated due to failed retry!", entry.getValue().getId());
                        terminateConnection(entry.getValue().getId());
                    }
                }
                else {
                    logger.warn("Connection: \"{}\" has been terminated due to priority status!", entry.getValue().getId()); 
                    terminateConnection(entry.getValue().getId());
                }
            }
        }
    }

    /*
     *      Response to expiry
     */

    public void terminateConnection(String id) {
        ConnectionDto remove = activeConnections.get(id);
        logger.info("Terminated Connection: \n  ID:" + remove.getId() 
            + "\n  IP - " + remove.getIp() + ":" + remove.getPort()
        );
        activeConnections.remove(remove.getId());

    }


    public void addConnection(ConnectionDto... connection) {
        for(ConnectionDto connected : connection) {
            activeConnections.put(connected.getId(), connected);
        }
    }


    /*
     *      Getters
     */

    public TierRole getRole() { return instantiatorRole; }

    public ConnectionDto getConnectionInfoById(String id) { return activeConnections.get(id); }

    public ConcurrentHashMap<String, ConnectionDto> getActiveConnections() { return activeConnections; }

    public String[] getAllIds() { return activeConnections.keySet().toArray(new String[0]); }

    public int getActiveConnectionCount() {

        int connectionCount = 0;

        Iterator<Map.Entry<String, ConnectionDto>> iterator =
            activeConnections.entrySet().iterator();

        while(iterator.hasNext()){
            connectionCount++;
        }

        return connectionCount;
    }


    /*
        Mutators
    */ 

    public void setRole(TierRole instantiatorRole) { this.instantiatorRole = instantiatorRole; }
}
