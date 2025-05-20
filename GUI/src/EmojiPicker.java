import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom emoji picker component that displays a grid of emoji buttons.
 */
public class EmojiPicker extends JPanel {
    private final JTextField targetTextField;
    private final JPopupMenu popupMenu;
    
    // Map of emoji codes to their HTML entity representations
    private static final Map<String, String> EMOJI_MAP = new HashMap<>();
    
    static {
        // Smileys
        EMOJI_MAP.put(":smile:", "&#128522;");
        EMOJI_MAP.put(":laughing:", "&#128514;");
        EMOJI_MAP.put(":blush:", "&#128522;");
        EMOJI_MAP.put(":smiley:", "&#128515;");
        EMOJI_MAP.put(":relaxed:", "&#9786;");
        EMOJI_MAP.put(":smirk:", "&#128527;");
        EMOJI_MAP.put(":heart_eyes:", "&#128525;");
        EMOJI_MAP.put(":kissing_heart:", "&#128536;");
        EMOJI_MAP.put(":kissing_closed_eyes:", "&#128538;");
        EMOJI_MAP.put(":flushed:", "&#128563;");
        EMOJI_MAP.put(":relieved:", "&#128524;");
        EMOJI_MAP.put(":satisfied:", "&#128518;");
        EMOJI_MAP.put(":grin:", "&#128513;");
        EMOJI_MAP.put(":wink:", "&#128521;");
        EMOJI_MAP.put(":stuck_out_tongue_winking_eye:", "&#128540;");
        EMOJI_MAP.put(":stuck_out_tongue_closed_eyes:", "&#128541;");
        EMOJI_MAP.put(":grinning:", "&#128512;");
        EMOJI_MAP.put(":kissing:", "&#128535;");
        EMOJI_MAP.put(":kissing_smiling_eyes:", "&#128537;");
        EMOJI_MAP.put(":stuck_out_tongue:", "&#128539;");
        
        // Gestures and people
        EMOJI_MAP.put(":thumbsup:", "&#128077;");
        EMOJI_MAP.put(":thumbsdown:", "&#128078;");
        EMOJI_MAP.put(":ok_hand:", "&#128076;");
        EMOJI_MAP.put(":punch:", "&#128074;");
        EMOJI_MAP.put(":fist:", "&#9994;");
        EMOJI_MAP.put(":v:", "&#9996;");
        EMOJI_MAP.put(":wave:", "&#128075;");
        EMOJI_MAP.put(":hand:", "&#9995;");
        EMOJI_MAP.put(":open_hands:", "&#128080;");
        EMOJI_MAP.put(":point_up:", "&#9757;");
        EMOJI_MAP.put(":point_down:", "&#128071;");
        EMOJI_MAP.put(":point_left:", "&#128072;");
        EMOJI_MAP.put(":point_right:", "&#128073;");
        EMOJI_MAP.put(":raised_hands:", "&#128588;");
        EMOJI_MAP.put(":pray:", "&#128591;");
        EMOJI_MAP.put(":clap:", "&#128079;");
        
        // Objects and symbols
        EMOJI_MAP.put(":gift:", "&#127873;");
        EMOJI_MAP.put(":bell:", "&#128276;");
        EMOJI_MAP.put(":tada:", "&#127881;");
        EMOJI_MAP.put(":balloon:", "&#127880;");
        EMOJI_MAP.put(":cake:", "&#127874;");
        EMOJI_MAP.put(":heart:", "&#10084;");
        EMOJI_MAP.put(":broken_heart:", "&#128148;");
        EMOJI_MAP.put(":star:", "&#11088;");
        EMOJI_MAP.put(":sparkles:", "&#10024;");
        EMOJI_MAP.put(":zap:", "&#9889;");
        EMOJI_MAP.put(":boom:", "&#128165;");
        EMOJI_MAP.put(":fire:", "&#128293;");
        EMOJI_MAP.put(":sunny:", "&#9728;");
        EMOJI_MAP.put(":cloud:", "&#9729;");
        EMOJI_MAP.put(":umbrella:", "&#9748;");
        EMOJI_MAP.put(":coffee:", "&#9749;");
    }
    
    /**
     * Create an emoji picker that inserts emojis into the specified text field
     */
    public EmojiPicker(JTextField textField) {
        this.targetTextField = textField;
        
        // Create popup menu first to avoid initialization issues
        this.popupMenu = new JPopupMenu();
        
        setLayout(new GridLayout(8, 8, 2, 2));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setBackground(ThemeManager.getCurrentTheme().cardColor);
        
        // Create emoji buttons
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            // Convert HTML entity to actual Unicode character for display
            String unicodeEmoji = convertHtmlEntityToUnicode(entry.getValue());
            JButton emojiButton = new JButton(unicodeEmoji);
            emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            emojiButton.setFocusPainted(false);
            emojiButton.setBorderPainted(true);
            emojiButton.setContentAreaFilled(true);
            emojiButton.setToolTipText(entry.getKey());
            emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            emojiButton.addActionListener(e -> {
                int caretPosition = targetTextField.getCaretPosition();
                String currentText = targetTextField.getText();
                String newText = currentText.substring(0, caretPosition) + 
                                 entry.getKey() + 
                                 currentText.substring(caretPosition);
                targetTextField.setText(newText);
                targetTextField.setCaretPosition(caretPosition + entry.getKey().length());
                targetTextField.requestFocus();
                popupMenu.setVisible(false);
            });
            
            add(emojiButton);
        }
        
        // Add this panel to the popup menu
        popupMenu.add(this);
    }
    
    /**
     * Create a button that shows the emoji picker when clicked
     */
    public JButton createEmojiButton() {
        JButton emojiButton = new JButton("Emoji");
        emojiButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emojiButton.setFocusPainted(false);
        emojiButton.setBorderPainted(true);
        emojiButton.setContentAreaFilled(true);
        emojiButton.setToolTipText("Insert Emoji");
        emojiButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set fixed size to prevent layout issues
        emojiButton.setPreferredSize(new Dimension(80, 36));
        emojiButton.setMinimumSize(new Dimension(80, 36));
        emojiButton.setMaximumSize(new Dimension(80, 36));
        
        // Use mousePressed instead of mouseClicked for better responsiveness
        emojiButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Position the popup above the button
                popupMenu.show(emojiButton, 0, -popupMenu.getPreferredSize().height);
            }
        });
        
        return emojiButton;
    }
    
    /**
     * Process text to convert emoji codes to Unicode emojis
     */
    public static String processEmojis(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Replace emoji codes with actual Unicode characters
        StringBuilder result = new StringBuilder(text);
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            String emojiCode = entry.getKey();
            String htmlEntity = entry.getValue();
            
            // Convert HTML entity to actual Unicode character
            String unicodeEmoji = convertHtmlEntityToUnicode(htmlEntity);
            
            // Replace all occurrences in the string
            int index = result.indexOf(emojiCode);
            while (index != -1) {
                result.replace(index, index + emojiCode.length(), unicodeEmoji);
                index = result.indexOf(emojiCode, index + unicodeEmoji.length());
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convert HTML entity to actual Unicode character
     */
    private static String convertHtmlEntityToUnicode(String htmlEntity) {
        if (htmlEntity.startsWith("&#") && htmlEntity.endsWith(";")) {
            try {
                // Extract the numeric part
                String numericPart = htmlEntity.substring(2, htmlEntity.length() - 1);
                int codePoint = Integer.parseInt(numericPart);
                return new String(Character.toChars(codePoint));
            } catch (NumberFormatException e) {
                return htmlEntity;
            }
        }
        return htmlEntity;
    }
}