# Secure Local Chat Application - Technical Report

## Overview

This document provides a comprehensive technical overview of our secure local chat application. The application enables users on the same local network to communicate securely through an encrypted chat interface. Messages are encrypted using AES-128 in CBC mode, ensuring that even if network traffic is intercepted, the content remains protected.

## Core Concepts

### Multicast Communication

The application uses UDP multicast for network communication, which allows a single message to be efficiently delivered to multiple recipients on a local network. This is ideal for a chat application where each message needs to reach all connected users.

Key multicast concepts implemented:
- **Multicast Group**: All clients join the same multicast group (230.0.0.1) to receive messages
- **Port-based Communication**: All communication happens on port 5000
- **Interface Detection**: The application automatically detects the best network interface for multicast communication
- **Heartbeat Messages**: Special messages are sent periodically to maintain the user list

### Message Encryption

All messages are encrypted using AES-128 in CBC (Cipher Block Chaining) mode with PKCS5 padding:

1. **Encryption Process**:
   - A random Initialization Vector (IV) is generated for each message
   - The message is encrypted using the IV and a predefined key
   - The IV is prepended to the encrypted data
   - The combined data is encoded as Base64 for transmission

2. **Decryption Process**:
   - The Base64 string is decoded
   - The IV is extracted from the beginning of the data
   - The remaining data is decrypted using the IV and key
   - The original message is recovered

This approach ensures that even identical messages will have different encrypted forms, preventing pattern analysis.

### User Interface

The application features a modern, user-friendly interface with:
- **Chat Bubbles**: Messages appear in bubbles, with the user's messages on the right and others' on the left
- **User List**: Shows all currently active users with avatars and status indicators
- **Code Block Formatting**: Special formatting for code blocks with syntax highlighting
- **Theme Support**: Visual themes can be applied to customize the appearance

## Code Structure

### Main Components

1. **ChatWindow.java**
   - The main application window that contains all UI components
   - Manages the overall application state and user interactions
   - Coordinates between the UI and network components

2. **MulticastManager.java**
   - Handles all network communication
   - Sends and receives encrypted messages via UDP multicast
   - Manages user presence through heartbeat messages
   - Automatically selects the best network interface

3. **EncryptionUtils.java**
   - Provides encryption and decryption services
   - Implements AES-128/CBC/PKCS5Padding encryption
   - Handles message size limits and error recovery

4. **ChatPanel.java**
   - Displays the chat messages in a scrollable area
   - Manages the layout and rendering of message bubbles
   - Handles auto-scrolling to show new messages

5. **ChatBubbleRenderer.java**
   - Renders individual chat messages as bubbles
   - Supports different styles for user messages, others' messages, and system messages
   - Implements special formatting for code blocks with syntax highlighting
   - Provides copy functionality through context menus

6. **UserListRenderer.java**
   - Creates custom rendering for the user list
   - Displays users with avatars and status indicators
   - Highlights the current user in the list

7. **ThemeManager.java**
   - Manages the visual themes of the application
   - Provides color schemes for different UI elements
   - Allows switching between light and dark themes

## Detailed Analysis of Source Files

### 1. Main.java

The entry point of the application that initializes the chat window.

**Key Components:**
- **Main Method**: Initializes the application with proper look and feel
- **System Properties Setup**: Configures Java system properties for optimal UI rendering
- **Exception Handling**: Provides graceful error handling during startup
- **UI Thread Management**: Ensures UI components are created on the Event Dispatch Thread

### 2. ChatWindow.java

The primary window of the application that contains all UI components and coordinates their interactions.

**Key Components:**
- **UI Components**:
  - **JFrame**: The main application window with custom title bar and icon
  - **JSplitPane**: Divides the window into chat area and user list
  - **JTextField**: Input field for typing messages
  - **JButton**: Send button with icon and hover effects
  - **JMenuBar**: Contains application menus for settings and help
  - **StatusBar**: Shows connection status and message statistics

- **Event Handlers**:
  - **ActionListener**: For send button and menu items
  - **KeyListener**: For detecting Enter key in the message field
  - **WindowListener**: For handling window close events and proper shutdown

- **Network Integration**:
  - **MulticastManager Instance**: Manages all network communication
  - **Message Sending Logic**: Formats and encrypts outgoing messages
  - **User List Management**: Updates the user list based on network events

- **UI Management Methods**:
  - **appendMessage()**: Adds new messages to the chat panel
  - **updateUserList()**: Refreshes the user list when users join or leave
  - **showSettingsDialog()**: Displays the settings dialog
  - **updateReceivedStatistics()**: Tracks message statistics

### 3. ChatPanel.java

A specialized panel that displays chat messages in a scrollable area with automatic scrolling.

**Key Components:**
- **JScrollPane**: Provides scrolling capability for the message area
- **BoxLayout**: Arranges messages vertically with proper spacing
- **Message Management**:
  - **addMessage()**: Creates and adds message bubbles to the panel
  - **addSystemMessage()**: Adds specially formatted system messages
  - **autoScroll()**: Automatically scrolls to show new messages

- **UI Optimization**:
  - **Double Buffering**: Prevents flickering during updates
  - **Viewport Management**: Controls the visible portion of the chat
  - **Message Pruning**: Limits the number of displayed messages to prevent memory issues

### 4. ChatBubbleRenderer.java

A custom component that renders individual chat messages as stylized bubbles.

**Key Components:**
- **Rendering Engine**:
  - **paintComponent()**: Custom painting method for drawing bubbles
  - **Graphics2D Enhancements**: Uses advanced rendering hints for smooth text and shapes
  - **Word Wrapping**: Handles long messages with proper text wrapping
  - **Timestamp Formatting**: Displays message time in a consistent format

- **Bubble Types**:
  - **User Messages**: Right-aligned with user-specific color
  - **Other User Messages**: Left-aligned with sender name and avatar
  - **System Messages**: Center-aligned with special styling

- **Special Features**:
  - **Code Block Rendering**: Special formatting for code with syntax highlighting
  - **Copy Functionality**: Context menu for copying message text
  - **Shadow Effects**: Subtle shadows for visual depth

- **Mouse Interaction**:
  - **Context Menu**: Right-click menu for message actions
  - **Copy Button**: For code blocks with visual feedback
  - **Cursor Management**: Changes cursor when over interactive elements

### 5. EncryptionUtils.java

Utility class that provides encryption and decryption services for secure message transmission.

**Key Components:**
- **Constants**:
  - **ALGORITHM**: "AES" - The encryption algorithm used
  - **TRANSFORMATION**: "AES/CBC/PKCS5Padding" - The specific mode and padding
  - **KEY**: The encryption key (in a production app, this would be securely exchanged)
  - **IV_SIZE**: 16 bytes - Size of the Initialization Vector
  - **MAX_MESSAGE_SIZE**: Maximum allowed message size to prevent issues

- **Encryption Method**:
  - **Size Validation**: Checks and truncates oversized messages
  - **IV Generation**: Creates a random Initialization Vector for each message
  - **Cipher Initialization**: Sets up the encryption cipher with proper parameters
  - **Data Combination**: Combines IV and encrypted data for transmission
  - **Base64 Encoding**: Converts binary data to text for safe transmission

- **Decryption Method**:
  - **Base64 Decoding**: Converts from text back to binary
  - **IV Extraction**: Separates the IV from the encrypted data
  - **Cipher Setup**: Initializes the decryption cipher
  - **Error Handling**: Gracefully handles decryption failures
  - **Character Encoding**: Ensures proper text encoding/decoding

- **Utility Methods**:
  - **isEncrypted()**: Detects if a message is already encrypted
  - **truncateIfNeeded()**: Safely handles oversized messages

### 6. MulticastManager.java

Manages all network communication using UDP multicast for efficient message distribution.

**Key Components:**
- **Network Configuration**:
  - **Multicast Group**: InetAddress for the multicast group (230.0.0.1)
  - **Port**: Communication port (5000)
  - **Socket Management**: Creates and configures multicast sockets
  - **Interface Selection**: Logic to find the best network interface

- **Message Handling**:
  - **sendMessage()**: Encrypts and sends messages to the group
  - **receiveMessages()**: Background thread that listens for incoming messages
  - **processMessage()**: Parses and handles different message types

- **User Management**:
  - **Heartbeat System**: Periodic messages to indicate active users
  - **User Tracking**: Maintains a list of active users with timestamps
  - **Timeout Detection**: Removes users who haven't sent heartbeats

- **Network Optimization**:
  - **Interface Detection**: Finds the best network interface for multicast
  - **Error Recovery**: Handles network errors gracefully
  - **Bandwidth Management**: Optimizes message size and frequency

- **Shutdown Handling**:
  - **gracefulDisconnect()**: Sends goodbye message and closes resources
  - **Resource Cleanup**: Properly closes sockets and threads

### 7. UserListRenderer.java

Custom renderer for the user list that displays users with avatars and status indicators.

**Key Components:**
- **List Cell Renderer**:
  - **getListCellRendererComponent()**: Creates the visual representation of each user
  - **User Status Indicators**: Shows online/offline status with colors
  - **Avatar Generation**: Creates unique avatars based on usernames
  - **Current User Highlighting**: Visually distinguishes the current user

- **Visual Elements**:
  - **Avatar Panel**: Circular avatar with user initials
  - **Username Label**: Displays the username with appropriate styling
  - **Status Indicator**: Small colored dot showing user status
  - **Selection Highlighting**: Visual feedback for selected users

- **Customization**:
  - **Theme Integration**: Uses colors from the current theme
  - **Font Management**: Consistent typography with the rest of the UI
  - **Layout Control**: Proper spacing and alignment of elements

### 8. ThemeManager.java

Manages the visual appearance of the application with support for different themes.

**Key Components:**
- **Color Schemes**:
  - **ColorScheme Class**: Defines a complete set of colors for the UI
  - **Light Theme**: Bright background with dark text
  - **Dark Theme**: Dark background with light text
  - **Custom Themes**: Support for user-defined themes

- **Theme Application**:
  - **applyTheme()**: Updates all UI components with the new theme
  - **Component Traversal**: Recursively updates nested components
  - **Look and Feel Integration**: Works with Java's Look and Feel system

- **Theme Persistence**:
  - **saveThemePreference()**: Stores the user's theme choice
  - **loadThemePreference()**: Restores the previously selected theme

- **Utility Methods**:
  - **getCurrentTheme()**: Returns the active color scheme
  - **isDarkTheme()**: Checks if the dark theme is active
  - **getContrastColor()**: Calculates appropriate contrast colors

### 9. Utils.java

A collection of utility methods used throughout the application.

**Key Components:**
- **UI Utilities**:
  - **centerOnScreen()**: Centers windows on the display
  - **createImageIcon()**: Loads and scales image resources
  - **setUIFont()**: Changes the application font globally

- **String Utilities**:
  - **formatMessage()**: Formats messages with username and content
  - **extractUsername()**: Parses username from message strings
  - **sanitizeInput()**: Removes potentially problematic characters

- **System Utilities**:
  - **getOperatingSystem()**: Detects the current OS for platform-specific behavior
  - **getJavaVersion()**: Gets the current Java version
  - **getApplicationVersion()**: Returns the application version

- **Network Utilities**:
  - **getLocalHostname()**: Gets the local machine's hostname
  - **getLocalIPAddress()**: Gets the local IP address
  - **isNetworkAvailable()**: Checks if network connectivity is available

## Implementation Details

### ChatWindow.java - Core Implementation

The ChatWindow class serves as the central hub of the application, coordinating between the UI and network components:

```java
public class ChatWindow extends JFrame {
    private final ChatPanel chatPanel;
    private final JList<String> userList;
    private final DefaultListModel<String> userListModel;
    private final JTextField messageField;
    private final MulticastManager multicastManager;
    private final String username;
    private final StatusBar statusBar;
    
    // Message statistics
    private long messagesSent = 0;
    private long messagesReceived = 0;
    private long bytesSent = 0;
    private long bytesReceived = 0;
    
    // Constructor initializes UI and network components
    public ChatWindow(String username) {
        this.username = username;
        
        // Set up the window
        setTitle("Secure Local Chat - " + username);
        setSize(800, 600);
        setMinimumSize(new Dimension(500, 400));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Utils.centerOnScreen(this);
        
        // Initialize components
        chatPanel = new ChatPanel();
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        messageField = new JTextField();
        statusBar = new StatusBar();
        
        // Set up the network manager
        multicastManager = new MulticastManager(this, username);
        
        // Add components to the layout
        // ...
        
        // Add event listeners
        // ...
        
        // Start network communication
        startNetworking();
    }
    
    // Starts the network communication threads
    private void startNetworking() {
        // Start a background thread for receiving messages
        new Thread(() -> multicastManager.receiveMessages()).start();
        
        // Start a background thread for sending heartbeat messages
        new Thread(() -> {
            while (true) {
                try {
                    multicastManager.sendMessage("HEARTBEAT:" + username);
                    Thread.sleep(5000); // Send heartbeat every 5 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        // Add a system message
        SwingUtilities.invokeLater(() -> 
            chatPanel.addSystemMessage("Connected to chat as " + username, new Date()));
    }
    
    // Adds a message to the chat panel
    public void appendMessage(String message) {
        // Extract the username and content
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            String sender = message.substring(0, colonIndex).trim();
            String content = message.substring(colonIndex + 1).trim();
            
            // Add the message to the chat panel
            SwingUtilities.invokeLater(() -> 
                chatPanel.addMessage(sender, content, new Date(), sender.equals(username)));
        }
    }
    
    // Updates the user list when users join or leave
    public void updateUserList(String username, boolean isOnline) {
        SwingUtilities.invokeLater(() -> {
            if (isOnline && !userListModel.contains(username)) {
                userListModel.addElement(username);
                chatPanel.addSystemMessage(username + " joined the chat", new Date());
            } else if (!isOnline && userListModel.contains(username)) {
                userListModel.removeElement(username);
                chatPanel.addSystemMessage(username + " left the chat", new Date());
            }
        });
    }
    
    // Sends a message when the user clicks the send button or presses Enter
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Format and send the message
                String formattedMessage = username + ": " + message;
                multicastManager.sendMessage(formattedMessage);
                
                // Clear the input field
                messageField.setText("");
                
                // Update statistics
                messagesSent++;
                bytesSent += formattedMessage.length();
                updateStatusBar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error sending message: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Updates the status bar with current statistics
    private void updateStatusBar() {
        statusBar.updateStats(messagesSent, messagesReceived, bytesSent, bytesReceived);
    }
    
    // Updates statistics when a message is received
    public void updateReceivedStatistics(int bytes) {
        messagesReceived++;
        bytesReceived += bytes;
        updateStatusBar();
    }
    
    // Handles window closing with proper cleanup
    private void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to exit?", 
            "Confirm Exit", JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            // Send a goodbye message
            try {
                multicastManager.sendMessage("GOODBYE:" + username);
                multicastManager.gracefulDisconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Exit the application
            dispose();
            System.exit(0);
        }
    }
}
```

### MulticastManager.java - Network Implementation

The MulticastManager handles all network communication using UDP multicast:

```java
public class MulticastManager {
    private static final String MULTICAST_ADDRESS = "230.0.0.1";
    private static final int PORT = 5000;
    
    private final InetAddress group;
    private final ChatWindow chatWindow;
    private final String username;
    private MulticastSocket socket;
    
    // Constructor initializes the network components
    public MulticastManager(ChatWindow chatWindow, String username) {
        this.chatWindow = chatWindow;
        this.username = username;
        
        try {
            // Initialize the multicast group address
            group = InetAddress.getByName(MULTICAST_ADDRESS);
            
            // Validate that it's a multicast address
            if (!group.isMulticastAddress()) {
                throw new IOException(MULTICAST_ADDRESS + " is not a multicast address");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize multicast: " + e.getMessage(), e);
        }
    }
    
    // Finds the best network interface for multicast
    private NetworkInterface findMulticastInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                
                // Skip interfaces that are down, loopback, or don't support multicast
                if (!nif.isUp() || nif.isLoopback() || !nif.supportsMulticast()) {
                    continue;
                }
                
                // Check if this interface has a usable IPv4 address
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return nif;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Sends a message to the multicast group
    public void sendMessage(String message) throws Exception {
        // Create a new socket for sending
        try (DatagramSocket socket = new DatagramSocket()) {
            // Encrypt the message
            String encrypted = EncryptionUtils.encrypt(message);
            byte[] buffer = encrypted.getBytes();
            
            // Create and send the packet
            DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, group, PORT);
            socket.send(packet);
        }
    }
    
    // Receives messages in a background thread
    public void receiveMessages() {
        try {
            // Create a multicast socket
            socket = new MulticastSocket(PORT);
            socket.setReuseAddress(true);
            
            // Join the multicast group on the best interface
            NetworkInterface nif = findMulticastInterface();
            if (nif != null) {
                socket.joinGroup(new InetSocketAddress(group, PORT), nif);
            } else {
                // Fall back to default interface if no specific one works
                socket.joinGroup(group);
            }
            
            // Announce presence
            sendMessage("joined");
            
            // Receive messages in a loop
            byte[] buffer = new byte[65536];
            while (true) {
                try {
                    // Receive a packet
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    
                    // Update statistics
                    chatWindow.updateReceivedStatistics(packet.getLength());
                    
                    // Decrypt and process the message
                    String encrypted = new String(packet.getData(), 0, packet.getLength());
                    String plaintext = EncryptionUtils.decrypt(encrypted);
                    
                    // Process different message types
                    if (plaintext.startsWith("HEARTBEAT:")) {
                        // Extract username and update user list
                        String heartbeatUser = plaintext.substring(10);
                        chatWindow.updateUserList(heartbeatUser, true);
                    } else if (plaintext.startsWith("GOODBYE:")) {
                        // Extract username and update user list
                        String goodbyeUser = plaintext.substring(8);
                        chatWindow.updateUserList(goodbyeUser, false);
                    } else {
                        // Regular message - display it
                        chatWindow.appendMessage(plaintext);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Cleans up resources when shutting down
    public void gracefulDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.leaveGroup(group);
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### EncryptionUtils.java - Security Implementation

The EncryptionUtils class provides encryption and decryption services:

```java
public class EncryptionUtils {
    // Encryption constants
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String KEY = "LC32SecureChat!!"; // 16 bytes for AES-128
    private static final int IV_SIZE = 16; // 16 bytes for AES
    private static final int MAX_MESSAGE_SIZE = 10000; // Prevent very large messages
    
    // Encrypts a message using AES-128 in CBC mode
    public static String encrypt(String message) throws Exception {
        // Check message size to prevent issues
        if (message.length() > MAX_MESSAGE_SIZE) {
            message = message.substring(0, MAX_MESSAGE_SIZE) + 
                "... [Message truncated due to size]";
        }
        
        // Generate a random IV for each encryption
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Create key spec
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        
        // Initialize cipher with IV
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        
        // Encrypt the message
        byte[] encrypted = cipher.doFinal(message.getBytes());
        
        // Combine IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);
        
        // Encode as Base64 for transmission
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
    
    // Decrypts a message using AES-128 in CBC mode
    public static String decrypt(String encryptedMessage) throws Exception {
        try {
            // Decode from Base64
            byte[] encryptedData = Base64.getDecoder().decode(encryptedMessage);
            
            // Extract IV from the beginning of the data
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);
            
            // Extract the encrypted part
            byte[] encrypted = new byte[encryptedData.length - IV_SIZE];
            byteBuffer.get(encrypted);
            
            // Create key and IV specs
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // Decrypt the data
            byte[] decrypted = cipher.doFinal(encrypted);
            
            // Convert to string and return
            return new String(decrypted);
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }
    
    // Checks if a message is already encrypted
    public static boolean isEncrypted(String message) {
        // Simple heuristic: encrypted messages are Base64 encoded
        try {
            Base64.getDecoder().decode(message);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
```

### ChatBubbleRenderer.java - UI Implementation

The ChatBubbleRenderer creates visually appealing message bubbles:

```java
public class ChatBubbleRenderer extends JPanel {
    private static final int BUBBLE_RADIUS = 12;
    private static final int BUBBLE_SPACING = 5;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    
    private final String sender;
    private final String message;
    private final Date timestamp;
    private final boolean isCurrentUser;
    private final boolean isSystemMessage;
    private final Color bubbleColor;
    private final Color textColor;
    
    // For code block copy button
    private Rectangle codeBlockCopyButton;
    private String codeToClipboard;
    
    // Constructor for user messages
    public ChatBubbleRenderer(String sender, String message, Date timestamp, boolean isCurrentUser) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.isCurrentUser = isCurrentUser;
        this.isSystemMessage = false;
        
        // Get colors from the current theme
        ThemeManager.ColorScheme theme = ThemeManager.getCurrentTheme();
        this.bubbleColor = isCurrentUser ? theme.myMessageColor : theme.otherMessageColor;
        this.textColor = theme.textPrimaryColor;
        
        setupPanel();
        addCopyContextMenu();
    }
    
    // Constructor for system messages
    public ChatBubbleRenderer(String message, Date timestamp) {
        this.sender = "System";
        this.message = message;
        this.timestamp = timestamp;
        this.isCurrentUser = false;
        this.isSystemMessage = true;
        
        // Get colors from the current theme
        ThemeManager.ColorScheme theme = ThemeManager.getCurrentTheme();
        this.bubbleColor = theme.systemMessageColor;
        this.textColor = theme.textPrimaryColor;
        
        setupPanel();
        addCopyContextMenu();
    }
    
    // Sets up the panel and mouse listeners
    private void setupPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        // Use different spacing for system messages
        if (isSystemMessage) {
            setBorder(new EmptyBorder(2, BUBBLE_SPACING, 2, BUBBLE_SPACING));
        } else {
            setBorder(new EmptyBorder(BUBBLE_SPACING, BUBBLE_SPACING, BUBBLE_SPACING, BUBBLE_SPACING));
        }
        
        // Add mouse listeners for code block copy button
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (codeBlockCopyButton != null && codeBlockCopyButton.contains(e.getPoint())) {
                    if (codeToClipboard != null && !codeToClipboard.isEmpty()) {
                        copyMessageToClipboard(codeToClipboard);
                    }
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (codeBlockCopyButton != null) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        // Add mouse motion listener for cursor changes
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (codeBlockCopyButton != null && codeBlockCopyButton.contains(e.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
    
    // Custom painting for the chat bubble
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int bubbleWidth = width - 2 * BUBBLE_SPACING;
        int bubbleHeight = height - 2 * BUBBLE_SPACING;
        
        // Calculate bubble position based on message type
        int x = BUBBLE_SPACING;
        if (isCurrentUser && !isSystemMessage) {
            x = width - bubbleWidth - BUBBLE_SPACING;
        } else if (isSystemMessage) {
            x = (width - bubbleWidth) / 2;
        }
        
        // Draw bubble shadow
        Color shadowColor = new Color(0, 0, 0, 30);
        g2d.setColor(shadowColor);
        RoundRectangle2D shadow = new RoundRectangle2D.Float(
                x + 2, BUBBLE_SPACING + 2, bubbleWidth, bubbleHeight, BUBBLE_RADIUS, BUBBLE_RADIUS);
        g2d.fill(shadow);
        
        // Draw bubble background
        g2d.setColor(bubbleColor);
        RoundRectangle2D bubble = new RoundRectangle2D.Float(
                x, BUBBLE_SPACING, bubbleWidth, bubbleHeight, BUBBLE_RADIUS, BUBBLE_RADIUS);
        g2d.fill(bubble);
        
        // Draw sender name for others' messages
        if (!isCurrentUser && !isSystemMessage) {
            g2d.setColor(ThemeManager.getCurrentTheme().primaryColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString(sender, x + 10, BUBBLE_SPACING + 20);
        }
        
        // Draw message text
        g2d.setColor(textColor);
        Font messageFont = new Font("Segoe UI", 
                               isSystemMessage ? Font.ITALIC : Font.PLAIN, 
                               isSystemMessage ? 12 : 14);
        g2d.setFont(messageFont);
        
        // Calculate text position
        int textY = BUBBLE_SPACING + 40;
        if (isCurrentUser) {
            textY = BUBBLE_SPACING + 25;
        } else if (isSystemMessage) {
            textY = BUBBLE_SPACING + 18;
        }
        
        // Draw the message text with word wrap
        // ... (word wrapping code omitted for brevity)
        
        // Draw timestamp
        String time = TIME_FORMAT.format(timestamp);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.setColor(new Color(150, 150, 150));
        
        int timeWidth = g2d.getFontMetrics().stringWidth(time);
        int timeX = isCurrentUser ? x + bubbleWidth - timeWidth - 10 : x + 10;
        if (isSystemMessage) {
            timeX = x + (bubbleWidth - timeWidth) / 2;
        }
        
        g2d.drawString(time, timeX, height - 10);
        
        g2d.dispose();
    }
}
```

### ThemeManager.java - Theme Implementation

The ThemeManager provides theme support with light and dark modes:

```java
public class ThemeManager {
    // Available themes
    private static final String LIGHT_THEME = "Light";
    private static final String DARK_THEME = "Dark";
    
    // Current theme
    private static String currentTheme = LIGHT_THEME;
    
    // Color scheme class to hold all theme colors
    public static class ColorScheme {
        public Color backgroundColor;
        public Color textPrimaryColor;
        public Color textSecondaryColor;
        public Color primaryColor;
        public Color secondaryColor;
        public Color accentColor;
        public Color myMessageColor;
        public Color otherMessageColor;
        public Color systemMessageColor;
        public Color inputBackgroundColor;
        public Color borderColor;
        
        // Constructor initializes all colors
        public ColorScheme(boolean isDark) {
            if (isDark) {
                // Dark theme colors
                backgroundColor = new Color(33, 33, 33);
                textPrimaryColor = new Color(255, 255, 255);
                textSecondaryColor = new Color(200, 200, 200);
                primaryColor = new Color(66, 133, 244);
                secondaryColor = new Color(50, 50, 50);
                accentColor = new Color(255, 171, 64);
                myMessageColor = new Color(55, 71, 79);
                otherMessageColor = new Color(38, 50, 56);
                systemMessageColor = new Color(66, 66, 66);
                inputBackgroundColor = new Color(45, 45, 45);
                borderColor = new Color(70, 70, 70);
            } else {
                // Light theme colors
                backgroundColor = new Color(245, 245, 245);
                textPrimaryColor = new Color(33, 33, 33);
                textSecondaryColor = new Color(100, 100, 100);
                primaryColor = new Color(25, 118, 210);
                secondaryColor = new Color(225, 225, 225);
                accentColor = new Color(255, 152, 0);
                myMessageColor = new Color(225, 241, 255);
                otherMessageColor = new Color(255, 255, 255);
                systemMessageColor = new Color(240, 240, 240);
                inputBackgroundColor = new Color(255, 255, 255);
                borderColor = new Color(200, 200, 200);
            }
        }
    }
    
    // Get the current theme's color scheme
    public static ColorScheme getCurrentTheme() {
        return new ColorScheme(isDarkTheme());
    }
    
    // Check if dark theme is active
    public static boolean isDarkTheme() {
        return currentTheme.equals(DARK_THEME);
    }
    
    // Switch to a different theme
    public static void setTheme(String theme) {
        if (theme.equals(LIGHT_THEME) || theme.equals(DARK_THEME)) {
            currentTheme = theme;
            saveThemePreference(theme);
        }
    }
    
    // Apply the current theme to a component and all its children
    public static void applyTheme(Component component) {
        ColorScheme colors = getCurrentTheme();
        
        if (component instanceof JPanel) {
            component.setBackground(colors.backgroundColor);
        } else if (component instanceof JTextField) {
            component.setBackground(colors.inputBackgroundColor);
            component.setForeground(colors.textPrimaryColor);
            ((JTextField) component).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colors.borderColor),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        } else if (component instanceof JButton) {
            component.setBackground(colors.primaryColor);
            component.setForeground(Color.WHITE);
        } else if (component instanceof JList) {
            component.setBackground(colors.backgroundColor);
            component.setForeground(colors.textPrimaryColor);
        } else if (component instanceof JScrollPane) {
            component.setBackground(colors.backgroundColor);
            ((JScrollPane) component).getViewport().setBackground(colors.backgroundColor);
        }
        
        // Recursively apply theme to child components
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyTheme(child);
            }
        }
    }
    
    // Save theme preference to user settings
    private static void saveThemePreference(String theme) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            prefs.put("theme", theme);
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Load saved theme preference
    public static void loadThemePreference() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
            String savedTheme = prefs.get("theme", LIGHT_THEME);
            currentTheme = savedTheme;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Network Protocol

The application uses a simple protocol for different message types:

1. **Regular Messages**: `username: message content`
2. **Heartbeat Messages**: `HEARTBEAT:username`
3. **Goodbye Messages**: `GOODBYE:username`

All messages are encrypted before transmission and decrypted upon receipt.

## Security Considerations

1. **Encryption Strength**: AES-128 provides strong security for casual use, though AES-256 could be implemented for higher security requirements.

2. **Key Management**: The encryption key is currently hardcoded. In a production environment, a secure key exchange mechanism would be preferable.

3. **Network Scope**: The application is designed for local network use only. The multicast protocol does not traverse routers, limiting the communication to a single subnet.

4. **Message Integrity**: The encryption provides confidentiality but does not include message authentication. Adding HMAC would improve security against tampering.

## Performance Optimizations

1. **Buffer Sizing**: Large buffers (65KB) are used to handle messages of any reasonable size.

2. **UI Responsiveness**: All UI updates occur on the Event Dispatch Thread using `SwingUtilities.invokeLater()`.

3. **Message Truncation**: Very large messages are automatically truncated to prevent performance issues.

4. **Network Interface Selection**: The application automatically selects the most appropriate network interface for multicast communication.

5. **Rendering Optimizations**: High-quality rendering hints are used for text and graphics, with word wrapping to handle messages of any length.

## Conclusion

This secure local chat application demonstrates effective implementation of:
- Encrypted communication using AES-128/CBC
- Efficient message distribution using UDP multicast
- Modern UI design with chat bubbles and user presence indicators
- Robust error handling and network interface management

The modular code structure separates concerns between UI, networking, and encryption, making the application maintainable and extensible.