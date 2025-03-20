import javax.swing.*;

public class Main {
    private static final int API_PORT = 8080;

    public static void main(String[] args) {
        // No more GUI dialog prompts - just start the API server directly
        try {
            System.out.println("Starting LAN Chat API server on port " + API_PORT);
            // Start with a default nickname (will be overridden by frontend users)
            ApiServer apiServer = new ApiServer(API_PORT, "server");
            
            // Keep the program running
            System.out.println("API server started successfully. Access your Next.js app to connect.");
            System.out.println("Press Ctrl+C to exit");
            
            // Add shutdown hook to properly close resources
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                apiServer.stop();
            }));
        } catch (Exception e) {
            System.err.println("Failed to start API server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}



/*
cd ..
javac src/*.java -d out
cd out
java main
or---
javac -d out src/*.java
jar cvfe LANChat.jar Main -C out .
java -jar LANChat.jar

 */