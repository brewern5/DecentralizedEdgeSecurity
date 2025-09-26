/**
 * 
 *              Configuration for each of the nodes in the hierarchy. Will grab machine IP address and will try to generate avaiable ports.
 *              From ./server_config/serverConfig.properties
 */
package server.server_config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Inet4Address;
import java.net.InetAddress;            // Used for grabbing the machine's IP address
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;   // Error for trying to grab IP address

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;            // Utility for getting properties from any .properties file

public class ServerConfig {

    private static final Logger logger = LogManager.getLogger(ServerConfig.class);

    private static Properties properties = new Properties();      // Generate the properties object

    static {
        try {
            FileInputStream in = new FileInputStream("config/server_config/serverConfig.properties");
            properties.load(in);
            in.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     *   Grabs the machines IP and will return the IP.
     *   @return a string of the IP if found, if not found then a Null value
     */

    public String grabIP() throws UnknownHostException, SocketException {

        String realIp = null;

        for(NetworkInterface ni: java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if(ni.isLoopback() || !ni.isUp()) continue;
            for(InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                if(addr instanceof Inet4Address) {
                    realIp = addr.getHostAddress();
                    break;
                }
            }
            if(realIp != null) break;
        }

        return realIp; 
    }
    

    /**
     *  Will try and grab available port for the machine.    
     *  @param key - Which node is trying to grab the port
     *  @return int, if port is found. If not found 0
     */

    public int getPortByKey(String key) {

        int port = 0;
        try{
            port = Integer.parseInt(properties.getProperty(key));

        } catch (Error e){
            
        }
        return port;
    }

    public String getIPByKey(String key) {

        String IP = "";
        try{
            IP = properties.getProperty(key);
        } catch (Error e) {
            logger.error("Error getting " + key + "'s IP from config file!\n" + e);
        }
        return IP;
    }


    /**
     *  Will try to write/overwrite a key/value pair in config.properties    
     *  @param key - will look for this key in the config, if not there it will write it there
     *  @param value - the value to the key that will be inserted once the key is either found or written
     */

    public void writeToConfig(String key, String value) {

        // Trying to check to see if the config file has the node key, whatever that may be
        try{

            // If the key is already in the file, this will add the value
            if(properties.getProperty(key) != null){

                properties.setProperty(key, value);

                // Write the new key/value back to the file
                try(OutputStream outputStream = new FileOutputStream("config/server_config/serverConfig.properties")){
                    properties.store(outputStream, null);
                } catch(IOException ioe) {
                    logger.error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + ioe);
                } catch(Exception e) {
                    logger.error("Unknown error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + e);
                }

                logger.info("Overwrote:\t Key: ( " + key + " )\t Value: ( " + value + " )");
            } 
            else if(properties.getProperty(key) == null){

                properties.setProperty(key, value);

                // Write the new key/value back to the file
                try(OutputStream outputStream = new FileOutputStream("config/server_config/serverConfig.properties")){
                    properties.store(outputStream, null);
                } catch(IOException ioe) {
                    logger.error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + ioe);
                } catch(Exception e) {
                    logger.error("Unknown Error writing value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + e);
                }
            }
            else {
                // Error handling the key/value and or the file itself.
                throw new Exception("Unknown Error handling Key/Value for the config file!\n");
            }

        }catch (Exception e) { // Generic Exception
            logger.error("Error finding key: ( " + key + " ) from config file!\n" + e);
        }

    }

}