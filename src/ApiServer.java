import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ApiServer {
    private final int port;
    private HttpServer server;
    private final MulticastManager multicastManager;
    private final List<String> messages = new ArrayList<>();
    private final Set<String> sentMessages = new HashSet<>(); // Track sent messages
    private final Thread receiveThread;

    public ApiServer(int port, String nickname) throws IOException {
        this.port = port;
        
        // Create a MulticastManager that doesn't add messages to the list
        // We'll handle message reception manually through a separate multicast socket
        this.multicastManager = new MulticastManager(nickname, null);
        
        // Start the multicast receiver in a separate thread
        receiveThread = new Thread(() -> {
            try {
                System.out.println("Starting to receive multicast messages...");
                MulticastSocket multicastSocket = new MulticastSocket(MulticastManager.PORT);
                InetAddress group = InetAddress.getByName(MulticastManager.MULTICAST_GROUP);
                multicastSocket.joinGroup(group);
                byte[] buffer = new byte[1024];
                
                while (!Thread.currentThread().isInterrupted()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received message: " + message);
                    
                    synchronized (messages) {
                        // Only add the message if we haven't sent it ourselves
                        if (!sentMessages.contains(message)) {
                            messages.add(message);
                            System.out.println("Added new message to list: " + message);
                        } else {
                            System.out.println("Ignoring message we sent ourselves: " + message);
                            // Remove from sent messages to keep the set from growing too large
                            sentMessages.remove(message);
                        }
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("Error receiving multicast messages: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
        
        startServer();
        
        System.out.println("MulticastManager started in a separate thread");
    }

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Create contexts for API endpoints
        server.createContext("/api/messages", new GetMessagesHandler());
        server.createContext("/api/sendMessage", new SendMessageHandler());
        
        // Set up thread pool for handling requests
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        
        System.out.println("API Server started on port " + port);
        System.out.println("You can now access the chat from your Next.js application");
    }

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private class GetMessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Set CORS headers to allow requests from any origin
            setCorsHeaders(exchange);
            
            // Handle preflight OPTIONS request
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            // Prepare JSON response with all messages
            String response;
            synchronized (messages) {
                response = "[" + messages.stream()
                        .map(msg -> "\"" + msg.replace("\"", "\\\"") + "\"")
                        .collect(Collectors.joining(",")) + "]";
            }
            
            // Send the response
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            
            System.out.println("Sent " + messages.size() + " messages to client");
        }
    }

    private class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Set CORS headers to allow requests from any origin
            setCorsHeaders(exchange);
            
            // Handle preflight OPTIONS request
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            // Read the message from the request body
            String messageData;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                messageData = br.lines().collect(Collectors.joining());
            }
            
            // Remove quotes if the message is wrapped in them
            if (messageData.startsWith("\"") && messageData.endsWith("\"")) {
                messageData = messageData.substring(1, messageData.length() - 1);
            }
            
            // Parse the JSON to get username and message
            // Format should be: {"username": "name", "message": "text"}
            String username = "";
            String message = messageData;
            
            // Check if it's JSON format
            if (messageData.startsWith("{") && messageData.endsWith("}")) {
                try {
                    // Simple JSON parsing (without external libraries)
                    messageData = messageData.substring(1, messageData.length() - 1);
                    String[] parts = messageData.split(",");
                    for (String part : parts) {
                        String[] keyValue = part.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            String value = keyValue[1].trim().replace("\"", "");
                            
                            if (key.equals("username")) {
                                username = value;
                            } else if (key.equals("message")) {
                                message = value;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                    // If parsing fails, use the entire message as is
                }
            }
            
            System.out.println("Sending message from " + username + ": " + message);
            
            // Format the message with the provided username, not Java's
            String formattedMessage = username + ": " + message;
            
            // Add the message to the messages list first
            synchronized (messages) {
                messages.add(formattedMessage);
                // Add to sent messages set so we don't duplicate it later
                sentMessages.add(formattedMessage);
            }
            
            // Send the raw formatted message via multicast
            try {
                byte[] buffer = formattedMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, 
                    multicastManager.group, 
                    MulticastManager.PORT
                );
                multicastManager.socket.send(packet);
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Send success response
            String response = "{\"status\":\"success\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            
            System.out.println("Message sent successfully");
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("API Server stopped");
        }
        
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
    }
}