/**
 * 
 *              Configuration for each of the nodes in the hierarchy. Will grab machine IP address and will try to generate avaiable ports.
 *              From ./config/nodeConfig.properties
 */
package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;            // Used for grabbing the machine's IP address
import java.net.UnknownHostException;   // Error for trying to grab IP address

import java.util.Properties;            // Utility for getting properties from any .properties file

public class NodeConfig {

    private static Properties properties = new Properties();      // Generate the properties object

    static {
        try {
            FileInputStream in = new FileInputStream("config/nodeConfig.properties");
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

    public String grabIP() throws UnknownHostException {

        String ipAddress = null;
        
        InetAddress localHost = InetAddress.getLocalHost();     // Grabs the IP and will convert it to a Java object
        ipAddress = localHost.getHostAddress();                 // Generates the IP as a string to pass it back to the edge node using it

        writeToConfig("edgeServer.IP", ipAddress);          // Since the IP will be machine dependant at the moment, just grab the machine IP

        return ipAddress;       // Will return a null value if no IP is found 
    }

    public String getIPByKey(String key) {

        String IP = "";
        try{
            IP = properties.getProperty(key);
        } catch (Error e) {
            System.err.println("Error getting " + key + "'s IP from config file!\n");
            e.printStackTrace();
            // TODO: Write to logger the error
        }
        return IP;
    }
    

    /**
     *  Will try and grab available port for the machine.    
     *  @param key - Which node is trying to grab the port
     *  @return int, if port is found. If not found 0
     */

    public int getPort(String key){

        int port = 0;
        try{
            port = Integer.parseInt(properties.getProperty(key));

        } catch (Error e){
            System.err.println("Error getting port from config file!\n");
            e.printStackTrace();
        }
        return port;
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
                try(OutputStream outputStream = new FileOutputStream("config/nodeConfig.properties")){
                    properties.store(outputStream, null);
                } catch(IOException ioe) {
                    System.err.println("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n");
                    ioe.printStackTrace();
                    // TODO: Write to logger the error
                }

                System.out.println("\nOverwrote:\n\t Key: ( " + key + " )\n\t Value: ( " + value + " )");
                // TODO: Write to logger a warning
            } 
            else if(properties.getProperty(key) == null){

                properties.setProperty(key, value);

                // Write the new key/value back to the file
                try(OutputStream outputStream = new FileOutputStream("config/nodeConfig.properties")){
                    properties.store(outputStream, null);
                
                } catch(IOException ioe) {
                    System.err.println("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n");
                    ioe.printStackTrace();
                    // TODO: Write to logger the error
                }
            }
            else {
                // Error handling the key/value and or the file itself.
                throw new Exception("Unknown Error handling Key/Value for the config file!\n");
                // TODO: Write to logger the error
            }

        }catch (Exception e) { // Generic Exception
            System.err.println("Error finding key: ( " + key + " ) from config file!\n");
            e.printStackTrace();
            // TODO: Write to logger the error
        }

    }
}
