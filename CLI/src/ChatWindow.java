import java.util.*;
import java.text.SimpleDateFormat;

public class ChatWindow {
    private String nickname;
    private String clientId;
    private MulticastManager multicastManager;
    private Scanner scanner;

    // Tracks active users: maps nickname - last-seen timestamp (ms)
    private final Map<String, Long> activeUsers = new HashMap<>();
    // How often we broadcast "i am here" (10 seconds)
    private static final long PRESENCE_INTERVAL = 10000;
    // If we haven’t heard from a user in 20 seconds, we consider them offline
    private static final long TIMEOUT = 20000;

    // Constructor: initialize fields, start background threads, then go into chat
    // loop
    public ChatWindow(String nickname) {
        this.nickname = nickname;
        // Generate a random UUID so we can ignore our own packets when receiving
        this.clientId = UUID.randomUUID().toString();
        this.scanner = new Scanner(System.in);
        // Link networking code with this chat window
        multicastManager = new MulticastManager(nickname, clientId, this);
        // Start a daemon thread that continuously listens for incoming packets
        Thread receiveThread = new Thread(multicastManager::receiveMessages);
        receiveThread.setDaemon(true);
        receiveThread.start();
        startPresenceThread();
        // Enter the main chat-input loop
        startChat();
    }

    // Fires off "I'm alive" messages every PRESENCE_INTERVAL ms
    private void startPresenceThread() {
        Thread presenceThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(PRESENCE_INTERVAL);
                    multicastManager.sendPresence();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        presenceThread.setDaemon(true);
        presenceThread.start();
    }

    // Main loop: read user input, interpret commands or send chat messages
    public void startChat() {
        System.out.println("Welcome to LAN Chat, " + nickname + "!");
        System.out.println("Type your messages below. Type '/help' for commands.");
        while (true) {
            try {
                String input = scanner.nextLine();
                // Commands start with '/'
                if (input.startsWith("/")) {
                    if ("/exit".equals(input)) {
                        System.out.println("Exiting chat...");
                        break;
                    } else if ("/help".equals(input)) {
                        System.out.println("Commands: /exit, /users, /pm <nickname> <message>, /help");
                    } else if ("/users".equals(input)) {
                        synchronized (this) {
                            System.out.println("Active users: " + String.join(", ", activeUsers.keySet()));
                        }
                    }
                    // Handle private-message command separately
                    else if (input.startsWith("/pm ")) {
                        String[] parts = input.substring(4).trim().split(" ", 2);
                        if (parts.length == 2) {
                            String targetNickname = parts[0];
                            String pmMessage = parts[1];
                            String time = new SimpleDateFormat("HH:mm").format(new Date());
                            String fullMessage = "[" + time + "] " + nickname + ": " + pmMessage;
                            multicastManager.sendPrivateMessage(targetNickname, fullMessage);
                        } else {
                            System.out.println("Usage: /pm <nickname> <message>");
                        }
                    } else {
                        System.out.println("Unknown command. Type '/help' for commands.");
                    }
                } else {
                     // Regular chat message to everyone
                    multicastManager.sendMessage(input);
                }
            } catch (NoSuchElementException e) {
                System.out.println("Input stream closed. Exiting chat...");
                break;
            }
        }
    }

    // Called by MulticastManager when a presence packet arrives
    public synchronized void updateUserList(String nickname, long timestamp) {
        activeUsers.put(nickname, timestamp);
        activeUsers.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > TIMEOUT);
    }

     // Display an incoming message, color-coding by sender
    public void appendMessage(String message) {
        int colonIndex = message.indexOf(": ");
        if (colonIndex != -1) {
             // Split off the "[HH:mm] nick" part vs. the actual message text
            String senderInfo = message.substring(0, colonIndex);
            String text = message.substring(colonIndex + 2);
            int bracketIndex = senderInfo.indexOf("] ");
            if (bracketIndex != -1) {
                String timestamp = senderInfo.substring(0, bracketIndex + 1);
                String nickname = senderInfo.substring(bracketIndex + 2);
                // Pick a consistent ANSI color based on nickname’s hash
                String color = getColorForNickname(nickname);
                String displayMessage = color + timestamp + " " + nickname + ": " + text + "\u001B[0m";
                System.out.println(displayMessage);
            } else {
                // Fallback: just print raw if parsing fails
                System.out.println(message);
            }
        } else {
            System.out.println(message);
        }
    }

    // Map each nickname deterministically to one of six colors
    private String getColorForNickname(String nickname) {
        String[] colors = {
                "\u001B[31m", "\u001B[32m", "\u001B[33m", "\u001B[34m", "\u001B[35m", "\u001B[36m"
        };
        int colorIndex = Math.abs(nickname.hashCode()) % colors.length;
        return colors[colorIndex];
    }

    public String getClientId() {
        return clientId;
    }

    
    // Allow running this class directly in simple tests
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ChatWindow <nickname>");
            return;
        }
        new ChatWindow(args[0]);
    }
}