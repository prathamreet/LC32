import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MulticastManager {
    // Make these protected instead of private so ApiServer can access them
    protected static final String MULTICAST_GROUP = "230.0.0.1";
    protected static final int PORT = 5000;
    protected DatagramSocket socket;
    protected InetAddress group;
    private String nickname;
    private String clientId;
    private ChatWindow chatWindow;

    public MulticastManager(String nickname, String clientId, ChatWindow chatWindow) {
        this.nickname = nickname;
        this.clientId = clientId;
        this.chatWindow = chatWindow;
        setupNetworking();
    }

    private void setupNetworking() {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(MULTICAST_GROUP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
<<<<<<< HEAD
            // For direct use from the ChatWindow, still include the nickname
            String fullMessage = nickname + ": " + message;
=======
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            String fullMessage = clientId + "|[" + time + "] " + nickname + ": " + message;
>>>>>>> cli
            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            System.out.println("Sent message: " + fullMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPrivateMessage(String targetNickname, String message) {
        try {
            String fullMessage = clientId + "|pm|" + targetNickname + "|" + message;
            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPresence() {
        try {
            String presenceMessage = clientId + "|presence|" + nickname;
            byte[] buffer = presenceMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessages() {
        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {
            multicastSocket.joinGroup(group);
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
<<<<<<< HEAD
                String message = new String(packet.getData(), 0, packet.getLength());
                if (chatWindow != null) {
                    chatWindow.appendMessage(message);
                } else {
                    // For API server integration - override in subclasses
                    receiveMessage(message);
=======
                String received = new String(packet.getData(), 0, packet.getLength());
                String[] parts = received.split("\\|", 4);
                if (parts.length == 4 && "pm".equals(parts[1])) {
                    String senderClientId = parts[0];
                    String targetNickname = parts[2];
                    String message = parts[3];
                    if (targetNickname.equals(nickname) && !senderClientId.equals(clientId)) {
                        chatWindow.appendMessage(message);
                    }
                } else if (parts.length == 3 && "presence".equals(parts[1])) {
                    chatWindow.updateUserList(parts[2], System.currentTimeMillis());
                } else if (parts.length == 2 && !parts[0].equals(clientId)) {
                    chatWindow.appendMessage(parts[1]);
>>>>>>> cli
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
    
    // Method to be overridden by ApiServer
    public void receiveMessage(String message) {
        // Default implementation does nothing
        System.out.println("Received message (not handled): " + message);
    }
=======
>>>>>>> cli
}