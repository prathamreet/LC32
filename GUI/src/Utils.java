import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A collection of helpful tools and methods for our chat app.
 * This class provides common functions that are used throughout the application.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class Utils {
    // These colors are kept for backward compatibility
    // New code should use ThemeManager instead
    public static final Color PRIMARY_COLOR = new Color(66, 133, 244);  // Blue
    public static final Color SECONDARY_COLOR = new Color(76, 175, 80); // Green
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Light gray
    public static final Color MY_MESSAGE_COLOR = new Color(220, 248, 198); // Light green
    public static final Color OTHER_MESSAGE_COLOR = new Color(255, 255, 255); // White
    public static final Color SYSTEM_MESSAGE_COLOR = new Color(232, 234, 246); // Light purple
    
    /**
     * Adds a timestamp and nickname to a message.
     * 
     * @param nickname The sender's nickname
     * @param message The message content
     * @return A formatted message with timestamp and nickname
     */
    public static String formatMessage(String nickname, String message) {
        // Create a timestamp in hours:minutes:seconds format
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(new Date());
        
        // Combine everything into a nicely formatted message
        return "[" + timestamp + "] " + nickname + ": " + message;
    }
    
    /**
     * Converts a Color to a hex string (like #FF0000 for red).
     * 
     * @param color The color to convert
     * @return A hex string representation of the color
     */
    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * A custom border with rounded corners.
     * This makes UI elements look more modern and friendly.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;     // How rounded the corners are
        private final Color color;    // The border color
        private final int thickness;  // How thick the border is
        
        /**
         * Creates a new rounded border.
         * 
         * @param radius How rounded the corners should be
         * @param color The color of the border
         * @param thickness How thick the border should be
         */
        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }
        
        /**
         * Draws the border around a component.
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // Create a graphics object we can use for drawing
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Make the edges smooth
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Set the border color and thickness
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            
            // Draw a rounded rectangle as the border
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius));
            
            // Clean up
            g2d.dispose();
        }
        
        /**
         * Returns the space needed for the border.
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
        
        /**
         * Updates the provided insets with the space needed for the border.
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }
    }
    
    /**
     * Creates a custom styled button.
     * Note: This method is deprecated. Use ThemeManager.createThemedButton() instead.
     * 
     * @param text The button text
     * @param bgColor The background color
     * @param fgColor The text color
     * @return A styled JButton
     * @deprecated Use ThemeManager.createThemedButton() instead
     */
    @Deprecated
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        // Create a basic button
        JButton button = new JButton(text);
        
        // Style it with nice fonts and colors
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Use a hand cursor to show it's clickable
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add a hover effect to make it more interactive
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Darken the button when hovered
                button.setBackground(darken(bgColor, 0.1f));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Restore original color when not hovered
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Makes a color darker by a certain amount.
     * 
     * @param color The color to darken
     * @param factor How much to darken (0-1)
     * @return The darkened color
     */
    public static Color darken(Color color, float factor) {
        // Reduce each color component by the factor
        int r = Math.max(0, (int)(color.getRed() * (1 - factor)));
        int g = Math.max(0, (int)(color.getGreen() * (1 - factor)));
        int b = Math.max(0, (int)(color.getBlue() * (1 - factor)));
        
        // Create a new color with the darkened components
        return new Color(r, g, b);
    }
    
    /**
     * Makes a color lighter by a certain amount.
     * 
     * @param color The color to lighten
     * @param factor How much to lighten (0-1)
     * @return The lightened color
     */
    public static Color lighten(Color color, float factor) {
        // Increase each color component by the factor
        int r = Math.min(255, (int)(color.getRed() * (1 + factor)));
        int g = Math.min(255, (int)(color.getGreen() * (1 + factor)));
        int b = Math.min(255, (int)(color.getBlue() * (1 + factor)));
        
        // Create a new color with the lightened components
        return new Color(r, g, b);
    }
    
    /**
     * Creates a panel with a smooth gradient background.
     * 
     * @param startColor The color at the top
     * @param endColor The color at the bottom
     * @return A panel with a gradient background
     */
    public static JPanel createGradientPanel(Color startColor, Color endColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Create a graphics object we can use for drawing
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Make the edges smooth
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create a gradient from top to bottom
                GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
                );
                
                // Fill the panel with the gradient
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Clean up
                g2d.dispose();
            }
        };
    }
    
    /**
     * Creates a circular avatar with an initial in the center.
     * 
     * @param initial The letter to display (usually first letter of name)
     * @param bgColor The background color of the avatar
     * @param size The size of the avatar in pixels
     * @return A panel containing the avatar
     */
    public static JPanel createAvatarPanel(String initial, Color bgColor, int size) {
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Create a graphics object we can use for drawing
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Make the edges smooth
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a colored circle for the avatar background
                g2d.setColor(bgColor);
                g2d.fillOval(0, 0, size, size);
                
                // Draw the initial in the center
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                
                // Calculate the position to center the text
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getHeight();
                g2d.drawString(initial, (size - textWidth) / 2, size / 2 + textHeight / 4);
                
                // Clean up
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(size, size);
            }
        };
        
        // Make the panel background transparent
        avatarPanel.setOpaque(false);
        return avatarPanel;
    }
    
    /**
     * Creates a panel with rounded corners and an optional shadow.
     * 
     * @param bgColor The background color of the panel
     * @param cornerRadius How rounded the corners should be
     * @param withShadow Whether to add a shadow effect
     * @return A panel with rounded corners
     */
    public static JPanel createRoundedPanel(Color bgColor, int cornerRadius, boolean withShadow) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Create a graphics object we can use for drawing
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Make the edges smooth
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow if requested
                if (withShadow) {
                    // Create a layered shadow effect
                    for (int i = 0; i < 4; i++) {
                        // Calculate opacity for this layer
                        float alpha = 0.1f * (4 - i) / 4;
                        g2d.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
                        
                        // Draw a rounded rectangle for this shadow layer
                        g2d.fill(new RoundRectangle2D.Double(
                            i, i, getWidth() - i * 2, getHeight() - i * 2, cornerRadius, cornerRadius));
                    }
                }
                
                // Draw the panel background
                g2d.setColor(bgColor);
                
                // If we have a shadow, offset the background slightly
                g2d.fill(new RoundRectangle2D.Double(
                    withShadow ? 4 : 0, 
                    withShadow ? 4 : 0, 
                    getWidth() - (withShadow ? 8 : 0), 
                    getHeight() - (withShadow ? 8 : 0), 
                    cornerRadius, cornerRadius));
                
                // Clean up
                g2d.dispose();
            }
        };
        
        // Make the panel background transparent so our custom painting shows
        panel.setOpaque(false);
        return panel;
    }
}