import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * This class creates a custom look for users in the online users list.
 * It shows each user with a colored avatar and status indicator.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class UserListRenderer extends DefaultListCellRenderer {
    private final String currentUser; // The current user's nickname
    
    /**
     * Creates a new renderer for the user list.
     * 
     * @param currentUser The current user's nickname
     */
    public UserListRenderer(String currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Creates a custom component for each user in the list.
     * This is called automatically by the JList for each user.
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Create a panel to hold all the user info
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        // Get the username and check if it's the current user
        String username = value.toString();
        boolean isCurrentUser = username.equals(currentUser) || username.contains(currentUser);
        
        // Create a status indicator (colored dot)
        JPanel statusPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a colored circle to show online status
                // Blue for current user, green for others
                Color statusColor = isCurrentUser ? 
                    ThemeManager.getCurrentTheme().primaryColor : 
                    ThemeManager.getCurrentTheme().secondaryColor;
                g2d.setColor(statusColor);
                g2d.fillOval(0, 4, 12, 12);
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 20);
            }
        };
        statusPanel.setOpaque(false);
        
        // Create an avatar with the user's initial
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a colored circle for the avatar background
                // Blue for current user, green for others
                Color avatarColor = isCurrentUser ? 
                    ThemeManager.getCurrentTheme().primaryColor : 
                    ThemeManager.getCurrentTheme().secondaryColor;
                g2d.setColor(avatarColor);
                g2d.fillOval(0, 0, 30, 30);
                
                // Draw the first letter of the username in the avatar
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = username.substring(0, 1).toUpperCase();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                g2d.drawString(initial, (30 - textWidth) / 2, 15 + textHeight / 4);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 30);
            }
        };
        avatarPanel.setOpaque(false);
        
        // Create a label for the username
        // Add "(You)" if it's the current user
        JLabel usernameLabel = new JLabel(username + (isCurrentUser ? " (You)" : ""));
        usernameLabel.setFont(new Font("Segoe UI", isCurrentUser ? Font.BOLD : Font.PLAIN, 14));
        usernameLabel.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        // Combine the status indicator and avatar
        JPanel leftPanel = new JPanel(new BorderLayout(5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(statusPanel, BorderLayout.WEST);
        leftPanel.add(avatarPanel, BorderLayout.CENTER);
        
        // Add everything to the main panel
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(usernameLabel, BorderLayout.CENTER);
        
        // Change colors if this item is selected
        if (isSelected) {
            panel.setBackground(ThemeManager.getCurrentTheme().primaryColor.brighter());
            usernameLabel.setForeground(Color.WHITE);
        } else {
            panel.setBackground(ThemeManager.getCurrentTheme().cardColor);
        }
        
        return panel;
    }
}