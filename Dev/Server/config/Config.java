/**
 * 
 *              Configuration for each of the nodes in the hierarchy. Will grab machine IP address and will try to generate avaiable ports.
 *              From ./config/config.properties
 */
package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;            // Used for grabbing the machine's IP address
import java.net.UnknownHostException;   // Error for trying to grab IP address

import java.util.Properties;            // Utility for getting properties from any .properties file

public class Config {

    private static Properties properties = new Properties();      // Generate the properties object

    static {
        try {
            FileInputStream in = new FileInputStream("config/config.properties");
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

        return ipAddress;       // Will return a null value if no IP is found 
    }
    

    /**
     *  Will try and grab available port for the machine.    
     *  @param key - Which node is trying to grab the port
     *  @return int, if port is found. If not found 0
     */

    public int generatePort(String key){

        int port = 0;
        try{
            port = Integer.parseInt(properties.getProperty(key));

        } catch (Error e){
            System.err.println("Error getting port from config file!\n");
            e.printStackTrace();
        }
        return port;
    }
}
