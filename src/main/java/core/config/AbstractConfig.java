/**
 *  Author: Nathaniel Brewer
 * 
 *  Configuration for each of the tiers in the hierarchy. Will grab machine IP address and will try to generate avaiable ports.
 *  
 */
package core.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.Inet4Address;
import java.net.InetAddress;            
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;   

import org.apache.logging.log4j.Logger;

import core.identity.AbstractTierIdentity;
import core.identity.TierRole;

import java.util.Properties;          

public abstract class AbstractConfig {

    protected abstract Logger getLogger();

    protected static Properties properties = new Properties(); 

    protected Properties instanceProperties; 
    protected String instanceId; 

    protected AbstractTierIdentity tierDTO; 

    protected static String defaultConfigPath; 
    protected String instanceConfigPath;


    public AbstractConfig(AbstractTierIdentity tierDTO) {

        this.tierDTO = tierDTO;

        defaultConfigPath = tierDTO.getDefaultConfigPath();
        instanceConfigPath = tierDTO.getInstanceConfigPath();

        getLogger().info("DefaultConfig " + defaultConfigPath);
        getLogger().info("InstanceConfig " + instanceConfigPath);

        instanceId = tierDTO.getInstanceId();

        this.instanceProperties = properties;
        this.instanceProperties.clear();

        try(FileInputStream in = new FileInputStream(defaultConfigPath)){
            instanceProperties.load(in);
            getLogger().info("Loaded default config from: {}", defaultConfigPath);
        } catch(IOException e) {
            getLogger().error("Could not open default properties file!", e);
        }
        

        try {
            getLogger().info("Loaded instance config for: " + instanceId);
            FileInputStream in = new FileInputStream(instanceConfigPath);
            instanceProperties.load(in);
            in.close();
            getLogger().info("Instance config contains " + instanceProperties.size() + " properties:");
            for (String key : instanceProperties.stringPropertyNames()) {
                getLogger().info("  " + key + " = " + instanceProperties.getProperty(key));
            }
        } catch(IOException e) {
            getLogger().error("No instance config found for: " + instanceId + ". using default config instead.");
            instanceProperties = properties;
            instanceConfigPath = null; 
            getLogger().info("Fallback to default config contains " + instanceProperties.size() + " properties:");
            for (String key : instanceProperties.stringPropertyNames()) {
                getLogger().info("  " + key + " = " + instanceProperties.getProperty(key));
            }
        }
    }

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
        
        // For local testing only
        // TODO: REMOVE IN DEPLOYMENT
        writeToConfig("Coordinator.IP", realIp);
        if(tierDTO.getRole() != TierRole.COORDINATOR) {
            String higherTierEnumKey = tierDTO.getHigherTier() + ".IP";
            writeToConfig(higherTierEnumKey, realIp);

            String higherTierName = tierDTO.getHigherTier().name();
            String higherTierCanonicalKey =
                higherTierName.substring(0, 1) + higherTierName.substring(1).toLowerCase() + ".IP";

            if(!higherTierCanonicalKey.equals(higherTierEnumKey)) {
                writeToConfig(higherTierCanonicalKey, realIp);
            }
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
            String portString = instanceProperties.getProperty(key);
            port = Integer.parseInt(portString);
            getLogger().info("Retrieved port for key '" + key + "': " + port + " from " + 
                (instanceId != null ? "instance config (ID: " + instanceId + ")" : "default config"));
        } catch (Exception e){
            getLogger().error("Error getting port with key: {}", key);
        }
        return port;
    }

    public String getIPByKey(String key) {

        String IP = "";
        try{
            IP = instanceProperties.getProperty(key);
        } catch (Exception e) {
            getLogger().error("Error getting " + key + "'s IP from config file!\n" + e);
        }
        return IP;
    }

    private OutputStream openFile(String filePath) throws IOException {

        OutputStream out;

        try{
            out = new FileOutputStream(filePath);
            return out;
        } catch (Exception e) {
            getLogger().error("File from path: {}, could not be opened!", filePath);
        }

        throw new IOException("Could not open File from path: " + filePath + " could not be opened!");
    }


    /**
     *  Will try to write/overwrite a key/value pair in config.properties    
     *  @param key - will look for this key in the config, if not there it will write it there
     *  @param value - the value to the key that will be inserted once the key is either found or written
     */
    public void writeToConfig(String key, String value) {

        try{
            instanceProperties.setProperty(key, value);

            String configPath = (instanceId != null && instanceConfigPath != null) ? instanceConfigPath : defaultConfigPath;

            try(OutputStream outputStream = openFile(configPath)){
                instanceProperties.store(outputStream, null);
                getLogger().info("Overwrote:\t Key: ( " + key + " )\t Value: ( " + value + " ) in file: " + configPath);
            } catch(IOException ioe) {
                getLogger().error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to config file: " + configPath + "\n" + ioe);
                
                if(instanceId != null && instanceConfigPath != null && !configPath.equals(defaultConfigPath)) {
                    getLogger().info("Falling back to default config file...");
                    try(OutputStream outputStream = openFile(defaultConfigPath)) {
                        instanceProperties.store(outputStream, null);
                        getLogger().info("Overwrote:\t Key: ( " + key + " )\t Value: ( " + value + " ) in default file: " + defaultConfigPath);
                    } catch(IOException e) {
                        getLogger().error("Error adding value: ( " + value + " ) to key: ( " + key + " ) to default config file!\n" + e);
                    }
                }
            } catch(Exception e) {
                getLogger().error("Unknown error adding value: ( " + value + " ) to key: ( " + key + " ) to config file!\n" + e);
            }

        }catch (Exception e) {
            getLogger().error("Error writing key: ( " + key + " ) to config file!\n" + e);
        }

    }

}