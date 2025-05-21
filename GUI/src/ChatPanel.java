import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This panel displays all the chat messages in a scrollable area.
 * It handles different types of messages (user messages and system messages)
 * and automatically scrolls to show new messages.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class ChatPanel extends JPanel {
    private final List<Component> messages = new ArrayList<>(); // List of all message components
    private final String currentUser;                          // The current user's nickname
    private final JScrollPane scrollPane;                      // Scroll pane containing this panel
    
    /**
     * Creates a new chat panel for displaying messages.
     * 
     * @param currentUser The current user's nickname
     */
    public ChatPanel(String currentUser) {
        this.currentUser = currentUser;
        
        // Use BoxLayout to stack messages vertically
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Add some space at the bottom for better appearance
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create a scroll pane to hold all the messages
        scrollPane = new JScrollPane(this);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Hide scrollbars for a cleaner look, but still allow scrolling
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Make scrolling faster and smoother
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        
        // Make sure the scroll pane is a reasonable size
        scrollPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        // Add custom mouse wheel scrolling since we hid the scrollbars
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            int direction = e.getWheelRotation();
            int increment = verticalBar.getUnitIncrement() * 3; // Faster scrolling
            
            // Scroll up or down based on wheel direction
            if (direction < 0) {
                // Scroll up when wheel moves up
                verticalBar.setValue(verticalBar.getValue() - increment);
            } else {
                // Scroll down when wheel moves down
                verticalBar.setValue(verticalBar.getValue() + increment);
            }
        });
        
        // Make the scroll pane background match our theme
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Add a small amount of padding around the edges
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    /**
     * Gets the scroll pane that contains this chat panel.
     * 
     * @return The scroll pane containing the chat messages
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    /**
     * Adds a user message to the chat.
     * 
     * @param sender The nickname of the message sender
     * @param message The content of the message
     */
    public void addMessage(String sender, String message) {
        // Check if this message is from the current user
        boolean isCurrentUser = sender.equals(currentUser);
        
        // Create a chat bubble for this message
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(sender, message, new Date(), isCurrentUser);
        
        // Create a wrapper panel to position the bubble
        // My messages go on the right, others' messages go on the left
        JPanel wrapperPanel = new JPanel(new FlowLayout(
                isCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapperPanel.setOpaque(false);
        
        // Make the wrapper fill the width of the chat panel
        wrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 10));
        wrapperPanel.add(bubble);
        
        // Add the message to our list and panel
        messages.add(wrapperPanel);
        add(wrapperPanel);
        
        // Update the display and scroll to show the new message
        revalidate();
        scrollToBottom();
    }
    
    /**
     * Adds a user message to the chat with a specific timestamp.
     * 
     * @param sender The nickname of the message sender
     * @param message The content of the message
     * @param timestamp When the message was sent
     * @param isCurrentUser Whether this message is from the current user
     */
    public void addMessage(String sender, String message, Date timestamp, boolean isCurrentUser) {
        // Create a chat bubble with the specified timestamp
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(sender, message, timestamp, isCurrentUser);
        
        // Create a wrapper panel to position the bubble
        JPanel wrapperPanel = new JPanel(new FlowLayout(
                isCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapperPanel.setOpaque(false);
        
        // Make the wrapper fill the width of the chat panel
        wrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 10));
        wrapperPanel.add(bubble);
        
        // Add the message to our list and panel
        messages.add(wrapperPanel);
        add(wrapperPanel);
        
        // Update the display and scroll to show the new message
        revalidate();
        scrollToBottom();
    }
    
    /**
     * Adds a system message to the chat.
     * System messages are centered and have a different style.
     * 
     * @param message The system message to display
     */
    public void addSystemMessage(String message) {
        // Create a special bubble for system messages
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(message, new Date());
        
        // Center the system message
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperPanel.setOpaque(false);
        
        // Make the wrapper fill the width of the chat panel
        wrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 5));
        wrapperPanel.add(bubble);
        
        // Add the message to our list and panel
        messages.add(wrapperPanel);
        add(wrapperPanel);
        
        // Update the display and scroll to show the new message
        revalidate();
        scrollToBottom();
    }
    
    /**
     * Clears all messages from the chat.
     */
    public void clearMessages() {
        // Remove all messages from our list
        messages.clear();
        
        // Remove all components from the panel
        removeAll();
        
        // Update the display
        revalidate();
        repaint();
    }
    
    /**
     * Scrolls to the bottom of the chat to show the newest messages.
     * This is public so it can be called when the window is resized.
     */
    public void scrollToBottom() {
        // Use SwingUtilities.invokeLater to ensure this happens after layout
        SwingUtilities.invokeLater(() -> {
            try {
                // Make sure all components are properly sized
                scrollPane.validate();
                
                // Get the vertical scrollbar
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                
                // Scroll to the bottom
                vertical.setValue(vertical.getMaximum());
                
                // Make sure the UI is updated
                revalidate();
                repaint();
            } catch (Exception e) {
                // Log any errors but don't crash
                System.err.println("Error scrolling to bottom: " + e.getMessage());
            }
        });
    }
    
    /**
     * Updates the panel with the current theme colors.
     */
    public void applyTheme() {
        setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        revalidate();
        repaint();
    }
}