/*
    Author: Nathaniel Brewer

    This is the concrete implementation of the EdgeComponent. 
*/
package components.server.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import components.server.identity.ServerIdentity;
import components.server.listener.ServerListener;

import components.server.config.ServerConfig;

import components.server.connections.ServerNodeConnectionManager;
import components.server.connections.ServerCoordinatorConnectionManager;

import core.config.AbstractConfig;

import core.connection.Priority;
import core.connection.ConnectionDto;
import core.connection.ConnectionManager;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;
import core.packet.AbstractPacket;
import core.packet.initalization.InitalizationPacketManager;
import core.runtime.AbstractEdgeComponent;

public class ServerComponent extends AbstractEdgeComponent {
    
    private static ServerListener nodeListener;
    private static ServerListener coordinatorListener;

    private static final Logger logger = LogManager.getLogger(ServerComponent.class);
    @Override
    protected Logger getLogger() { return logger; }

    @Override
    protected void validateStartupArgs(String[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.error("Server requires instanceId argument!");
            throw new IllegalArgumentException("Server requires instanceId argument");
        }
    }

    @Override
    protected AbstractTierIdentity buildIdentity(String[] args) {
        return new ServerIdentity(TierRole.SERVER, TierRole.COORDINATOR, args[0]);
    }

    @Override 
    protected AbstractConfig loadConfig(AbstractTierIdentity identity) {
        return new ServerConfig(identity);
    }

    @Override
    protected void initializeConnections() {

        try {
            connectionManagers.put(
                identity.getHigherTier(), 
                ServerCoordinatorConnectionManager.getInstance("", "", identity.getRole())
            );

            ConnectionManager serverConnection = connectionManagers.get(identity.getHigherTier());

            serverConnection.addConnection(new ConnectionDto(
                "1",
                config.getIPByKey("Coordinator.IP"),
                config.getPortByKey("Coordinator.listeningPort"),
                Priority.CRITICAL
            ));
            
            LinkedHashMap<String, String> payload = new LinkedHashMap<>();
            payload.put(
                "Server.listeningPort",
                String.valueOf(config.getPortByKey("Server.listeningPort")) 
            );

            AbstractPacket initPacket = new InitalizationPacketManager(
                "",
                "",
                "1",
                identity.getRole(),
                serverConnection,
                config.getIPByKey("Server.IP")
            )
            .createOutgoingPacket();

            initPacket.addPayload(payload);

            serverConnection.sendToConnection("1", initPacket);

            membershipState.assignId(serverConnection.getInstanceId());
            membershipState.assignClusterId(serverConnection.getClusterId());

        } catch(Exception e) {
            logger.error("Error Sending Initalization Packet: " + e);
        }

    }

    @Override
    protected void startListeners() {

        int timeoutMs = 2000; 

        try {
            coordinatorListener = new ServerListener(
                config.getPortByKey("Node.listeningPort"), 
                timeoutMs,
                identity
            );

            listenerExecutor.execute(serverListener);
            logger.info("Node listener submitted on port {}", serverListener.getActivePort());
        } catch (Exception e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Node.listeningPort")
                + e
            );
            // TODO: try to grab new port if this one is unavailable
        }
    }

    @Override
    protected void scheduleTimers() {
        try{
            if (scheduler == null) {
                throw new IllegalStateException("Scheduler is not initialized before scheduleTimers()");
            }

            scheduler.scheduleAtFixedRate(() ->{
                try {
                    boolean keepAliveSent;

                    keepAliveSent = connectionManagers.get(identity.getHigherTier()).sendKeepAlive();

                    if(!keepAliveSent){
                        logger.error("Keep alive was not sent!");
                    }

                } catch(core.exception.KeepAliveException e) {
                    // Detailed logging for keep-alive specific failures
                    logger.error("Keep-alive failed at stage: {} for connection: {} - {}", 
                               e.getStage(), e.getConnectionId(), e.getMessage());
                
                    // Different handling based on failure type
                    if (e.isSendFailure()) {
                        logger.error("Failed to send keep-alive packet - network or socket issue");
                    } else if (e.isAckFailure()) {
                        if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_RECEIVED) {
                            logger.error("Keep-alive sent but ACK not received - {} may be down", identity.getHigherTier());
                        } else if (e.getStage() == core.exception.KeepAliveException.FailureStage.ACK_NOT_HANDLED) {
                            logger.error("ACK received but not handled properly - handler issue");
                        }
                    }
                }
            }, 5, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unhandled Exception when creating Scheduler!", e);
        }
        
    }

    @Override
    protected void beforeStop() {
        if (serverListener != null) {
            serverListener.closeSocket();
        }
    }

    @Override
    protected void afterStart() {
        // TODO: PeerList request
    }

}
