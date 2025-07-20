package server_logger;

import java.util.logging.*;
import java.io.*;

public class ServerLogger {

    private static final Logger logger = Logger.getLogger(ServerLogger.class.getName());
    
    static {
        try {
            Handler fileHandler = new FileHandler("server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to initialize file handler for logger", ioe);

        }
    }

    public static void formatter(String classification, String message) {

        // TODO: create a standard format for all logging.

    }

    private static void info(String message) {
        logger.log(Level.INFO, message);
    }

    private static void warning(String message) {

    }

    private static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

}
