import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Modern Swing-based chat window with enhanced UI.
 * Sends via MulticastManager, displays decrypted messages.
 */
public class ChatWindow {
    private JTextComponent messageField; // Changed to JTextComponent to support both JTextField and JTextArea
    private JButton sendButton;
    private final MulticastManager multicastManager;
    private final String nickname;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private final Set<String> activeUsers = new HashSet<>();
    private ChatPanel chatPanel;
    private JFrame frame;

    private JButton themeButton;
    private JButton clearButton;
    private JPanel statusBar;
    private JLabel networkStatusLabel; // Added for network status display
    private JLabel statisticsLabel; // For displaying technical statistics
    private int messagesSent = 0;
    private int messagesReceived = 0;
    private long bytesTransferred = 0;

    public ChatWindow(String nickname) {
        this.nickname = nickname;
        multicastManager = new MulticastManager(nickname, this);

        // Apply theme
        ThemeManager.applyTheme();

        // Build UI
        frame = new JFrame();
        frame.setTitle("LAN Chat – " + nickname);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setMinimumSize(new Dimension(700, 500));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPanel.setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Main split pane with improved responsiveness
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // Use proportional divider location instead of fixed pixel value
        splitPane.setResizeWeight(0.8); // Chat area gets 80% of space when resizing
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        
        // Chat panel (left side) with improved responsiveness
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        leftPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        leftPanel.setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Set minimum and preferred size for better responsiveness
        leftPanel.setMinimumSize(new Dimension(400, 300));
        leftPanel.setPreferredSize(new Dimension(650, 500));
        
        // Create a technical-looking header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getCurrentTheme().dividerColor),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Create a technical-looking title
        JLabel titleLabel = new JLabel("LAN CHAT SECURE COMMUNICATIONS");
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        titleLabel.setForeground(ThemeManager.getCurrentTheme().primaryColor);
        
        // Add a technical-looking status indicator
        JPanel statusIndicator = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusIndicator.setOpaque(false);
        
        // Add a blinking "ACTIVE" indicator
        JLabel activeLabel = new JLabel("■ ACTIVE");
        activeLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        activeLabel.setForeground(ThemeManager.getCurrentTheme().secondaryColor);
        
        // Make the indicator blink
        javax.swing.Timer blinkTimer = new javax.swing.Timer(800, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeLabel.getForeground().equals(ThemeManager.getCurrentTheme().secondaryColor)) {
                    activeLabel.setForeground(ThemeManager.getCurrentTheme().backgroundColor);
                } else {
                    activeLabel.setForeground(ThemeManager.getCurrentTheme().secondaryColor);
                }
            }
        });
        blinkTimer.start();
        
        statusIndicator.add(activeLabel);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusIndicator, BorderLayout.EAST);
        
        // Create chat panel
        chatPanel = new ChatPanel(nickname);
        
        // Toolbar with buttons
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        toolbarPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // No theme toggle button as we only use dark mode
        themeButton = new JButton("Dark Mode");
        themeButton.setVisible(false); // Hide the button
        
        // No clear button
        clearButton = new JButton("Clear");
        clearButton.setVisible(false); // Hide the button
        
        // Don't add any buttons to toolbar as they're all hidden
        // toolbarPanel.add(themeButton);
        // toolbarPanel.add(clearButton);
        
        // Message input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Create multi-line message field with responsive sizing
        JTextArea textArea = new JTextArea(3, 20);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getCurrentTheme().dividerColor),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textArea.setBackground(ThemeManager.getCurrentTheme().cardColor);
        textArea.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        // Add key bindings for the text area
        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textArea.getActionMap();
        
        // Enter to send, Shift+Enter for new line
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "insert-break");
        
        actionMap.put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt = textArea.getText().trim();
                if (!txt.isEmpty()) {
                    // Check if it's a technical command
                    if (txt.startsWith("/")) {
                        processCommand(txt);
                    } else {
                        multicastManager.sendMessage(txt);
                    }
                    textArea.setText("");
                }
                textArea.requestFocus();
            }
        });
        
        // Wrap in scroll pane with hidden scrollbars
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(200, 60));
        scrollPane.setMinimumSize(new Dimension(100, 60));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Hide scrollbars but keep scrolling functionality
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Use the text area as our message field
        messageField = textArea;
        
        // Add a technical-looking tooltip
        messageField.setToolTipText("<html><body style='width: 300px'>" +
            "<div style='font-family: monospace; font-size: 11px;'>" +
            "<b>SECURE MESSAGE INPUT</b><br>" +
            "Type your message or use commands:<br>" +
            "<span style='color: #3399ff;'>/help</span> - Show available commands<br>" +
            "<span style='color: #3399ff;'>/tech</span> - Toggle technical info<br>" +
            "<span style='color: #3399ff;'>/stats</span> - Show network statistics<br><br>" +
            "<b>CODE BLOCKS:</b><br>" +
            "Format code with:<br>" +
            "<span style='color: #3399ff;'>```language<br>code here<br>```</span><br>" +
            "Example languages: js, java, python, html<br><br>" +
            "Messages are encrypted with AES-128/CBC" +
            "</div></body></html>");
        
        sendButton = ThemeManager.createThemedButton("Send", true);
        sendButton.setPreferredSize(new Dimension(80, 36));
        
        // Action listener for send button
        ActionListener sendAction = e -> {
            String txt = messageField.getText().trim();
            if (!txt.isEmpty()) {
                // Check if it's a technical command
                if (txt.startsWith("/")) {
                    processCommand(txt);
                } else {
                    multicastManager.sendMessage(txt);
                }
                messageField.setText("");
            }
            messageField.requestFocus();
        };
        
        // We don't need to add action listener to JTextArea as we already set up key bindings
        // for Ctrl+Enter in the text area creation code
        sendButton.addActionListener(sendAction);
        
        // Create a panel for the message input with fixed layout
        JPanel messageInputPanel = new JPanel(new BorderLayout(5, 0));
        messageInputPanel.setOpaque(false);
        
        // Add the scroll pane containing the text area
        messageInputPanel.add(scrollPane, BorderLayout.CENTER);
        
        inputPanel.add(messageInputPanel, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        // Enhanced status bar with network details
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getCurrentTheme().dividerColor),
            new EmptyBorder(5, 10, 5, 10)
        ));
        statusBar.setBackground(new Color(33, 33, 33)); // Slightly darker than background
        
        // Create a label that will be updated with network details
        JLabel statusLabel = new JLabel("Initializing network...");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 11)); // Monospaced font for technical look
        statusLabel.setForeground(new Color(0, 255, 0)); // Green text like a terminal
        
        // Store the label as a field so it can be updated later
        this.networkStatusLabel = statusLabel;
        
        // Add a small icon to make it look more technical
        JPanel statusIconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusIconPanel.setOpaque(false);
        
        // Create a small colored dot to indicate connection status
        JPanel statusDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 255, 0)); // Green dot
                g2d.fillOval(0, 0, 8, 8);
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(8, 8);
            }
        };
        
        statusIconPanel.add(statusDot);
        statusIconPanel.add(statusLabel);
        
        statusBar.add(statusIconPanel, BorderLayout.WEST);
        
        // Add statistics panel to the right side of the status bar
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statsPanel.setOpaque(false);
        
        // Create statistics label with technical information
        statisticsLabel = new JLabel("[STATS] Msgs: 0 | Bytes: 0");
        statisticsLabel.setFont(new Font("Consolas", Font.PLAIN, 11)); // Monospaced font for technical look
        statisticsLabel.setForeground(new Color(180, 180, 180)); // Light gray text
        
        statsPanel.add(statisticsLabel);
        statusBar.add(statsPanel, BorderLayout.EAST);
        
        // No top panel needed since we removed the header
        // Keep toolbar for potential future use
        toolbarPanel.setOpaque(false);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        
        // Add the header panel to the top
        leftPanel.add(headerPanel, BorderLayout.NORTH);
        leftPanel.add(chatPanel.getScrollPane(), BorderLayout.CENTER);
        leftPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // User list panel (right side) with improved responsiveness
        JPanel userPanel = ThemeManager.createCardPanel();
        userPanel.setLayout(new BorderLayout(0, 0));
        userPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Set minimum and preferred size for better responsiveness
        userPanel.setMinimumSize(new Dimension(150, 300));
        userPanel.setPreferredSize(new Dimension(200, 400));
        
        // User list header
        JPanel usersHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        usersHeaderPanel.setOpaque(false);
        
        JLabel usersHeaderLabel = new JLabel("Online Users");
        usersHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        usersHeaderLabel.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        usersHeaderPanel.add(usersHeaderLabel);
        
        // User count label
        JLabel userCountLabel = new JLabel("1 user online");
        userCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userCountLabel.setForeground(ThemeManager.getCurrentTheme().textSecondaryColor);
        userCountLabel.setBorder(new EmptyBorder(5, 5, 15, 5));
        
        // User list
        userListModel = new DefaultListModel<>();
        userListModel.addElement(nickname);
        activeUsers.add(nickname);
        
        userList = new JList<>(userListModel);
        userList.setCellRenderer(new UserListRenderer(nickname));
        userList.setBackground(ThemeManager.getCurrentTheme().cardColor);
        userList.setBorder(null);
        
        // Create scroll pane without visible scrollbars
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setBorder(null);
        
        // Hide scrollbars but still allow scrolling with mouse wheel
        userScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        userScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Increase scroll speed for better UX without visible scrollbar
        userScrollPane.getVerticalScrollBar().setUnitIncrement(25);
        
        // Add mouse wheel listener for scrolling without visible scrollbar
        userScrollPane.addMouseWheelListener(e -> {
            JScrollBar verticalBar = userScrollPane.getVerticalScrollBar();
            int direction = e.getWheelRotation();
            int increment = verticalBar.getUnitIncrement() * 3; // Faster scrolling
            
            // Scroll up or down based on wheel direction
            if (direction < 0) {
                // Scroll up
                verticalBar.setValue(verticalBar.getValue() - increment);
            } else {
                // Scroll down
                verticalBar.setValue(verticalBar.getValue() + increment);
            }
        });
        userScrollPane.setBackground(ThemeManager.getCurrentTheme().cardColor);
        
        // Add components to user panel
        JPanel userHeaderPanel = new JPanel(new BorderLayout());
        userHeaderPanel.setOpaque(false);
        userHeaderPanel.add(usersHeaderPanel, BorderLayout.NORTH);
        userHeaderPanel.add(userCountLabel, BorderLayout.CENTER);
        
        userPanel.add(userHeaderPanel, BorderLayout.NORTH);
        userPanel.add(userScrollPane, BorderLayout.CENTER);
        
        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(userPanel);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // Add content to frame
        frame.setLayout(new BorderLayout());
        frame.add(contentPanel, BorderLayout.CENTER);
        
        frame.setLocationRelativeTo(null);
        
        // Add window resize listener to handle responsive layout
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Ensure the chat panel is properly resized
                chatPanel.revalidate();
                chatPanel.repaint();
                
                // Ensure scroll pane shows the latest messages
                chatPanel.scrollToBottom();
            }
        });
        
        // Add window state listener to handle maximizing
        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if ((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                    // Window was maximized
                    // Reset the split pane divider to maintain proper proportions
                    SwingUtilities.invokeLater(() -> {
                        // Find the split pane and reset its divider location
                        for (Component comp : frame.getContentPane().getComponents()) {
                            findAndResetSplitPane(comp);
                        }
                        
                        // Ensure chat panel is properly resized
                        chatPanel.revalidate();
                        chatPanel.repaint();
                        chatPanel.scrollToBottom();
                        
                        // Force a complete repaint
                        frame.revalidate();
                        frame.repaint();
                    });
                }
            }
        });
        
        frame.setVisible(true);
        
        // No welcome message in chat area
        
        // Start background receive
        new Thread(multicastManager::receiveMessages, "Receiver-Thread").start();
        
        // Focus on message field
        messageField.requestFocus();
        
        // Start heartbeat timer to periodically announce presence
        javax.swing.Timer heartbeatTimer = new javax.swing.Timer(10000, e -> multicastManager.sendHeartbeat());
        heartbeatTimer.setRepeats(true);
        heartbeatTimer.start();
        
        // Add shutdown hook to send goodbye message when app closes
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            multicastManager.sendGoodbye();
            try {
                // Give some time for the message to be sent
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }));
        
        // Send initial heartbeat
        multicastManager.sendHeartbeat();
    }
    

    
    /**
     * Toggle theme method is no longer used as we only have dark mode
     * Kept as a stub for compatibility
     */
    private void toggleTheme() {
        // Do nothing - we only use dark mode
    }
    
    /**
     * Apply theme to all components without recreating the UI
     */
    private void applyThemeToAllComponents() {
        // Apply theme to the frame and all its components
        SwingUtilities.updateComponentTreeUI(frame);
        
        // Update specific components that need special handling
        chatPanel.applyTheme();
        
        // Update colors for various panels
        updateComponentColors(frame);
        
        // Ensure the UI is properly refreshed
        frame.revalidate();
        frame.repaint();
    }
    
    /**
     * Recursively update colors for all components
     */
    private void updateComponentColors(Component component) {
        ThemeManager.ColorScheme theme = ThemeManager.getCurrentTheme();
        
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            if (panel == statusBar) {
                panel.setBackground(theme.cardColor);
            } else {
                panel.setBackground(theme.backgroundColor);
            }
        } else if (component instanceof JTextField) {
            JTextField textField = (JTextField) component;
            textField.setBackground(theme.cardColor);
            textField.setForeground(theme.textPrimaryColor);
            textField.setCaretColor(theme.primaryColor);
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button == sendButton) {
                button.setBackground(theme.primaryColor);
                button.setForeground(Color.WHITE);
            }
        } else if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            list.setBackground(theme.cardColor);
            list.setForeground(theme.textPrimaryColor);
        }
        
        // Recursively update child components
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                updateComponentColors(child);
            }
        }
    }

    /**
     * Appends a message to the chat panel.
     */
    public void appendMessage(String message) {
        try {
            // Check if message contains a colon
            int colonIndex = message.indexOf(":");
            if (colonIndex <= 0) {
                // Malformed message, show as system message
                appendSystemMessage("Received: " + message);
                return;
            }
            
            String sender = message.substring(0, colonIndex).trim();
            String content = message.substring(colonIndex + 1).trim();
            
            // Skip empty messages
            if (content.isEmpty()) {
                return;
            }
            
            // Add user to the list if not already there
            if (!activeUsers.contains(sender)) {
                activeUsers.add(sender);
                SwingUtilities.invokeLater(() -> userListModel.addElement(sender));
            }
            
            // Generate technical packet information for display
            boolean isFromCurrentUser = sender.equals(nickname);
            String packetInfo = generatePacketInfo(message, isFromCurrentUser);
            
            // Add to chat panel with technical packet info if enabled
            if (isShowingTechnicalInfo) {
                chatPanel.addMessage(sender, packetInfo + "\n" + content);
            } else {
                chatPanel.addMessage(sender, content);
            }
            
            // Print debug info to console
            System.out.println("Message from: " + sender + ", content: " + content);
        } catch (Exception e) {
            // Fallback for malformed messages
            System.err.println("Error parsing message: " + e.getMessage());
            appendSystemMessage("Received: " + message);
        }
    }
    
    // Flag to toggle technical information display
    private boolean isShowingTechnicalInfo = false;
    
    /**
     * Toggle the display of technical packet information
     */
    public void toggleTechnicalInfo() {
        isShowingTechnicalInfo = !isShowingTechnicalInfo;
        appendSystemMessage("Technical packet information display " + 
                           (isShowingTechnicalInfo ? "enabled" : "disabled"));
    }
    
    /**
     * Generate technical packet information for a message
     */
    private String generatePacketInfo(String message, boolean isOutgoing) {
        StringBuilder info = new StringBuilder();
        info.append("+-- PACKET INFO ");
        for (int i = 0; i < 40; i++) info.append("-");
        info.append("+\n");
        
        // Calculate message size
        int messageSize = message.getBytes().length;
        
        // Generate a mock packet ID
        String packetId = String.format("%08X", message.hashCode());
        
        // Calculate a mock checksum
        int checksum = 0;
        for (byte b : message.getBytes()) {
            checksum = (checksum + (b & 0xFF)) % 0xFFFF;
        }
        
        // Add packet details
        info.append("| Direction: ").append(isOutgoing ? "OUTGOING" : "INCOMING").append("\n");
        info.append("| Size: ").append(messageSize).append(" bytes\n");
        info.append("| Packet ID: ").append(packetId).append("\n");
        info.append("| Checksum: 0x").append(String.format("%04X", checksum)).append("\n");
        info.append("| Encryption: AES-128/ECB\n");
        info.append("| Timestamp: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())).append("\n");
        info.append("+");
        for (int i = 0; i < 50; i++) info.append("-");
        info.append("+");
        
        return info.toString();
    }
    
    /**
     * Appends a system message to the chat panel.
     */
    public void appendSystemMessage(String message) {
        chatPanel.addSystemMessage(message);
    }
    
    /**
     * Add a user to the online users list
     */
    public void addUserToList(String username) {
        if (!activeUsers.contains(username)) {
            activeUsers.add(username);
            SwingUtilities.invokeLater(() -> {
                userListModel.addElement(username);
                updateUserCount();
            });
        }
    }
    
    /**
     * Remove a user from the online users list
     */
    public void removeUserFromList(String username) {
        if (activeUsers.contains(username) && !username.equals(nickname)) {
            activeUsers.remove(username);
            SwingUtilities.invokeLater(() -> {
                userListModel.removeElement(username);
                updateUserCount();
                appendSystemMessage(username + " left");
            });
        }
    }
    
    /**
     * Update the user count label
     */
    private void updateUserCount() {
        int count = userListModel.getSize();
        // Find the user count label and update it
        for (Component comp : frame.getComponents()) {
            updateUserCountRecursive(comp, count);
        }
    }
    
    /**
     * Update the network status in the status bar
     */
    public void updateNetworkStatus(String networkInfo) {
        if (networkStatusLabel != null) {
            SwingUtilities.invokeLater(() -> {
                // Add a technical-looking prefix to make it look cool
                networkStatusLabel.setText("[NET] " + networkInfo);
                
                // Make the font monospaced for a more technical look
                networkStatusLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
                
                // Set text color based on content (error = red, normal = green)
                if (networkInfo.contains("ERROR") || networkInfo.contains("failed")) {
                    networkStatusLabel.setForeground(new Color(255, 50, 50)); // Red for errors
                    
                    // Find the status dot and make it red
                    Component parent = networkStatusLabel.getParent();
                    if (parent instanceof Container) {
                        for (Component c : ((Container) parent).getComponents()) {
                            if (c instanceof JPanel && c.getPreferredSize().width == 8) {
                                c.setForeground(new Color(255, 50, 50));
                                c.repaint();
                                break;
                            }
                        }
                    }
                } else {
                    networkStatusLabel.setForeground(new Color(0, 255, 0)); // Green for normal status
                }
            });
        }
    }
    
    /**
     * Recursively search for and update the user count label
     */
    private void updateUserCountRecursive(Component comp, int count) {
        if (comp instanceof JLabel) {
            JLabel label = (JLabel) comp;
            if (label.getText() != null && label.getText().contains("user") && label.getText().contains("online")) {
                label.setText(count + (count == 1 ? " user" : " users") + " online");
            }
        }
        
        if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                updateUserCountRecursive(child, count);
            }
        }
    }
    
    /**
     * Recursively find and reset the split pane divider location
     */
    private void findAndResetSplitPane(Component comp) {
        if (comp instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) comp;
            // Reset the divider location to maintain the proper proportion
            splitPane.setDividerLocation(0.8);
        }
        
        if (comp instanceof Container) {
            Container container = (Container) comp;
            for (Component child : container.getComponents()) {
                findAndResetSplitPane(child);
            }
        }
    }
    
    /**
     * Update statistics when a message is sent
     * @param messageSize The size of the message in bytes
     */
    public void updateSentStatistics(int messageSize) {
        messagesSent++;
        bytesTransferred += messageSize;
        updateStatisticsDisplay();
    }
    
    /**
     * Update statistics when a message is received
     * @param messageSize The size of the message in bytes
     */
    public void updateReceivedStatistics(int messageSize) {
        messagesReceived++;
        bytesTransferred += messageSize;
        updateStatisticsDisplay();
    }
    
    /**
     * Update the statistics display in the status bar
     */
    private void updateStatisticsDisplay() {
        if (statisticsLabel != null) {
            SwingUtilities.invokeLater(() -> {
                String bytesFormatted = formatBytes(bytesTransferred);
                statisticsLabel.setText(String.format("[STATS] Tx: %d | Rx: %d | %s", 
                    messagesSent, messagesReceived, bytesFormatted));
            });
        }
    }
    
    /**
     * Format bytes to a human-readable format (KB, MB)
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Process technical commands that start with /
     */
    private void processCommand(String command) {
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "/help":
                showCommandHelp();
                break;
            case "/stats":
                showNetworkStats();
                break;
            case "/tech":
                toggleTechnicalInfo();
                break;
            case "/ping":
                pingUser(args);
                break;
            case "/netinfo":
                showNetworkInfo();
                break;
            case "/clear":
                chatPanel.clearMessages();
                appendSystemMessage("Chat cleared");
                break;
            case "/theme":
                appendSystemMessage("Current theme: " + ThemeManager.getCurrentThemeName());
                break;
            case "/time":
                appendSystemMessage("Server time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z").format(new Date()));
                break;
            case "/code":
                showCodeExample();
                break;
            default:
                appendSystemMessage("Unknown command: " + cmd + ". Type /help for available commands.");
                break;
        }
    }
    
    /**
     * Show help for available commands
     */
    private void showCommandHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Available commands:\n");
        help.append("/help - Show this help message\n");
        help.append("/stats - Show network statistics\n");
        help.append("/tech - Toggle technical packet information\n");
        help.append("/ping [user] - Ping a user or all users\n");
        help.append("/netinfo - Show detailed network information\n");
        help.append("/clear - Clear the chat window\n");
        help.append("/theme - Show current theme\n");
        help.append("/time - Show server time\n");
        help.append("/code - Show code block example");
        
        appendSystemMessage(help.toString());
    }
    
    /**
     * Show network statistics
     */
    private void showNetworkStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Network Statistics:\n");
        stats.append("Messages sent: ").append(messagesSent).append("\n");
        stats.append("Messages received: ").append(messagesReceived).append("\n");
        stats.append("Total bytes transferred: ").append(formatBytes(bytesTransferred)).append("\n");
        stats.append("Active users: ").append(activeUsers.size()).append("\n");
        stats.append("Encryption: AES-128/ECB");
        
        appendSystemMessage(stats.toString());
    }
    
    /**
     * Ping a user or all users
     */
    private void pingUser(String username) {
        if (username.isEmpty()) {
            appendSystemMessage("Pinging all users...");
            multicastManager.sendMessage("/ping all");
        } else {
            appendSystemMessage("Pinging user: " + username);
            multicastManager.sendMessage("/ping " + username);
        }
    }
    
    /**
     * Show detailed network information
     */
    private void showNetworkInfo() {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Network Information:\n");
            
            // Get local host information
            InetAddress localhost = InetAddress.getLocalHost();
            info.append("Local hostname: ").append(localhost.getHostName()).append("\n");
            info.append("Local IP: ").append(localhost.getHostAddress()).append("\n");
            
            // Get network interfaces
            info.append("\nNetwork Interfaces:\n");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (nif.isUp() && !nif.isLoopback() && !nif.isVirtual()) {
                    info.append("- ").append(nif.getDisplayName()).append("\n");
                    info.append("  MAC: ").append(formatMacAddress(nif.getHardwareAddress())).append("\n");
                    info.append("  MTU: ").append(nif.getMTU()).append("\n");
                    
                    Enumeration<InetAddress> addresses = nif.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        if (addr instanceof Inet4Address) {
                            info.append("  IPv4: ").append(addr.getHostAddress()).append("\n");
                        } else if (addr instanceof Inet6Address) {
                            info.append("  IPv6: ").append(addr.getHostAddress()).append("\n");
                        }
                    }
                }
            }
            
            appendSystemMessage(info.toString());
        } catch (Exception e) {
            appendSystemMessage("Error getting network info: " + e.getMessage());
        }
    }
    
    /**
     * Format MAC address as XX:XX:XX:XX:XX:XX
     */
    private String formatMacAddress(byte[] mac) {
        if (mac == null) return "Unknown";
        
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
     * Show an example of code block formatting
     */
    private void showCodeExample() {
        appendSystemMessage("Code Block Example:\n" +
                           "To share code, use the following format:\n\n" +
                           "```language\n" +
                           "your code here\n" +
                           "```\n\n" +
                           "For example, to share JavaScript code:\n\n" +
                           "```js\n" +
                           "function sayHello(name) {\n" +
                           "    console.log(`Hello, ${name}!`);\n" +
                           "    return true;\n" +
                           "}\n" +
                           "```\n\n" +
                           "Supported languages include: js, java, python, html, css, and more.");
        
        // Send an example to the chat
        multicastManager.sendMessage("```java\n" +
                                    "public class HelloWorld {\n" +
                                    "    public static void main(String[] args) {\n" +
                                    "        System.out.println(\"Hello, World!\");\n" +
                                    "    }\n" +
                                    "}\n" +
                                    "```");
    }
}
