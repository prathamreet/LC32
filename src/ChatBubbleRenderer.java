import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Custom renderer for chat messages that displays them as bubbles.
 * Supports different styles for user's messages, others' messages, and system messages.
 */
public class ChatBubbleRenderer extends JPanel {
    private static final int BUBBLE_RADIUS = 12; // Reduced from 15
    private static final int BUBBLE_SPACING = 5; // Reduced from 10
    private static final int AVATAR_SIZE = 24; // Reduced from 30
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    
    private final String sender;
    private final String message;
    private final Date timestamp;
    private final boolean isCurrentUser;
    private final boolean isSystemMessage;
    private final Color bubbleColor;
    private final Color textColor;
    
    /**
     * Create a chat bubble for a user message
     */
    public ChatBubbleRenderer(String sender, String message, Date timestamp, boolean isCurrentUser) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.isCurrentUser = isCurrentUser;
        this.isSystemMessage = false;
        
        ThemeManager.ColorScheme theme = ThemeManager.getCurrentTheme();
        this.bubbleColor = isCurrentUser ? theme.myMessageColor : theme.otherMessageColor;
        this.textColor = theme.textPrimaryColor;
        
        setupPanel();
    }
    
    /**
     * Create a chat bubble for a system message
     */
    public ChatBubbleRenderer(String message, Date timestamp) {
        this.sender = "System";
        this.message = message;
        this.timestamp = timestamp;
        this.isCurrentUser = false;
        this.isSystemMessage = true;
        
        ThemeManager.ColorScheme theme = ThemeManager.getCurrentTheme();
        this.bubbleColor = theme.systemMessageColor;
        this.textColor = theme.textPrimaryColor;
        
        setupPanel();
    }
    
    private void setupPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());
        
        // Use smaller border for system messages to make them more compact
        if (isSystemMessage) {
            setBorder(new EmptyBorder(2, BUBBLE_SPACING, 2, BUBBLE_SPACING));
        } else {
            setBorder(new EmptyBorder(BUBBLE_SPACING, BUBBLE_SPACING, BUBBLE_SPACING, BUBBLE_SPACING));
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Enable high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        int width = getWidth();
        int height = getHeight();
        int bubbleWidth = width - 2 * BUBBLE_SPACING;
        int bubbleHeight = height - 2 * BUBBLE_SPACING;
        
        // Calculate bubble position
        int x = BUBBLE_SPACING;
        if (isCurrentUser && !isSystemMessage) {
            x = width - bubbleWidth - BUBBLE_SPACING;
        } else if (isSystemMessage) {
            x = (width - bubbleWidth) / 2;
        }
        
        // Draw bubble background with a slight shadow for depth
        Color shadowColor = new Color(0, 0, 0, 30);
        g2d.setColor(shadowColor);
        RoundRectangle2D shadow = new RoundRectangle2D.Float(
                x + 2, BUBBLE_SPACING + 2, bubbleWidth, bubbleHeight, BUBBLE_RADIUS, BUBBLE_RADIUS);
        g2d.fill(shadow);
        
        // Draw the actual bubble
        g2d.setColor(bubbleColor);
        RoundRectangle2D bubble = new RoundRectangle2D.Float(
                x, BUBBLE_SPACING, bubbleWidth, bubbleHeight, BUBBLE_RADIUS, BUBBLE_RADIUS);
        g2d.fill(bubble);
        
        // Draw a subtle border around the bubble
        g2d.setColor(new Color(bubbleColor.getRed(), bubbleColor.getGreen(), bubbleColor.getBlue(), 200));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.draw(bubble);
        
        // Draw sender name
        if (!isCurrentUser && !isSystemMessage) {
            g2d.setColor(ThemeManager.getCurrentTheme().primaryColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.drawString(sender, x + 10, BUBBLE_SPACING + 20);
        }
        
        // Draw message text with improved font rendering
        g2d.setColor(textColor);
        // Use a font that supports emoji characters
        // Use smaller font for system messages
        Font messageFont = new Font("Segoe UI Emoji", 
                                   isSystemMessage ? Font.ITALIC : Font.PLAIN, 
                                   isSystemMessage ? 12 : 14);
        g2d.setFont(messageFont);
        
        // Calculate text position
        int textY = BUBBLE_SPACING + 40;
        if (isCurrentUser) {
            textY = BUBBLE_SPACING + 25;
        } else if (isSystemMessage) {
            textY = BUBBLE_SPACING + 18; // Reduced for system messages
        }
        
        // Draw message with word wrap
        FontMetrics fm = g2d.getFontMetrics(messageFont);
        int textX = x + 10;
        
        // Handle null or empty message
        if (message == null || message.isEmpty()) {
            g2d.drawString("[Empty message]", textX, textY);
        } else {
            // Split by newlines and draw each line
            String[] lines = message.split("\n");
            for (String line : lines) {
                // Word wrap for long lines
                int availableWidth = bubbleWidth - 20; // 10px padding on each side
                if (fm.stringWidth(line) <= availableWidth) {
                    // Line fits, draw it directly
                    g2d.drawString(line, textX, textY);
                    textY += fm.getHeight();
                } else {
                    // Line is too long, need to wrap
                    String[] words = line.split(" ");
                    StringBuilder currentLine = new StringBuilder();
                    
                    for (String word : words) {
                        // Check if adding this word would exceed the width
                        String testLine = currentLine + (currentLine.length() > 0 ? " " : "") + word;
                        if (fm.stringWidth(testLine) <= availableWidth) {
                            // Word fits, add it to the current line
                            if (currentLine.length() > 0) {
                                currentLine.append(" ");
                            }
                            currentLine.append(word);
                        } else {
                            // Word doesn't fit, draw the current line and start a new one
                            if (currentLine.length() > 0) {
                                g2d.drawString(currentLine.toString(), textX, textY);
                                textY += fm.getHeight();
                                currentLine = new StringBuilder(word);
                            } else {
                                // Single word is too long, need to break it
                                g2d.drawString(word, textX, textY);
                                textY += fm.getHeight();
                            }
                        }
                    }
                    
                    // Draw the last line if there's anything left
                    if (currentLine.length() > 0) {
                        g2d.drawString(currentLine.toString(), textX, textY);
                        textY += fm.getHeight();
                    }
                }
            }
        }
        
        // Draw timestamp
        String time = TIME_FORMAT.format(timestamp);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.setColor(new Color(150, 150, 150));
        
        int timeWidth = fm.stringWidth(time);
        int timeX = isCurrentUser ? x + bubbleWidth - timeWidth - 10 : x + 10;
        if (isSystemMessage) {
            timeX = x + (bubbleWidth - timeWidth) / 2;
        }
        
        g2d.drawString(time, timeX, BUBBLE_SPACING + bubbleHeight - 10);
        
        g2d.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        // Use smaller font for system messages
        Font messageFont = isSystemMessage ? 
            new Font("Segoe UI", Font.ITALIC, 12) : 
            new Font("Segoe UI", Font.PLAIN, 14);
            
        FontMetrics fm = getFontMetrics(messageFont);
        
        // Calculate max width based on parent container size if available
        Container parent = getParent();
        int containerWidth = 400; // Default fallback
        
        if (parent != null && parent.getWidth() > 0) {
            // Use a percentage of the parent width
            containerWidth = (int)(parent.getWidth() * 0.8);
            // Ensure reasonable bounds
            containerWidth = Math.max(200, Math.min(containerWidth, 600));
        }
        
        // Use narrower width for system messages
        int maxWidth = isSystemMessage ? Math.min(300, containerWidth - 40) : containerWidth;
        int textWidth = 0;
        int lineCount = 0;
        
        // Handle null or empty message
        if (message == null || message.isEmpty()) {
            textWidth = fm.stringWidth("[Empty message]");
            lineCount = 1;
        } else {
            // Calculate width and line count with word wrapping
            for (String line : message.split("\n")) {
                int lineWidth = fm.stringWidth(line);
                if (lineWidth <= maxWidth - 40) { // 40px for padding
                    // Line fits without wrapping
                    textWidth = Math.max(textWidth, lineWidth);
                    lineCount++;
                } else {
                    // Line needs wrapping
                    String[] words = line.split(" ");
                    StringBuilder currentLine = new StringBuilder();
                    int currentLineWidth = 0;
                    
                    for (String word : words) {
                        int wordWidth = fm.stringWidth(" " + word);
                        if (currentLineWidth + wordWidth <= maxWidth - 40) {
                            // Word fits on current line
                            currentLine.append(" ").append(word);
                            currentLineWidth += wordWidth;
                        } else {
                            // Word doesn't fit, start a new line
                            textWidth = Math.max(textWidth, currentLineWidth);
                            lineCount++;
                            currentLine = new StringBuilder(word);
                            currentLineWidth = fm.stringWidth(word);
                        }
                    }
                    
                    // Add the last line
                    if (currentLine.length() > 0) {
                        textWidth = Math.max(textWidth, currentLineWidth);
                        lineCount++;
                    }
                }
            }
        }
        
        // Ensure minimum width for very short messages
        // System messages can be narrower
        textWidth = Math.max(textWidth, isSystemMessage ? 80 : 100);
        
        // Calculate bubble dimensions
        int bubbleWidth = Math.min(maxWidth, textWidth + 40); // Add padding
        
        // Reduce extra space for system messages
        int extraHeight;
        if (isSystemMessage) {
            extraHeight = 20; // Minimal extra space for system messages
        } else {
            extraHeight = isCurrentUser ? 30 : 50; // Extra space for sender name and timestamp
        }
        
        int bubbleHeight = fm.getHeight() * Math.max(1, lineCount) + extraHeight;
        
        // Use smaller spacing for system messages
        int spacing = isSystemMessage ? 2 : BUBBLE_SPACING;
        
        return new Dimension(bubbleWidth + 2 * spacing, bubbleHeight + 2 * spacing);
    }
}