import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ApiServer {
    private final int port;
    private HttpServer server;
    private final MulticastManager multicastManager;
    private final List<String> messages = new ArrayList<>();

    public ApiServer(int port, String nickname) throws IOException {
        this.port = port;
        
        // Create a MulticastManager with a custom message receiver
        this.multicastManager = new MulticastManager(nickname, null) {
            @Override
            public void receiveMessage(String message) {
                synchronized (messages) {
                    messages.add(message);
                    System.out.println("Added message to list: " + message);
                }
            }
        };
        
        startServer();
        
        // Start receiving messages in a separate thread
        new Thread(() -> multicastManager.receiveMessages()).start();
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
            String message;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                message = br.lines().collect(Collectors.joining());
            }
            
            // Remove quotes if the message is wrapped in them
            if (message.startsWith("\"") && message.endsWith("\"")) {
                message = message.substring(1, message.length() - 1);
            }
            
            System.out.println("Sending message: " + message);
            
            // Send the message via multicast
            multicastManager.sendMessage(message);
            
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
    
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("API Server stopped");
        }
    }
}