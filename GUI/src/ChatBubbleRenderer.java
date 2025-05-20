import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    
    // For code block copy button
    private Rectangle codeBlockCopyButton;
    private String codeToClipboard;
    
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
        
        // Add right-click context menu for copying
        addCopyContextMenu();
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
        
        // Add right-click context menu for copying
        addCopyContextMenu();
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
        
        // Add mouse listener for code block copy button
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (codeBlockCopyButton != null && codeBlockCopyButton.contains(e.getPoint())) {
                    // Copy code to clipboard
                    if (codeToClipboard != null && !codeToClipboard.isEmpty()) {
                        copyMessageToClipboard(codeToClipboard);
                    }
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                // Change cursor to hand when over the copy button
                if (codeBlockCopyButton != null) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Reset cursor
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        // Add mouse motion listener to change cursor when over the copy button
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
    
    /**
     * Add a context menu for copying message text
     */
    private void addCopyContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        
        // Add copy message option
        JMenuItem copyItem = new JMenuItem("Copy Message");
        copyItem.addActionListener(e -> {
            copyMessageToClipboard(message);
        });
        popupMenu.add(copyItem);
        
        // Add copy with sender option
        JMenuItem copyWithSenderItem = new JMenuItem("Copy with Sender");
        copyWithSenderItem.addActionListener(e -> {
            copyMessageToClipboard(sender + ": " + message);
        });
        popupMenu.add(copyWithSenderItem);
        
        // For code blocks, add special copy option
        if (message != null && message.contains("```")) {
            int firstDelimiter = message.indexOf("```");
            int lastDelimiter = message.lastIndexOf("```");
            
            if (firstDelimiter >= 0 && lastDelimiter > firstDelimiter) {
                JMenuItem copyCodeItem = new JMenuItem("Copy Code Only");
                copyCodeItem.addActionListener(e -> {
                    // Extract just the code part
                    int firstNewline = message.indexOf('\n', firstDelimiter);
                    if (firstNewline > 0 && firstNewline < lastDelimiter) {
                        String code = message.substring(firstNewline + 1, lastDelimiter);
                        copyMessageToClipboard(code);
                    }
                });
                popupMenu.add(copyCodeItem);
            }
        }
        
        // Add mouse listener to show popup menu on right-click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }
            
            private void showPopup(MouseEvent e) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
    
    /**
     * Copy text to clipboard
     */
    private void copyMessageToClipboard(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        try {
            // Get system clipboard
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            
            // Set clipboard contents
            StringSelection selection = new StringSelection(text);
            clipboard.setContents(selection, null);
            
            // Visual feedback (flash the bubble)
            Color originalColor = getBackground();
            setBackground(new Color(100, 200, 100)); // Flash green
            
            // Reset color after a short delay
            Timer timer = new Timer(300, evt -> {
                setBackground(originalColor);
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
            
        } catch (Exception e) {
            System.err.println("Error copying to clipboard: " + e.getMessage());
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
        
        // Draw message with word wrap and code block support
        FontMetrics fm = g2d.getFontMetrics(messageFont);
        int textX = x + 10;
        
        // Handle null or empty message
        if (message == null || message.isEmpty()) {
            g2d.drawString("[Empty message]", textX, textY);
        } else {
            // Check if this is a code block
            boolean isCodeBlock = false;
            String codeLanguage = "";
            String messageContent = message;
            
            // Check for code block format: ```language\ncode\n```
            if (message.startsWith("```")) {
                int firstNewline = message.indexOf('\n');
                int endCodeBlock = message.lastIndexOf("```");
                
                if (firstNewline > 3 && endCodeBlock > firstNewline) {
                    isCodeBlock = true;
                    codeLanguage = message.substring(3, firstNewline).trim();
                    messageContent = message.substring(firstNewline + 1, endCodeBlock);
                    
                    // Draw code block with special formatting
                    drawCodeBlock(g2d, messageContent, codeLanguage, textX, textY, bubbleWidth - 20);
                    return;
                }
            }
            
            // Regular message - split by newlines and draw each line
            String[] lines = messageContent.split("\n");
            for (String line : lines) {
                // Word wrap for long lines
                int availableWidth = bubbleWidth - 20; // 10px padding on each side
                
                // Handle extremely long words by forcing breaks
                if (line.length() > 100) {
                    // Break into chunks of reasonable size
                    int chunkSize = 50; // Characters per chunk
                    for (int i = 0; i < line.length(); i += chunkSize) {
                        String chunk = line.substring(i, Math.min(i + chunkSize, line.length()));
                        if (i > 0) {
                            // Add a hyphen to indicate continuation
                            chunk = "→ " + chunk;
                        }
                        
                        // Draw the chunk
                        g2d.drawString(chunk, textX, textY);
                        textY += fm.getHeight();
                    }
                } else if (fm.stringWidth(line) <= availableWidth) {
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
                                if (word.length() > 30) {
                                    // Break very long word
                                    int half = word.length() / 2;
                                    g2d.drawString(word.substring(0, half) + "-", textX, textY);
                                    textY += fm.getHeight();
                                    g2d.drawString(word.substring(half), textX, textY);
                                } else {
                                    g2d.drawString(word, textX, textY);
                                }
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
        
        int timeWidth = g2d.getFontMetrics().stringWidth(time);
        
        // Ensure timestamp doesn't overlap with message text
        // Add minimal padding at the bottom for the timestamp
        textY += 8; // Reduced space for timestamp
        
        int timeX = isCurrentUser ? x + bubbleWidth - timeWidth - 10 : x + 10;
        if (isSystemMessage) {
            timeX = x + (bubbleWidth - timeWidth) / 2;
        }
        
        g2d.drawString(time, timeX, textY);
        
        g2d.dispose();
    }
    
    /**
     * Draw a formatted code block with syntax highlighting
     */
    private void drawCodeBlock(Graphics2D g2d, String code, String language, int x, int y, int width) {
        // Use a monospaced font for code
        Font codeFont = new Font("Consolas", Font.PLAIN, 12);
        g2d.setFont(codeFont);
        FontMetrics fm = g2d.getFontMetrics(codeFont);
        
        // Draw code block header with language and copy button
        g2d.setColor(new Color(180, 180, 180));
        String headerText = "Code: " + language;
        g2d.drawString(headerText, x, y);
        
        // Draw a copy button
        int buttonWidth = 60;
        int buttonHeight = 18;
        int buttonX = x + width - buttonWidth;
        int buttonY = y - fm.getAscent();
        
        // Draw button background
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 5, 5);
        
        // Draw button border
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawRoundRect(buttonX, buttonY, buttonWidth, buttonHeight, 5, 5);
        
        // Draw button text
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString("Copy", buttonX + 18, buttonY + 13);
        
        // Store button location for mouse click handling
        codeBlockCopyButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);
        codeToClipboard = code;
        
        // Continue with code block rendering
        y += fm.getHeight() + 5;
        g2d.setFont(codeFont);
        
        // Draw code block background
        Color codeBlockBg = new Color(30, 30, 30);
        g2d.setColor(codeBlockBg);
        g2d.fillRect(x - 5, y - fm.getAscent(), width + 10, fm.getHeight() * (code.split("\n").length + 1));
        
        // Draw code with basic syntax highlighting
        String[] lines = code.split("\n");
        for (String line : lines) {
            // Apply simple syntax highlighting based on language
            drawSyntaxHighlightedLine(g2d, line, language, x, y, width);
            y += fm.getHeight();
        }
    }
    
    /**
     * Draw a single line of code with basic syntax highlighting
     */
    private void drawSyntaxHighlightedLine(Graphics2D g2d, String line, String language, int x, int y, int width) {
        FontMetrics fm = g2d.getFontMetrics();
        int availableWidth = width;
        
        // Simple syntax highlighting
        if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
            // Comments
            g2d.setColor(new Color(95, 145, 95)); // Green for comments
            g2d.drawString(line, x, y);
        } else {
            // Split the line into tokens for highlighting
            StringBuilder token = new StringBuilder();
            int currentX = x;
            
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                
                // Check if character is a delimiter
                if (c == ' ' || c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == ',' || c == '.') {
                    // Draw the accumulated token with appropriate color
                    if (token.length() > 0) {
                        g2d.setColor(getSyntaxColor(token.toString(), language));
                        g2d.drawString(token.toString(), currentX, y);
                        currentX += fm.stringWidth(token.toString());
                        token = new StringBuilder();
                    }
                    
                    // Draw the delimiter
                    g2d.setColor(new Color(200, 200, 200)); // Light gray for delimiters
                    g2d.drawString(String.valueOf(c), currentX, y);
                    currentX += fm.stringWidth(String.valueOf(c));
                } else {
                    token.append(c);
                }
            }
            
            // Draw any remaining token
            if (token.length() > 0) {
                g2d.setColor(getSyntaxColor(token.toString(), language));
                g2d.drawString(token.toString(), currentX, y);
            }
        }
    }
    
    /**
     * Get appropriate color for syntax highlighting based on token and language
     */
    private Color getSyntaxColor(String token, String language) {
        // Keywords for common languages
        String[] keywords = {"function", "var", "let", "const", "if", "else", "for", "while", "return", 
                            "class", "public", "private", "static", "void", "int", "string", "boolean",
                            "def", "import", "from", "true", "false", "null", "this", "new"};
        
        // Check if token is a keyword
        for (String keyword : keywords) {
            if (token.equals(keyword)) {
                return new Color(86, 156, 214); // Blue for keywords
            }
        }
        
        // Check if token is a number
        if (token.matches("\\d+(\\.\\d+)?")) {
            return new Color(181, 206, 168); // Light green for numbers
        }
        
        // Check if token is a string (starts and ends with quotes)
        if ((token.startsWith("\"") && token.endsWith("\"")) || 
            (token.startsWith("'") && token.endsWith("'"))) {
            return new Color(206, 145, 120); // Orange for strings
        }
        
        // Default color for other tokens
        return new Color(220, 220, 220); // Light gray for normal text
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
            // Check if this is a code block
            boolean isCodeBlock = false;
            String messageContent = message;
            
            if (message.startsWith("```")) {
                int firstNewline = message.indexOf('\n');
                int endCodeBlock = message.lastIndexOf("```");
                
                if (firstNewline > 3 && endCodeBlock > firstNewline) {
                    isCodeBlock = true;
                    messageContent = message.substring(firstNewline + 1, endCodeBlock);
                    
                    // Code blocks need more width
                    textWidth = Math.max(textWidth, 350);
                    // Add extra height for code blocks
                    lineCount += 2; // For header and footer
                }
            }
            
            // Calculate width and line count with word wrapping
            for (String line : messageContent.split("\n")) {
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
        
        int bubbleHeight = fm.getHeight() * Math.max(1, lineCount) + extraHeight + 8; // Reduced to 8px for timestamp
        
        // Use smaller spacing for system messages
        int spacing = isSystemMessage ? 2 : BUBBLE_SPACING;
        
        return new Dimension(bubbleWidth + 2 * spacing, bubbleHeight + 2 * spacing);
    }
}