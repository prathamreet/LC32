import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom renderer for the user list that displays users with avatars and status indicators.
 */
public class UserListRenderer extends DefaultListCellRenderer {
    private final String currentUser;
    
    public UserListRenderer(String currentUser) {
        this.currentUser = currentUser;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        String username = value.toString();
        boolean isCurrentUser = username.equals(currentUser) || username.contains(currentUser);
        
        // Status indicator
        JPanel statusPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw status circle
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
        
        // Avatar panel
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw avatar circle
                Color avatarColor = isCurrentUser ? 
                    ThemeManager.getCurrentTheme().primaryColor : 
                    ThemeManager.getCurrentTheme().secondaryColor;
                g2d.setColor(avatarColor);
                g2d.fillOval(0, 0, 30, 30);
                
                // Draw initial
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
        
        // Username label
        JLabel usernameLabel = new JLabel(username + (isCurrentUser ? " (You)" : ""));
        usernameLabel.setFont(new Font("Segoe UI", isCurrentUser ? Font.BOLD : Font.PLAIN, 14));
        usernameLabel.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        // Add components to panel
        JPanel leftPanel = new JPanel(new BorderLayout(5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(statusPanel, BorderLayout.WEST);
        leftPanel.add(avatarPanel, BorderLayout.CENTER);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(usernameLabel, BorderLayout.CENTER);
        
        // Set background color based on selection
        if (isSelected) {
            panel.setBackground(ThemeManager.getCurrentTheme().primaryColor.brighter());
            usernameLabel.setForeground(Color.WHITE);
        } else {
            panel.setBackground(ThemeManager.getCurrentTheme().cardColor);
        }
        
        return panel;
    }
}