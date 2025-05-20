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
            // Create a socket that's bound to any available port
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(0));
            socket.setSoTimeout(30000); // 30 second timeout
            
            // Get the multicast group address
            group = InetAddress.getByName(MULTICAST_GROUP);
            
            // Print network information for debugging
            System.out.println("Multicast group: " + MULTICAST_GROUP);
            System.out.println("Port: " + PORT);
            System.out.println("Local address: " + socket.getLocalAddress());
            System.out.println("Local port: " + socket.getLocalPort());
            
            // Update status bar with more detailed network information
            String networkDetails = "Multicast: " + MULTICAST_GROUP + ":" + PORT + 
                                   " | Local: " + socket.getLocalAddress() + ":" + socket.getLocalPort() +
                                   " | Socket: " + (socket.isBound() ? "Bound" : "Not Bound") +
                                   (socket.isConnected() ? ", Connected" : ", Not Connected") +
                                   " | Timeout: " + socket.getSoTimeout() + "ms" +
                                   " | TTL: " + socket.getTrafficClass() +
                                   " | Buffer: " + socket.getReceiveBufferSize() + "B";
            chatWindow.updateNetworkStatus(networkDetails);
            
            // Log all network interfaces for debugging
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
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                chatWindow.appendSystemMessage("Error setting up network: " + e.getMessage()));
        }
    }

    /** Encrypts and broadcasts a chat message. */
    public void sendMessage(String message) {
        try {
            // Check message size before encryption
            if (message.length() > 4096) {
                // Show warning for very large messages
                chatWindow.appendSystemMessage("Warning: Your message is very large (" + 
                                              message.length() + " characters). It may be truncated.");
            }
            
            String full = nickname + ": " + message;
            String encrypted = EncryptionUtils.encrypt(full);
            byte[] buffer = encrypted.getBytes();
            
            // Check if packet size exceeds UDP limit
            if (buffer.length > 65507) { // Max UDP packet size
                chatWindow.appendSystemMessage("Error: Message too large to send. Please send a shorter message.");
                return;
            }
            
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
            
            // Update statistics
            chatWindow.updateSentStatistics(buffer.length);
        } catch (Exception e) {
            e.printStackTrace();
            chatWindow.appendSystemMessage("Error sending message: " + e.getMessage());
        }
    }
    
    /** Send a heartbeat to let others know we're online */
    public void sendHeartbeat() {
        try {
            String heartbeat = "HEARTBEAT:" + nickname;
            String encrypted = EncryptionUtils.encrypt(heartbeat);
            byte[] buffer = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Send a goodbye message when leaving */
    public void sendGoodbye() {
        try {
            String goodbye = "GOODBYE:" + nickname;
            String encrypted = EncryptionUtils.encrypt(goodbye);
            byte[] buffer = encrypted.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Continuously listen, decrypt, and forward to GUI. */
    public void receiveMessages() {
        try {
            // Create a new MulticastSocket with specific settings for better compatibility
            MulticastSocket mcast = new MulticastSocket(PORT);
            mcast.setReuseAddress(true);
            mcast.setSoTimeout(0); // No timeout for receiving
            
            // Try multiple network interfaces if needed
            boolean joinedGroup = false;
            
            // First try the specific interface finder
            NetworkInterface nif = findMulticastInterface();
            if (nif != null) {
                try {
                    InetSocketAddress groupAddress = new InetSocketAddress(group, PORT);
                    mcast.joinGroup(groupAddress, nif);
                    // Update status bar with detailed network information
                    try {
                        Enumeration<InetAddress> addresses = nif.getInetAddresses();
                        StringBuilder ipInfo = new StringBuilder();
                        while (addresses.hasMoreElements()) {
                            InetAddress addr = addresses.nextElement();
                            if (addr instanceof Inet4Address) {
                                ipInfo.append(addr.getHostAddress()).append(" ");
                            }
                        }
                        
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
                        // Fallback to simple display if error occurs
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
                    // Only update status bar, don't add message to chat area
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
                            InetSocketAddress groupAddress = new InetSocketAddress(group, PORT);
                            mcast.joinGroup(groupAddress, iface);
                            // Update status bar with detailed network information
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
                                // Fallback to simple display if error occurs
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
            
            if (!joinedGroup) {
                // Only show error in status bar, not in chat
                chatWindow.updateNetworkStatus("ERROR: No multicast interface found | Status: Disconnected");
            }

            // Announce presence with compact message
            sendMessage("joined");
            
            // Receive messages in a loop
            byte[] buffer = new byte[65536]; // Increased buffer size for larger encrypted messages
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    mcast.receive(packet);
                    
                    // Update statistics with received packet size
                    chatWindow.updateReceivedStatistics(packet.getLength());
    
                    String encrypted = new String(packet.getData(), 0, packet.getLength());
                    String plaintext = EncryptionUtils.decrypt(encrypted);

                    // Handle special messages
                    if (plaintext.startsWith("HEARTBEAT:")) {
                        // Extract username from heartbeat
                        String user = plaintext.substring("HEARTBEAT:".length());
                        // Update user list
                        SwingUtilities.invokeLater(() -> chatWindow.addUserToList(user));
                    } else if (plaintext.startsWith("GOODBYE:")) {
                        // Extract username from goodbye message
                        String user = plaintext.substring("GOODBYE:".length());
                        // Remove from user list
                        SwingUtilities.invokeLater(() -> chatWindow.removeUserFromList(user));
                    } else {
                        // Regular message - update GUI on EDT
                        SwingUtilities.invokeLater(() -> chatWindow.appendMessage(plaintext));
                    }
                } catch (SocketTimeoutException e) {
                    // Timeout is normal, just continue
                    continue;
                } catch (Exception e) {
                    // Log error but continue receiving
                    System.err.println("Error processing received packet: " + e.getMessage());
                    // Don't show decryption errors to the user to avoid cluttering the chat
                    if (!e.getMessage().contains("Decryption failed") && 
                        !e.getMessage().contains("padding") &&
                        !e.getMessage().contains("bad key")) {
                        chatWindow.appendSystemMessage("Network error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Only show error in status bar, not in chat
            chatWindow.updateNetworkStatus("ERROR: Connection failed | Exception: " + e.getClass().getSimpleName() + " | " + e.getMessage());
        }
    }
    
    /**
     * Format MAC address as a readable string
     */
    private String formatMacAddress(byte[] mac) {
        if (mac == null) {
            return "Unknown";
        }
        
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
     * Find a suitable network interface for multicast
     */
    private NetworkInterface findMulticastInterface() {
        try {
            // First try to find a non-loopback interface that supports multicast
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp() && !nif.isLoopback() && nif.supportsMulticast()) {
                    // Check if it has an IPv4 address
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            return nif;
                        }
                    }
                }
            }
            
            // If no suitable interface found, try loopback as last resort
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
        
        return null;
    }
}
