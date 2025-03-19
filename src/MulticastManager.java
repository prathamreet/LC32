import java.io.IOException;
import java.net.*;

public class MulticastManager {
    // Make these protected instead of private so ApiServer can access them
    protected static final String MULTICAST_GROUP = "230.0.0.1";
    protected static final int PORT = 5000;
    protected DatagramSocket socket;
    protected InetAddress group;
    private String nickname;
    private ChatWindow chatWindow;

    public MulticastManager(String nickname, ChatWindow chatWindow) {
        this.nickname = nickname;
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
            // For direct use from the ChatWindow, still include the nickname
            String fullMessage = nickname + ": " + message;
            byte[] buffer = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            System.out.println("Sent message: " + fullMessage);
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
                String message = new String(packet.getData(), 0, packet.getLength());
                if (chatWindow != null) {
                    chatWindow.appendMessage(message);
                } else {
                    // For API server integration - override in subclasses
                    receiveMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Method to be overridden by ApiServer
    public void receiveMessage(String message) {
        // Default implementation does nothing
        System.out.println("Received message (not handled): " + message);
    }
}