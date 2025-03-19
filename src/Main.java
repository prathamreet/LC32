import javax.swing.*;

public class Main {
    private static final int API_PORT = 8080;

    public static void main(String[] args) {
        // Get nickname
        String nickname = null;
        if (args.length > 0) {
            nickname = args[0];
        } else {
            nickname = JOptionPane.showInputDialog("Enter your nickname:");
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            // Start the GUI if needed
            boolean startGui = false;
            
            if (args.length > 1) {
                startGui = args[1].equalsIgnoreCase("gui");
            } else {
                startGui = JOptionPane.showConfirmDialog(null, 
                    "Would you like to start the GUI version?", 
                    "Start Option", 
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            }
            
            if (startGui) {
                System.out.println("Starting GUI with nickname: " + nickname);
                new ChatWindow(nickname);
            } else {
                // Start the API server for the Next.js frontend
                try {
                    System.out.println("Starting API server with nickname: " + nickname);
                    ApiServer apiServer = new ApiServer(API_PORT, nickname);
                    
                    // Keep the program running
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
        } else {
            System.out.println("No nickname provided. Exiting.");
            System.exit(0);
        }
    }
}