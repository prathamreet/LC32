import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import javax.swing.SwingUtilities;  // ✅ this one is missing

/**
 * Handles encrypted multicast send/receive for Swing UI.
 */
public class MulticastManager {
    private static final String MULTICAST_GROUP = "230.0.0.1";
    private static final int PORT = 5000;

    private DatagramSocket socket;
    private InetAddress group;
    private final String nickname;
    private final ChatWindow chatWindow;

    public MulticastManager(String nickname, ChatWindow chatWindow) {
        this.nickname   = nickname;
        this.chatWindow = chatWindow;
        setupNetworking();
    }

    private void setupNetworking() {
        try {
            socket = new DatagramSocket();
            group  = InetAddress.getByName(MULTICAST_GROUP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Encrypts and broadcasts a chat message. */
    public void sendMessage(String message) {
        try {
            String full = nickname + ": " + message;
            String encrypted = EncryptionUtils.encrypt(full);
            byte[] buffer = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Continuously listen, decrypt, and forward to GUI. */
    public void receiveMessages() {
        try (MulticastSocket mcast = new MulticastSocket(PORT)) {
            // Select a working IPv4 interface
            NetworkInterface nif = null;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface i = interfaces.nextElement();
                if (i.isUp() && !i.isLoopback() && i.supportsMulticast()) {
                    nif = i;
                    break;
                }
            }
            if (nif == null) throw new IOException("No multicast interface");

            SocketAddress groupAddr = new InetSocketAddress(group, PORT);
            mcast.joinGroup(groupAddr, nif);

            byte[] buffer = new byte[2048];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mcast.receive(packet);

                String encrypted = new String(packet.getData(), 0, packet.getLength());
                String plaintext = EncryptionUtils.decrypt(encrypted);

                // Update GUI on EDT
                SwingUtilities.invokeLater(() -> chatWindow.appendMessage(plaintext));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
