import java.util.*;
import java.text.SimpleDateFormat;

public class ChatWindow {
    private String nickname;
    private String clientId; // Unique ID for this client
    private MulticastManager multicastManager;
    private Scanner scanner;
    private Map<String, Long> activeUsers = new HashMap<>();
    private static final long PRESENCE_INTERVAL = 10000; // 10 seconds
    private static final long TIMEOUT = 20000;           // 20 seconds

    public ChatWindow(String nickname) {
        this.nickname = nickname;
        this.clientId = UUID.randomUUID().toString(); // Generate unique ID
        this.scanner = new Scanner(System.in);
        multicastManager = new MulticastManager(nickname, clientId, this);
        new Thread(multicastManager::receiveMessages).start();
        startPresenceThread();
        startChat();
    }

    private void startPresenceThread() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(PRESENCE_INTERVAL);
                    multicastManager.sendPresence();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void startChat() {
        System.out.println("Welcome to LAN Chat, " + nickname + "!");
        System.out.println("Type your messages below. Type '/help' for commands.");
        while (true) {
            try {
                String input = scanner.nextLine();
                if (input.startsWith("/")) {
                    if ("/exit".equals(input)) {
                        System.out.println("Exiting chat...");
                        break;
                    } else if ("/help".equals(input)) {
                        System.out.println("Commands: /exit, /users, /pm <nickname> <message>, /help");
                    } else if ("/users".equals(input)) {
                        System.out.println("Active users: " + String.join(", ", activeUsers.keySet()));
                    } else {
                        System.out.println("Unknown command. Type '/help' for commands.");
                    }
                } else {
                    multicastManager.sendMessage(input);
                }
            } catch (NoSuchElementException e) {
                System.out.println("Input stream closed. Exiting chat...");
                break;
            }
        }
    }

    public void appendMessage(String message) {
        // message format: "[HH:mm] nickname: message"
        int colonIndex = message.indexOf(": ");
        if (colonIndex != -1) {
            String senderInfo = message.substring(0, colonIndex); // "[HH:mm] nickname"
            String text = message.substring(colonIndex + 2);     // "message"
            // Extract nickname from senderInfo
            int bracketIndex = senderInfo.indexOf("] ");
            if (bracketIndex != -1) {
                String timestamp = senderInfo.substring(0, bracketIndex + 1); // "[HH:mm]"
                String nickname = senderInfo.substring(bracketIndex + 2);     // "nickname"
                String coloredNickname = getColoredNickname(nickname);        // Apply color
                String displayMessage = timestamp + " " + coloredNickname + ": " + text;
                System.out.println(displayMessage); // Output the formatted message
            } else {
                System.out.println(message); // Fallback if format is unexpected
            }
        } else {
            System.out.println(message); // Fallback for malformed messages
        }
    }

    private String getColoredNickname(String nickname) {
        String[] colors = {
            "\u001B[31m", // Red
            "\u001B[32m", // Green
            "\u001B[33m", // Yellow
            "\u001B[34m", // Blue
            "\u001B[35m", // Magenta
            "\u001B[36m"  // Cyan
        };
        int colorIndex = Math.abs(nickname.hashCode()) % colors.length;
        return colors[colorIndex] + nickname + "\u001B[0m"; // Reset color after nickname
    }

    public void updateUserList(String nickname, long timestamp) {
        activeUsers.put(nickname, timestamp);
        activeUsers.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > TIMEOUT);
    }

    public String getClientId() {
        return clientId;
    }
}