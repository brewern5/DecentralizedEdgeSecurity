/*
 *      Author: Nathaniel Brewer
 * 
 *      This is the top of the hierarchy in our edge network, this is the connection point between the
 *      hierarchy and the cloud. Since this is the top of the network, it's instantiation is the highest
 *      priority, and will be started first.
 * 
 *      It's only direct child is the Server. This coordinator may potentially have multiple so that will
 *      need to be considered
 * 
 *      When started the Coordinator will connected to 'coordinatorConfig.properties' through the 
 *      coordinatorConfig.java file as the 'config' object. This will allow for the getting of the IP address
 *      and the Port(s).
 * 
 *      After these items are grabbed, a listener for the server will be instantiated to be used later. This
 *      will finish the initalization process. Once this is done, the listener will be thrown into the thread
 *      to be constantly ran seperate of this project
 */
package components.coordinator;

import java.io.IOException;

import java.net.SocketException;
import java.net.UnknownHostException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import components.coordinator.connections.CoordinatorConnectionManager;
import components.coordinator.identity.CoordinatorIdentity;
import components.coordinator.listener.CoordinatorListener;
import components.coordinator.config.CoordinatorConfig;

import core.config.AbstractConfig;
import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

public class EdgeCoordinator {

    private static final Logger logger = LogManager.getLogger(EdgeCoordinator.class);

    private static volatile String coordinatorId = null;

    private static CoordinatorConnectionManager serverConnectionManager;

    private static String IP;

    private static CoordinatorListener serverListener;

    private static ScheduledExecutorService timerScheduler;

    private static AbstractConfig config;
    private static AbstractTierIdentity instanceDTO;

    public static void init() {

        setCoordinatorId(UUID.randomUUID().toString());

        serverConnectionManager = CoordinatorConnectionManager.getInstance(coordinatorId, null, "Coordinator");

        try{
            logger.info("\t\tEDGE COORDINATOR");
            IP = config.grabIP(); 
        } catch (UnknownHostException e) {
            logger.error("Error: Unable to determine local host IP address.\n" + e);
        } catch (SocketException e) {
            logger.error("Error: Unable to determine IP Address");
        }

        try {
            serverListener = new CoordinatorListener(
                config.getPortByKey("Coordinator.listeningPort"),
                 5000
            );  

        } catch (IOException e) {
            logger.error(
                "Error creating Listening Socket on port " 
                + config.getPortByKey("Coordinator.listeningPort")
                + "\n" + e
            );
            // TODO: try to grab new port if this one is unavailable
        } catch (Exception e) {
            logger.error("Unknown Error creating listener ports!\n" + e);
        }
        /*
         *      Try and Create timers
         */
        try {
            initializeTimers();
        } catch (Exception e) {
            logger.error("Error creating Timers: \n" + e);
        }
    }

    /*
     *      ID assignment 
     */

    public static synchronized void setCoordinatorId(String id) {
        coordinatorId = id;
        logger.info("Coordinator ID assigned: {}", id);
    }

    public static synchronized String getCoordinatorId() {
        return coordinatorId;
    }

    /*
     *      Timer creation
     */
    private static void initializeTimers() {
        timerScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "EdgeCoordinator-Timer");
            //t.setDaemon(true);
            return t;
        });

        timerScheduler.scheduleAtFixedRate(() -> {
            try{
                serverConnectionManager.checkExpiredConnections();
            } catch (Exception e){
                logger.error("Exception in expiry timer: ", e);
            }
        }, 20, 20, TimeUnit.SECONDS);
        logger.info("Timer for checking Expired connections created!");
    }
    /*
     *          MAIN
     */
    public static void main(String[] args) {

        String instanceId = args.length > 0 ? args[0] : null;

        if(instanceId != "DEFAULT_"+TierRole.COORDINATOR) {
            logger.info("Starting {} Instance with ID: {}", TierRole.COORDINATOR, instanceId);
        } else {
            logger.warn("Starting with default tier config! ---- DEFAULT_{}",TierRole.COORDINATOR);
            logger.warn("Only one instance of Tier.{} can be made with default config!", TierRole.COORDINATOR);
        }

        instanceDTO = new CoordinatorIdentity(TierRole.COORDINATOR, TierRole.NETWORK, instanceId);

        
        try{
            config = new CoordinatorConfig(instanceDTO);
        } catch(Exception e) {
            logger.error("Could not open the {} config!" + e.getMessage(), TierRole.COORDINATOR);
        }

        init();

        Thread listeningThread = new Thread(serverListener);

        listeningThread.start();

        /* 
        // TODO: DEMO
        Scanner in = new Scanner(System.in);
        boolean on = true;
        while(on){
            System.out.println("Manually send message to server: ");
            String message = in.nextLine();

            if(!message.isEmpty()) {
                CoordinatorPacket messagePacket = new CoordinatorGenericPacket(
                    CoordinatorPacketType.MESSAGE,
                    getCoordinatorId(),
                    message
                );

                String[] serverIdArray = serverConnectionManager.getAllIds();

                HashMap<Integer, String> servers = new HashMap<>();

                boolean hasNum = false;

                int trys = 0;

                while(!hasNum) {
                    servers.clear();
                    System.out.println("Chose the Server to send to based on the Number next to it!");
                    for(int i = 1; i < serverIdArray.length + 1; i++) {
                        System.out.println(i + " : " + serverIdArray[i-1]);
                        servers.put(i, serverIdArray[i-1]);
                    }
                    int serverNum = in.nextInt();
                    if(!servers.get(serverNum).isEmpty()) {
                        new CoordinatorConnectionDtoManager(
                            serverConnectionManager.getConnectionInfoById(servers.get(serverNum)))
                            .send(messagePacket);
                        hasNum = true;
                    }
                    else if(trys > 2){
                        System.out.println("Try again next time loser!");
                        hasNum = true;
                    }
                    trys++;
                }
            }
        }
        in.close();
    */
   } 
}