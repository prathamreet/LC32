import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom panel for displaying chat messages as bubbles.
 * Supports different message types and auto-scrolling.
 */
public class ChatPanel extends JPanel {
    private final List<Component> messages = new ArrayList<>();
    private final String currentUser;
    private final JScrollPane scrollPane;
    
    public ChatPanel(String currentUser) {
        this.currentUser = currentUser;
        
        // Use BoxLayout for vertical stacking of messages
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Add minimal padding at the bottom to ensure space after the last message
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create a scroll pane with improved settings for better responsiveness
        scrollPane = new JScrollPane(this);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Hide scrollbars but still allow scrolling with mouse wheel
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Increase scroll speed for better UX without visible scrollbar
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        
        // Make sure the scroll pane fills available space
        scrollPane.setMinimumSize(new Dimension(300, 200));
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        // Create a custom scroll pane that still allows scrolling with mouse wheel
        scrollPane.addMouseWheelListener(e -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
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
        
        // Improve scroll pane appearance
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        
        // Add a small amount of padding to prevent messages touching the edges
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    /**
     * Get the scroll pane containing this chat panel
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }
    
    /**
     * Add a user message to the chat
     */
    public void addMessage(String sender, String message) {
        boolean isCurrentUser = sender.equals(currentUser);
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(sender, message, new Date(), isCurrentUser);
        
        // Align bubble to left or right based on sender
        JPanel wrapperPanel = new JPanel(new FlowLayout(
                isCurrentUser ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        wrapperPanel.setOpaque(false);
        
        // Make wrapper panel fill the width for better responsiveness
        wrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 10));
        wrapperPanel.add(bubble);
        
        messages.add(wrapperPanel);
        add(wrapperPanel);
        revalidate();
        scrollToBottom();
    }
    
    /**
     * Add a system message to the chat
     */
    public void addSystemMessage(String message) {
        ChatBubbleRenderer bubble = new ChatBubbleRenderer(message, new Date());
        
        // Center the system message
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperPanel.setOpaque(false);
        
        // Make wrapper panel fill the width for better responsiveness
        wrapperPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height + 5));
        wrapperPanel.add(bubble);
        
        messages.add(wrapperPanel);
        add(wrapperPanel);
        revalidate();
        scrollToBottom();
    }
    
    /**
     * Clear all messages from the chat
     */
    public void clearMessages() {
        messages.clear();
        removeAll();
        revalidate();
        repaint();
    }
    
    /**
     * Scroll to the bottom of the chat
     * Made public so it can be called when window is resized
     */
    public void scrollToBottom() {
        // Use a more robust approach to ensure scrolling works correctly
        SwingUtilities.invokeLater(() -> {
            try {
                // First validate the component hierarchy to ensure sizes are correct
                scrollPane.validate();
                
                // Get the vertical scrollbar
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                
                // Set to maximum value to scroll to bottom
                vertical.setValue(vertical.getMaximum());
                
                // Additional call to ensure UI is updated
                revalidate();
                repaint();
            } catch (Exception e) {
                // Log any errors but don't crash
                System.err.println("Error scrolling to bottom: " + e.getMessage());
            }
        });
    }
    
    /**
     * Apply the current theme to the chat panel
     */
    public void applyTheme() {
        setBackground(ThemeManager.getCurrentTheme().backgroundColor);
        revalidate();
        repaint();
    }
}