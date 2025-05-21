import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import javax.swing.SwingUtilities;

/**
 * This class handles all the network communication for our chat app.
 * It sends and receives messages using UDP multicast, which allows
 * messages to be sent to multiple users at once on a local network.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class MulticastManager {
    // Network settings
    private static final String MULTICAST_GROUP = "230.0.0.1"; // Special address for multicast
    private static final int PORT = 5000;                      // Port we'll use for communication

    // Network components
    private DatagramSocket socket;    // Socket for sending messages
    private InetAddress group;        // The multicast group address
    private final String nickname;    // User's nickname
    private final ChatWindow chatWindow; // Reference to the chat window for updates

    /**
     * Creates a new MulticastManager to handle network communication.
     * 
     * @param nickname The user's nickname
     * @param chatWindow The chat window to update with messages
     */
    public MulticastManager(String nickname, ChatWindow chatWindow) {
        this.nickname = nickname;
        this.chatWindow = chatWindow;
        setupNetworking(); // Set up the network connection
    }

    /**
     * Sets up the network connection for sending and receiving messages.
     */
    private void setupNetworking() {
        try {
            // Create a socket that can send to any address
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true); // Allow address reuse for better compatibility
            socket.bind(new InetSocketAddress(0)); // Bind to any available port
            socket.setSoTimeout(30000); // 30 second timeout for operations
            
            // Get the multicast group address we'll send messages to
            group = InetAddress.getByName(MULTICAST_GROUP);
            
            // Print network information for debugging
            System.out.println("Multicast group: " + MULTICAST_GROUP);
            System.out.println("Port: " + PORT);
            System.out.println("Local address: " + socket.getLocalAddress());
            System.out.println("Local port: " + socket.getLocalPort());
            
            // Show detailed network information in the status bar
            String networkDetails = "Multicast: " + MULTICAST_GROUP + ":" + PORT + 
                                   " | Local: " + socket.getLocalAddress() + ":" + socket.getLocalPort() +
                                   " | Socket: " + (socket.isBound() ? "Bound" : "Not Bound") +
                                   (socket.isConnected() ? ", Connected" : ", Not Connected") +
                                   " | Timeout: " + socket.getSoTimeout() + "ms" +
                                   " | TTL: " + socket.getTrafficClass() +
                                   " | Buffer: " + socket.getReceiveBufferSize() + "B";
            chatWindow.updateNetworkStatus(networkDetails);
            
            // Check all network interfaces to find ones that support multicast
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp()) {
                    System.out.println("Interface: " + nif.getDisplayName() + 
                                      ", Multicast: " + nif.supportsMulticast() +
                                      ", Loopback: " + nif.isLoopback());
                }
            }
            
        } catch (IOException e) {
            // If something goes wrong, show an error message
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                chatWindow.appendSystemMessage("Error setting up network: " + e.getMessage()));
        }
    }

    /**
     * Encrypts and sends a chat message to everyone in the multicast group.
     * 
     * @param message The message to send
     */
    public void sendMessage(String message) {
        try {
            // Check if the message is very large
            if (message.length() > 4096) {
                // Warn the user about large messages
                chatWindow.appendSystemMessage("Warning: Your message is very large (" + 
                                              message.length() + " characters). It may be truncated.");
            }
            
            // Add the nickname to the message
            String full = nickname + ": " + message;
            
            // Encrypt the message for security
            String encrypted = EncryptionUtils.encrypt(full);
            byte[] buffer = encrypted.getBytes();
            
            // Make sure the message isn't too big for UDP
            if (buffer.length > 65507) { // Max UDP packet size
                chatWindow.appendSystemMessage("Error: Message too large to send. Please send a shorter message.");
                return;
            }
            
            // Create and send the packet to the multicast group
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            
            // Update statistics in the UI
            chatWindow.updateSentStatistics(buffer.length);
        } catch (Exception e) {
            // If something goes wrong, show an error message
            e.printStackTrace();
            chatWindow.appendSystemMessage("Error sending message: " + e.getMessage());
        }
    }
    
    /**
     * Sends a heartbeat message to let others know we're online.
     * This is sent periodically to keep the user list updated.
     */
    public void sendHeartbeat() {
        try {
            // Create a special heartbeat message with our nickname
            String heartbeat = "HEARTBEAT:" + nickname;
            
            // Encrypt and send it
            String encrypted = EncryptionUtils.encrypt(heartbeat);
            byte[] buffer = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a goodbye message when leaving the chat.
     * This lets others know to remove us from their user list.
     */
    public void sendGoodbye() {
        try {
            // Create a special goodbye message with our nickname
            String goodbye = "GOODBYE:" + nickname;
            
            // Encrypt and send it
            String encrypted = EncryptionUtils.encrypt(goodbye);
            byte[] buffer = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Continuously listens for incoming messages, decrypts them,
     * and updates the chat window. This runs in its own thread.
     */
    public void receiveMessages() {
        try {
            // Create a special socket for receiving multicast messages
            MulticastSocket mcast = new MulticastSocket(PORT);
            mcast.setReuseAddress(true);
            mcast.setSoTimeout(0); // No timeout - wait forever for messages
            
            // We need to join the multicast group to receive messages
            boolean joinedGroup = false;
            
            // First try to find the best network interface
            NetworkInterface nif = findMulticastInterface();
            if (nif != null) {
                try {
                    // Try to join the multicast group on this interface
                    InetSocketAddress groupAddress = new InetSocketAddress(group, PORT);
                    mcast.joinGroup(groupAddress, nif);
                    
                    // Show detailed network information in the status bar
                    try {
                        // Get all IP addresses for this interface
                        Enumeration<InetAddress> addresses = nif.getInetAddresses();
                        StringBuilder ipInfo = new StringBuilder();
                        while (addresses.hasMoreElements()) {
                            InetAddress addr = addresses.nextElement();
                            if (addr instanceof Inet4Address) {
                                ipInfo.append(addr.getHostAddress()).append(" ");
                            }
                        }
                        
                        // Create a detailed status message
                        String networkDetails = "Interface: " + nif.getDisplayName() + 
                                              " | IP: " + ipInfo + 
                                              " | Multicast: " + MULTICAST_GROUP + ":" + PORT +
                                              " | MTU: " + nif.getMTU() +
                                              " | MAC: " + formatMacAddress(nif.getHardwareAddress()) +
                                              " | Speed: " + (nif.isVirtual() ? "Virtual" : "Physical") +
                                              " | Status: " + (nif.isUp() ? "UP" : "DOWN") +
                                              (nif.isLoopback() ? " (Loopback)" : "");
                        chatWindow.updateNetworkStatus(networkDetails);
                    } catch (Exception e) {
                        // If we can't get detailed info, just show the interface name
                        chatWindow.updateNetworkStatus(nif.getDisplayName());
                    }
                    joinedGroup = true;
                } catch (Exception e) {
                    System.err.println("Failed to join multicast group on interface " + nif.getDisplayName() + ": " + e.getMessage());
                }
            }
            
            // If that didn't work, try the default interface
            if (!joinedGroup) {
                try {
                    mcast.joinGroup(group);
                    chatWindow.updateNetworkStatus("Default Network Interface");
                    joinedGroup = true;
                } catch (Exception e) {
                    System.err.println("Failed to join multicast group on default interface: " + e.getMessage());
                }
            }
            
            // If still not joined, try all interfaces one by one
            if (!joinedGroup) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements() && !joinedGroup) {
                    NetworkInterface iface = interfaces.nextElement();
                    if (iface.isUp() && iface.supportsMulticast()) {
                        try {
                            // Try to join on this interface
                            InetSocketAddress groupAddress = new InetSocketAddress(group, PORT);
                            mcast.joinGroup(groupAddress, iface);
                            
                            // Show detailed network information
                            try {
                                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                                StringBuilder ipInfo = new StringBuilder();
                                while (addresses.hasMoreElements()) {
                                    InetAddress addr = addresses.nextElement();
                                    if (addr instanceof Inet4Address) {
                                        ipInfo.append(addr.getHostAddress()).append(" ");
                                    }
                                }
                                
                                String networkDetails = "Interface: " + iface.getDisplayName() + 
                                                      " | IP: " + ipInfo + 
                                                      " | Multicast: " + MULTICAST_GROUP + ":" + PORT +
                                                      " | MTU: " + iface.getMTU() +
                                                      " | MAC: " + formatMacAddress(iface.getHardwareAddress()) +
                                                      " | Speed: " + (iface.isVirtual() ? "Virtual" : "Physical") +
                                                      " | Status: " + (iface.isUp() ? "UP" : "DOWN") +
                                                      (iface.isLoopback() ? " (Loopback)" : "");
                                chatWindow.updateNetworkStatus(networkDetails);
                            } catch (Exception e) {
                                chatWindow.updateNetworkStatus(iface.getDisplayName());
                            }
                            joinedGroup = true;
                            break;
                        } catch (Exception e) {
                            // Continue to next interface
                        }
                    }
                }
            }
            
            // If we couldn't join any group, show an error
            if (!joinedGroup) {
                chatWindow.updateNetworkStatus("ERROR: No multicast interface found | Status: Disconnected");
            }

            // Let everyone know we've joined
            sendMessage("joined");
            
            // Now start receiving messages in a loop
            byte[] buffer = new byte[65536]; // Large buffer for encrypted messages
            while (true) {
                try {
                    // Wait for a packet to arrive
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mcast.receive(packet);
                    
                    // Update statistics with the size of the received packet
                    chatWindow.updateReceivedStatistics(packet.getLength());
    
                    // Convert the packet data to a string and decrypt it
                    String encrypted = new String(packet.getData(), 0, packet.getLength());
                    String plaintext = EncryptionUtils.decrypt(encrypted);

                    // Handle different types of messages
                    if (plaintext.startsWith("HEARTBEAT:")) {
                        // This is a heartbeat message - update the user list
                        String user = plaintext.substring("HEARTBEAT:".length());
                        SwingUtilities.invokeLater(() -> chatWindow.addUserToList(user));
                    } else if (plaintext.startsWith("GOODBYE:")) {
                        // This is a goodbye message - remove the user from the list
                        String user = plaintext.substring("GOODBYE:".length());
                        SwingUtilities.invokeLater(() -> chatWindow.removeUserFromList(user));
                    } else {
                        // This is a regular chat message - add it to the chat
                        SwingUtilities.invokeLater(() -> chatWindow.appendMessage(plaintext));
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout is normal, just continue
                    continue;
                } catch (Exception e) {
                    // Log the error but keep receiving
                    System.err.println("Error processing received packet: " + e.getMessage());
                    
                    // Don't show decryption errors to avoid cluttering the chat
                    if (!e.getMessage().contains("Decryption failed") && 
                        !e.getMessage().contains("padding") &&
                        !e.getMessage().contains("bad key")) {
                        chatWindow.appendSystemMessage("Network error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // If something goes wrong with the whole receive loop
            e.printStackTrace();
            chatWindow.updateNetworkStatus("ERROR: Connection failed | Exception: " + e.getClass().getSimpleName() + " | " + e.getMessage());
        }
    }
    
    /**
     * Formats a MAC address as a readable string (like 00:11:22:33:44:55).
     * 
     * @param mac The MAC address bytes
     * @return A formatted MAC address string
     */
    private String formatMacAddress(byte[] mac) {
        if (mac == null) {
            return "Unknown";
        }
        
        // Convert each byte to a hex value and join with colons
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X", mac[i]));
            if (i < mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }
    
    /**
     * Finds the best network interface for multicast communication.
     * 
     * @return The best network interface, or null if none found
     */
    private NetworkInterface findMulticastInterface() {
        try {
            // First try to find a regular network interface (not loopback)
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp() && !nif.isLoopback() && nif.supportsMulticast()) {
                    // Make sure it has an IPv4 address
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            return nif;
                        }
                    }
                }
            }
            
            // If no regular interface works, try loopback as a last resort
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp() && nif.isLoopback() && nif.supportsMulticast()) {
                    return nif;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // If we couldn't find any suitable interface
        return null;
    }
}
