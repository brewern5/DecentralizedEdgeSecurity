package server_logger;

import java.util.logging.*;
import java.io.IOException;

public class ServerLogger {

    private static Logger logger;
    
    static {
        setupLogger();
    }

    private static void setupLogger() {
        logger = Logger.getLogger("ServerLog");
        logger.setUseParentHandlers(false);

        try {
            // Create file handler
            Handler fileHandler = new FileHandler("logs/server_logs.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.INFO);

            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);

            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.out.println("Failed to instantiate logger: " +e.getMessage());
        }
        
    }

    public static void logException(Exception e) {
        logger.log(Level.WARNING, "EXCEPTION HAS OCCURED!", e);
    }

    public static void logEvent(String message) {
        logger.log(Level.INFO, message);
    }


    public static void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    public static Logger getLogger() {
        return logger;
    }

}
