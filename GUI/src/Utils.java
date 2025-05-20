import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class providing helper methods for the chat application.
 */
public class Utils {
    // Legacy color constants - kept for backward compatibility
    // New code should use ThemeManager instead
    public static final Color PRIMARY_COLOR = new Color(66, 133, 244);
    public static final Color SECONDARY_COLOR = new Color(76, 175, 80);
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color MY_MESSAGE_COLOR = new Color(220, 248, 198);
    public static final Color OTHER_MESSAGE_COLOR = new Color(255, 255, 255);
    public static final Color SYSTEM_MESSAGE_COLOR = new Color(232, 234, 246);
    
    /**
     * Formats a message with nickname and timestamp.
     */
    public static String formatMessage(String nickname, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new Date());
        return "[" + timestamp + "] " + nickname + ": " + message;
    }
    
    /**
     * Converts a Color object to hex string.
     */
    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Creates a rounded border for UI components.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;
        
        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }
    
    /**
     * Creates a custom button with modern styling.
     * @deprecated Use ThemeManager.createThemedButton() instead
     */
    @Deprecated
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(darken(bgColor, 0.1f));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Darkens a color by the specified factor.
     */
    public static Color darken(Color color, float factor) {
        int r = Math.max(0, (int)(color.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - factor)));
        return new Color(r, g, b);
    }
    
    /**
     * Lightens a color by the specified factor.
     */
    public static Color lighten(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() * (1 + factor)));
        int g = Math.min(255, (int)(color.getGreen() * (1 + factor)));
        int b = Math.min(255, (int)(color.getBlue() * (1 + factor)));
        return new Color(r, g, b);
    }
    
    /**
     * Creates a panel with a gradient background
     */
    public static JPanel createGradientPanel(Color startColor, Color endColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
    }
    
    /**
     * Creates a circular avatar with the given initial and color
     */
    public static JPanel createAvatarPanel(String initial, Color bgColor, int size) {
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle
                g2d.setColor(bgColor);
                g2d.fillOval(0, 0, size, size);
                
                // Draw initial
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                g2d.drawString(initial, (size - textWidth) / 2, size / 2 + textHeight / 4);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(size, size);
            }
        };
        avatarPanel.setOpaque(false);
        return avatarPanel;
    }
    
    /**
     * Creates a rounded panel with optional shadow
     */
    public static JPanel createRoundedPanel(Color bgColor, int cornerRadius, boolean withShadow) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow if requested
                if (withShadow) {
                    for (int i = 0; i < 4; i++) {
                        float alpha = 0.1f * (4 - i) / 4;
                        g2d.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                        g2d.fill(new RoundRectangle2D.Double(i, i, getWidth() - i * 2, getHeight() - i * 2, cornerRadius, cornerRadius));
                    }
                }
                
                // Draw panel background
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Double(withShadow ? 4 : 0, withShadow ? 4 : 0, 
                        getWidth() - (withShadow ? 8 : 0), getHeight() - (withShadow ? 8 : 0), 
                        cornerRadius, cornerRadius));
                
                g2d.dispose();
            }
        };
        
        panel.setOpaque(false);
        return panel;
    }
}