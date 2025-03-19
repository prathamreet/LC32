import java.io.IOException;
import java.net.*;

public class MulticastManager {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 5000;
    private DatagramSocket socket;
    private InetAddress group;
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
            String fullMessage = nickname + ": " + message;
            byte[] buffer = fullMessage.getBytes();
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
                String message = new String(packet.getData(), 0, packet.getLength());
                chatWindow.appendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
