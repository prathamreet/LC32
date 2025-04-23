import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;
import java.text.SimpleDateFormat;

public class MulticastManager {
    // Group address and port for UDP multicast
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 5000;
    // Raw socket for sending; MulticastSocket for receiving
    private DatagramSocket socket;
    private InetAddress group;

    private String nickname;
    private String clientId;
    private ChatWindow chatWindow;

    public MulticastManager(String nickname, String clientId, ChatWindow chatWindow) {
        this.nickname = nickname;
        this.clientId = clientId;
        this.chatWindow = chatWindow;
        setupNetworking();
    }

    // Create a DatagramSocket and resolve the multicast group address
    private void setupNetworking() {
        try {
            socket = new DatagramSocket();
            group = InetAddress.getByName(MULTICAST_GROUP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send a public chat message (encrypted) to the multicast group
    public void sendMessage(String message) {
        try {
            String time = new SimpleDateFormat("HH:mm").format(new Date());
            String fullMessage = clientId + "|[" + time + "] " + nickname + ": " + message;

            String encryptedMessage = EncryptionUtils.encrypt(fullMessage);
            byte[] buffer = encryptedMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send a private message marked with "pm" and the target nickname
    public void sendPrivateMessage(String targetNickname, String message) {
        try {
            String fullMessage = clientId + "|pm|" + targetNickname + "|" + message;
            String encryptedMessage = EncryptionUtils.encrypt(fullMessage);

            byte[] buffer = encryptedMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Send a presence heartbeat so others know we’re online
    public void sendPresence() {
        try {
            String presenceMessage = clientId + "|presence|" + nickname;
            String encryptedMessage = EncryptionUtils.encrypt(presenceMessage);

            byte[] buffer = encryptedMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Continuously listen for incoming multicast packets
    public void receiveMessages() {
        try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {

            NetworkInterface nif = null;

            // Automatically pick a usable IPv4 interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface i = interfaces.nextElement();
                if (i.isUp() && !i.isLoopback() && i.supportsMulticast()) {
                    nif = i;
                    break;
                }
            }

            if (nif == null) {
                throw new IOException("No suitable IPv4 network interface found.");
            }

            SocketAddress groupAddr = new InetSocketAddress(group, PORT);
            multicastSocket.joinGroup(groupAddr, nif);

            // multicastSocket.joinGroup(group);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String encryptedReceived = new String(packet.getData(), 0, packet.getLength());
                String received = EncryptionUtils.decrypt(encryptedReceived);
                // Split into up to 4 parts based on our simple protocol
                String[] parts = received.split("\\|", 4);

                // Private message?
                if (parts.length == 4 && "pm".equals(parts[1])) {
                    String senderClientId = parts[0];
                    String targetNickname = parts[2];
                    String message = parts[3];
                    // Only deliver if it’s addressed to me and not from me
                    if (targetNickname.equals(nickname) && !senderClientId.equals(clientId)) {
                        chatWindow.appendMessage(message);
                    }
                }
                // Presence update?
                else if (parts.length == 3 && "presence".equals(parts[1])) {
                    chatWindow.updateUserList(parts[2], System.currentTimeMillis());
                }
                // Regular public message?
                else if (parts.length == 2 && !parts[0].equals(clientId)) {
                    chatWindow.appendMessage(parts[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}