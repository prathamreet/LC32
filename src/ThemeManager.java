import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Manages application themes and styling.
 * Provides light and dark mode with consistent color schemes.
 */
public class ThemeManager {
    // Theme types
    public static final String LIGHT_THEME = "light";
    public static final String DARK_THEME = "dark";
    public static final String MATERIAL_THEME = "material";
    
    // Current theme - always use dark theme
    private static String currentTheme = DARK_THEME;
    
    // Preferences for saving user theme choice
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String THEME_PREF_KEY = "app_theme";
    
    // Theme color maps
    private static final Map<String, ColorScheme> themes = new HashMap<>();
    
    static {
        // Initialize themes
        initializeThemes();
        
        // Always use dark theme
        currentTheme = DARK_THEME;
        // Save to preferences
        prefs.put(THEME_PREF_KEY, DARK_THEME);
    }
    
    /**
     * Initialize theme color schemes
     */
    private static void initializeThemes() {
        // Only dark theme is used
        ColorScheme darkScheme = new ColorScheme();
        darkScheme.primaryColor = new Color(33, 150, 243);         // Blue
        darkScheme.secondaryColor = new Color(76, 175, 80);        // Green
        darkScheme.accentColor = new Color(255, 152, 0);           // Orange
        darkScheme.backgroundColor = new Color(33, 33, 33);        // Dark Gray
        darkScheme.cardColor = new Color(66, 66, 66);              // Medium Gray
        darkScheme.textPrimaryColor = new Color(255, 255, 255);    // White
        darkScheme.textSecondaryColor = new Color(189, 189, 189);  // Light Gray
        darkScheme.dividerColor = new Color(97, 97, 97);           // Gray
        darkScheme.myMessageColor = new Color(55, 71, 79);         // Dark Blue Gray
        darkScheme.otherMessageColor = new Color(66, 66, 66);      // Medium Gray
        darkScheme.systemMessageColor = new Color(38, 50, 56);     // Dark Blue Gray
        themes.put(DARK_THEME, darkScheme);
        
        // Add references to other themes to avoid errors, but they're not used
        themes.put(LIGHT_THEME, darkScheme);
        themes.put(MATERIAL_THEME, darkScheme);
    }
    
    /**
     * Get the current color scheme
     */
    public static ColorScheme getCurrentTheme() {
        return themes.get(currentTheme);
    }
    
    /**
     * Get the current theme name
     */
    public static String getCurrentThemeName() {
        return currentTheme;
    }
    
    /**
     * Set the application theme
     */
    public static void setTheme(String themeName) {
        if (themes.containsKey(themeName)) {
            // Store the old theme for comparison
            String oldTheme = currentTheme;
            
            // Set the new theme
            currentTheme = themeName;
            
            // Save to preferences
            prefs.put(THEME_PREF_KEY, themeName);
            
            // Only apply if the theme actually changed
            if (!oldTheme.equals(themeName)) {
                System.out.println("Changing theme from " + oldTheme + " to " + themeName);
                applyTheme();
            }
        } else {
            System.err.println("Theme not found: " + themeName);
        }
    }
    
    /**
     * Apply the current theme to the UI
     */
    public static void applyTheme() {
        ColorScheme scheme = getCurrentTheme();
        
        // Set look and feel
        try {
            // Use cross-platform look and feel for consistency
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            // Fix font rendering
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Set UI colors
            UIManager.put("Panel.background", scheme.backgroundColor);
            UIManager.put("TextPane.background", scheme.backgroundColor);
            UIManager.put("TextField.background", scheme.cardColor);
            UIManager.put("TextArea.background", scheme.cardColor);
            UIManager.put("List.background", scheme.cardColor);
            UIManager.put("ComboBox.background", scheme.cardColor);
            
            UIManager.put("Panel.foreground", scheme.textPrimaryColor);
            UIManager.put("Label.foreground", scheme.textPrimaryColor);
            UIManager.put("TextField.foreground", scheme.textPrimaryColor);
            UIManager.put("TextArea.foreground", scheme.textPrimaryColor);
            UIManager.put("List.foreground", scheme.textPrimaryColor);
            UIManager.put("ComboBox.foreground", scheme.textPrimaryColor);
            
            // Fix button appearance
            UIManager.put("Button.background", scheme.primaryColor);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusPainted", false);
            UIManager.put("Button.borderPainted", true);
            UIManager.put("Button.margin", new Insets(5, 10, 5, 10));
            UIManager.put("Button.select", Utils.darken(scheme.primaryColor, 0.1f));
            
            // Fix scroll pane appearance
            UIManager.put("ScrollPane.background", scheme.backgroundColor);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbDarkShadow", scheme.backgroundColor);
            UIManager.put("ScrollBar.thumbHighlight", scheme.primaryColor);
            UIManager.put("ScrollBar.thumbShadow", scheme.primaryColor);
            UIManager.put("ScrollBar.track", scheme.backgroundColor);
            
            // Fix split pane appearance
            UIManager.put("SplitPane.dividerSize", 5);
            UIManager.put("SplitPane.background", scheme.backgroundColor);
            UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
            
            // Update all open windows
            for (Window window : Window.getWindows()) {
                try {
                    // Update the component tree UI
                    SwingUtilities.updateComponentTreeUI(window);
                    
                    // Force a complete repaint
                    window.invalidate();
                    window.validate();
                    window.repaint();
                    
                    // Update all child components
                    for (Component comp : window.getComponents()) {
                        updateComponentTreeRecursively(comp);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating UI for window: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Recursively update all components in the hierarchy
     */
    private static void updateComponentTreeRecursively(Component component) {
        if (component instanceof JComponent) {
            JComponent jcomp = (JComponent) component;
            jcomp.invalidate();
            jcomp.validate();
            jcomp.repaint();
        }
        
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                updateComponentTreeRecursively(child);
            }
        }
    }
    
    /**
     * Create a styled button with the current theme
     */
    public static JButton createThemedButton(String text, boolean isPrimary) {
        ColorScheme scheme = getCurrentTheme();
        Color bgColor = isPrimary ? scheme.primaryColor : scheme.cardColor;
        Color fgColor = isPrimary ? Color.WHITE : scheme.textPrimaryColor;
        
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(!isPrimary);
        if (!isPrimary) {
            button.setBorder(BorderFactory.createLineBorder(scheme.dividerColor, 1));
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Utils.darken(bgColor, 0.1f));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    /**
     * Create a styled text field with the current theme
     */
    public static JTextField createThemedTextField() {
        ColorScheme scheme = getCurrentTheme();
        
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(scheme.cardColor);
        textField.setForeground(scheme.textPrimaryColor);
        textField.setCaretColor(scheme.primaryColor);
        
        // Use a simpler border that's more visible
        textField.setBorder(new CompoundBorder(
                new LineBorder(scheme.dividerColor, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        
        // Set minimum and preferred size to ensure visibility
        textField.setMinimumSize(new Dimension(100, 36));
        textField.setPreferredSize(new Dimension(200, 36));
        
        // Make sure the text field is opaque
        textField.setOpaque(true);
        
        return textField;
    }
    
    /**
     * Create a material design card panel
     */
    public static JPanel createCardPanel() {
        ColorScheme scheme = getCurrentTheme();
        
        JPanel panel = new JPanel();
        panel.setBackground(scheme.cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(5, 2, 0.2f),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        return panel;
    }
    
    /**
     * Color scheme class to hold theme colors
     */
    public static class ColorScheme {
        public Color primaryColor;
        public Color secondaryColor;
        public Color accentColor;
        public Color backgroundColor;
        public Color cardColor;
        public Color textPrimaryColor;
        public Color textSecondaryColor;
        public Color dividerColor;
        public Color myMessageColor;
        public Color otherMessageColor;
        public Color systemMessageColor;
    }
    
    /**
     * Custom border that creates a shadow effect
     */
    public static class ShadowBorder extends AbstractBorder {
        private final int shadowSize;
        private final int cornerRadius;
        private final float shadowOpacity;
        
        public ShadowBorder(int shadowSize, int cornerRadius, float shadowOpacity) {
            this.shadowSize = shadowSize;
            this.cornerRadius = cornerRadius;
            this.shadowOpacity = shadowOpacity;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw shadow
            for (int i = 0; i < shadowSize; i++) {
                float opacity = shadowOpacity * (shadowSize - i) / shadowSize;
                g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));
                g2.drawRoundRect(x + i, y + i, width - i * 2 - 1, height - i * 2 - 1, cornerRadius, cornerRadius);
            }
            
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = shadowSize;
            return insets;
        }
    }
}