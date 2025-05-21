import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * The ThemeManager handles all the visual styling for our chat app.
 * It makes sure everything looks consistent and modern with a dark theme.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class ThemeManager {
    // Different theme options (though we only use dark theme for now)
    public static final String LIGHT_THEME = "light";
    public static final String DARK_THEME = "dark";
    public static final String MATERIAL_THEME = "material";
    
    // We're always using the dark theme in this version
    private static String currentTheme = DARK_THEME;
    
    // This helps remember the user's theme choice between app launches
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String THEME_PREF_KEY = "app_theme";
    
    // This map stores all our color schemes
    private static final Map<String, ColorScheme> themes = new HashMap<>();
    
    // When the class is loaded, set up our themes
    static {
        // Create all the color schemes
        initializeThemes();
        
        // Always use dark theme for this version
        currentTheme = DARK_THEME;
        // Save this choice to preferences
        prefs.put(THEME_PREF_KEY, DARK_THEME);
    }
    
    /**
     * Sets up all the color schemes for our different themes.
     * Currently we only use the dark theme.
     */
    private static void initializeThemes() {
        // Create our dark theme color scheme
        ColorScheme darkScheme = new ColorScheme();
        darkScheme.primaryColor = new Color(33, 150, 243);         // A nice blue for main elements
        darkScheme.secondaryColor = new Color(76, 175, 80);        // Green for accents and highlights
        darkScheme.accentColor = new Color(255, 152, 0);           // Orange for special elements
        darkScheme.backgroundColor = new Color(33, 33, 33);        // Dark gray for backgrounds
        darkScheme.cardColor = new Color(66, 66, 66);              // Medium gray for cards and panels
        darkScheme.textPrimaryColor = new Color(255, 255, 255);    // White for main text
        darkScheme.textSecondaryColor = new Color(189, 189, 189);  // Light gray for secondary text
        darkScheme.dividerColor = new Color(97, 97, 97);           // Gray for borders and dividers
        darkScheme.myMessageColor = new Color(55, 71, 79);         // Dark blue-gray for my messages
        darkScheme.otherMessageColor = new Color(66, 66, 66);      // Medium gray for others' messages
        darkScheme.systemMessageColor = new Color(38, 50, 56);     // Dark blue-gray for system messages
        themes.put(DARK_THEME, darkScheme);
        
        // We're not using these themes, but we add references to avoid errors
        themes.put(LIGHT_THEME, darkScheme);
        themes.put(MATERIAL_THEME, darkScheme);
    }
    
    /**
     * Gets the current color scheme being used.
     * 
     * @return The current theme's color scheme
     */
    public static ColorScheme getCurrentTheme() {
        return themes.get(currentTheme);
    }
    
    /**
     * Gets the name of the current theme.
     * 
     * @return The current theme name (e.g., "dark")
     */
    public static String getCurrentThemeName() {
        return currentTheme;
    }
    
    /**
     * Changes the application theme.
     * Note: In this version, we always use the dark theme.
     * 
     * @param themeName The name of the theme to use
     */
    public static void setTheme(String themeName) {
        if (themes.containsKey(themeName)) {
            // Remember the old theme to check if we need to update
            String oldTheme = currentTheme;
            
            // Set the new theme
            currentTheme = themeName;
            
            // Save the choice for next time
            prefs.put(THEME_PREF_KEY, themeName);
            
            // Only apply the theme if it's different from the current one
            if (!oldTheme.equals(themeName)) {
                System.out.println("Changing theme from " + oldTheme + " to " + themeName);
                applyTheme();
            }
        } else {
            System.err.println("Theme not found: " + themeName);
        }
    }
    
    /**
     * Applies the current theme to the entire user interface.
     * This updates colors, fonts, and styles for all components.
     */
    public static void applyTheme() {
        ColorScheme scheme = getCurrentTheme();
        
        try {
            // Use the cross-platform look and feel for consistency
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            // Make fonts look smoother and nicer
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            
            // Set background colors for different components
            UIManager.put("Panel.background", scheme.backgroundColor);
            UIManager.put("TextPane.background", scheme.backgroundColor);
            UIManager.put("TextField.background", scheme.cardColor);
            UIManager.put("TextArea.background", scheme.cardColor);
            UIManager.put("List.background", scheme.cardColor);
            UIManager.put("ComboBox.background", scheme.cardColor);
            
            // Set text colors for different components
            UIManager.put("Panel.foreground", scheme.textPrimaryColor);
            UIManager.put("Label.foreground", scheme.textPrimaryColor);
            UIManager.put("TextField.foreground", scheme.textPrimaryColor);
            UIManager.put("TextArea.foreground", scheme.textPrimaryColor);
            UIManager.put("List.foreground", scheme.textPrimaryColor);
            UIManager.put("ComboBox.foreground", scheme.textPrimaryColor);
            
            // Make buttons look nicer
            UIManager.put("Button.background", scheme.primaryColor);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focusPainted", false);
            UIManager.put("Button.borderPainted", true);
            UIManager.put("Button.margin", new Insets(5, 10, 5, 10));
            UIManager.put("Button.select", Utils.darken(scheme.primaryColor, 0.1f));
            
            // Improve scroll pane appearance
            UIManager.put("ScrollPane.background", scheme.backgroundColor);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbDarkShadow", scheme.backgroundColor);
            UIManager.put("ScrollBar.thumbHighlight", scheme.primaryColor);
            UIManager.put("ScrollBar.thumbShadow", scheme.primaryColor);
            UIManager.put("ScrollBar.track", scheme.backgroundColor);
            
            // Make split panes look better
            UIManager.put("SplitPane.dividerSize", 5);
            UIManager.put("SplitPane.background", scheme.backgroundColor);
            UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
            
            // Update all open windows with the new theme
            for (Window window : Window.getWindows()) {
                try {
                    // Update all components in the window
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
     * Updates all components in a container recursively.
     * This ensures the theme is applied to every component.
     * 
     * @param component The component to update
     */
    private static void updateComponentTreeRecursively(Component component) {
        // If it's a Swing component, update it
        if (component instanceof JComponent) {
            JComponent jcomp = (JComponent) component;
            jcomp.invalidate();
            jcomp.validate();
            jcomp.repaint();
        }
        
        // If it's a container, update all its children too
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                updateComponentTreeRecursively(child);
            }
        }
    }
    
    /**
     * Creates a nice-looking button with our theme colors.
     * 
     * @param text The text to display on the button
     * @param isPrimary Whether this is a primary action button
     * @return A styled JButton
     */
    public static JButton createThemedButton(String text, boolean isPrimary) {
        ColorScheme scheme = getCurrentTheme();
        
        // Choose colors based on whether it's a primary button
        Color bgColor = isPrimary ? scheme.primaryColor : scheme.cardColor;
        Color fgColor = isPrimary ? Color.WHITE : scheme.textPrimaryColor;
        
        // Create and style the button
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(!isPrimary);
        
        // Add a border for secondary buttons
        if (!isPrimary) {
            button.setBorder(BorderFactory.createLineBorder(scheme.dividerColor, 1));
        }
        
        // Use a hand cursor to show it's clickable
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add a hover effect to make the button more interactive
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Darken the button when hovered
                button.setBackground(Utils.darken(bgColor, 0.1f));
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
     * Creates a nice-looking text field with our theme colors.
     * 
     * @return A styled JTextField
     */
    public static JTextField createThemedTextField() {
        ColorScheme scheme = getCurrentTheme();
        
        // Create and style the text field
        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(scheme.cardColor);
        textField.setForeground(scheme.textPrimaryColor);
        textField.setCaretColor(scheme.primaryColor);
        
        // Add a nice border with padding
        textField.setBorder(new CompoundBorder(
                new LineBorder(scheme.dividerColor, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        
        // Set size to ensure it's visible and usable
        textField.setMinimumSize(new Dimension(100, 36));
        textField.setPreferredSize(new Dimension(200, 36));
        
        // Make sure the background is visible
        textField.setOpaque(true);
        
        return textField;
    }
    
    /**
     * Creates a panel with a card-like appearance and shadow.
     * 
     * @return A styled JPanel with shadow border
     */
    public static JPanel createCardPanel() {
        ColorScheme scheme = getCurrentTheme();
        
        // Create the panel with our card color
        JPanel panel = new JPanel();
        panel.setBackground(scheme.cardColor);
        
        // Add a shadow border and some padding
        panel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(5, 2, 0.2f),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        return panel;
    }
    
    /**
     * A class that holds all the colors for a theme.
     * This makes it easy to switch between different color schemes.
     */
    public static class ColorScheme {
        public Color primaryColor;       // Main color for important elements
        public Color secondaryColor;     // Secondary color for accents
        public Color accentColor;        // Accent color for highlights
        public Color backgroundColor;    // Background color for the app
        public Color cardColor;          // Color for cards and panels
        public Color textPrimaryColor;   // Color for main text
        public Color textSecondaryColor; // Color for less important text
        public Color dividerColor;       // Color for borders and dividers
        public Color myMessageColor;     // Color for my chat bubbles
        public Color otherMessageColor;  // Color for others' chat bubbles
        public Color systemMessageColor; // Color for system messages
    }
    
    /**
     * A custom border that creates a shadow effect around components.
     * This makes panels look like they're floating above the background.
     */
    public static class ShadowBorder extends AbstractBorder {
        private final int shadowSize;     // How big the shadow is
        private final int cornerRadius;   // How rounded the corners are
        private final float shadowOpacity; // How dark the shadow is
        
        /**
         * Creates a new shadow border.
         * 
         * @param shadowSize How big the shadow should be
         * @param cornerRadius How rounded the corners should be
         * @param shadowOpacity How dark the shadow should be (0-1)
         */
        public ShadowBorder(int shadowSize, int cornerRadius, float shadowOpacity) {
            this.shadowSize = shadowSize;
            this.cornerRadius = cornerRadius;
            this.shadowOpacity = shadowOpacity;
        }
        
        /**
         * Draws the shadow border around a component.
         */
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw multiple lines with decreasing opacity to create a shadow effect
            for (int i = 0; i < shadowSize; i++) {
                // Calculate opacity for this layer of the shadow
                float opacity = shadowOpacity * (shadowSize - i) / shadowSize;
                g2.setColor(new Color(0, 0, 0, (int) (opacity * 255)));
                
                // Draw a rounded rectangle for this layer
                g2.drawRoundRect(x + i, y + i, width - i * 2 - 1, height - i * 2 - 1, cornerRadius, cornerRadius);
            }
            
            g2.dispose();
        }
        
        /**
         * Returns the space needed for the border.
         */
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(shadowSize, shadowSize, shadowSize, shadowSize);
        }
        
        /**
         * Updates the provided insets with the space needed for the border.
         */
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = shadowSize;
            return insets;
        }
    }
}