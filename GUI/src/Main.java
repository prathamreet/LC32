import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Main entry point for the LAN Chat application.
 * This class handles the initial setup and login screen.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class Main {
    /**
     * The main method that starts our chat application.
     * It applies the theme and shows the login dialog.
     */
    public static void main(String[] args) {
        // First, we need to set up our app's look and feel
        ThemeManager.applyTheme();
        
        // Now let's show the login screen where users can enter their nickname
        SwingUtilities.invokeLater(Main::showLoginDialog);
    }
    
    /**
     * Creates and displays a splash screen with app info.
     * Note: Currently not used but kept for future use.
     */
    private static void showSplashScreen() {
        // Create a window without borders for our splash screen
        JWindow splashScreen = new JWindow();
        splashScreen.setSize(500, 300);
        splashScreen.setLocationRelativeTo(null); // Center on screen
        
        // Make a cool gradient background for our splash screen
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create a smooth color transition from top to bottom
                GradientPaint gradient = new GradientPaint(
                    0, 0, ThemeManager.getCurrentTheme().primaryColor,
                    getWidth(), getHeight(), ThemeManager.getCurrentTheme().secondaryColor
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        splashPanel.setLayout(new BorderLayout());
        
        // Add our app title with a big, bold font
        JLabel titleLabel = new JLabel("LAN Chat", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        
        // Add a subtitle explaining what our app does
        JLabel subtitleLabel = new JLabel("Secure P2P Communication", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(240, 240, 240));
        
        // Show the current version number
        JLabel versionLabel = new JLabel("Version 1.0", JLabel.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(220, 220, 220));
        
        // Add a loading bar to show the app is starting up
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Continuous animation
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(255, 255, 255, 50));
        progressBar.setForeground(Color.WHITE);
        
        // Arrange all our text elements in the center
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.setOpaque(false); // Make it transparent
        centerPanel.add(titleLabel);
        centerPanel.add(subtitleLabel);
        centerPanel.add(versionLabel);
        
        // Add everything to our splash panel
        splashPanel.add(centerPanel, BorderLayout.CENTER);
        splashPanel.add(progressBar, BorderLayout.SOUTH);
        
        // Set the panel as the content of our splash window
        splashScreen.setContentPane(splashPanel);
        splashScreen.setVisible(true);
        
        // Close the splash screen after 1.5 seconds
        Timer timer = new Timer(1500, e -> splashScreen.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Creates and displays the login dialog where users enter their nickname.
     * This is the first screen users see when starting the app.
     */
    private static void showLoginDialog() {
        // Create a custom dialog with no window borders
        JDialog loginDialog = new JDialog((Frame) null, "LAN Chat Login", true);
        loginDialog.setSize(450, 350);
        loginDialog.setLocationRelativeTo(null); // Center on screen
        loginDialog.setResizable(false);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setUndecorated(true); // Remove standard window borders
        
        // Create a nice-looking panel with shadows for our content
        JPanel mainPanel = ThemeManager.createCardPanel();
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Create a header with our app logo and name
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);
        
        // Create a circular logo with "LC" text
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a blue circle for our logo
                g2d.setColor(ThemeManager.getCurrentTheme().primaryColor);
                g2d.fillOval(0, 0, 60, 60);
                
                // Add "LC" text in the center of the circle
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 24));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "LC";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();
                g2d.drawString(text, (60 - textWidth) / 2, 30 + textHeight / 4);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, 60);
            }
        };
        logoPanel.setOpaque(false);
        
        // Create a panel for the app title and subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        
        // Add the app title
        JLabel titleLabel = new JLabel("LAN Chat");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getCurrentTheme().primaryColor);
        
        // Add a subtitle explaining what the app does
        JLabel subtitleLabel = new JLabel("Connect with peers on your local network");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(ThemeManager.getCurrentTheme().textSecondaryColor);
        
        // Add title and subtitle to the panel
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Combine logo and title in a single panel
        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.add(logoPanel);
        logoTitlePanel.add(titlePanel);
        
        // Add the combined panel to the header
        headerPanel.add(logoTitlePanel, BorderLayout.CENTER);
        
        // Create a panel for the nickname input field
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        // Add a label for the nickname field
        JLabel nicknameLabel = new JLabel("Enter your nickname:");
        nicknameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nicknameLabel.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        // Create a styled text field for entering the nickname
        JTextField nicknameField = ThemeManager.createThemedTextField();
        
        // Add the label and field to the input panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        inputPanel.add(nicknameLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(nicknameField, gbc);
        
        // We're only using dark mode, so no theme selector needed
        
        // Add some space before the buttons
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 0, 5, 0);
        
        // Create a panel for our buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        // Create a button to join the chat
        JButton loginButton = ThemeManager.createThemedButton("Join Chat", true);
        loginButton.setPreferredSize(new Dimension(120, 40));
        
        // Create a button to cancel and exit
        JButton cancelButton = ThemeManager.createThemedButton("Cancel", false);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        
        // Add both buttons to the panel
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        // Add a close button in the top-right corner
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.setForeground(ThemeManager.getCurrentTheme().textSecondaryColor);
        closeButton.setBorderPainted(true);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            loginDialog.dispose();
            System.exit(0);
        });
        
        // Create a panel for the top section with the close button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeButton, BorderLayout.EAST);
        
        // Add all components to the main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Make the dialog draggable by clicking and dragging anywhere
        new ComponentMover(loginDialog, mainPanel);
        
        // Set the main panel as the content of our dialog
        loginDialog.add(mainPanel);
        
        // Add functionality to the login button
        loginButton.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            if (!nickname.isEmpty()) {
                // If we have a valid nickname, close the login and open the chat
                loginDialog.dispose();
                new ChatWindow(nickname);
            } else {
                // Show an error if no nickname was entered
                JOptionPane.showMessageDialog(loginDialog, 
                        "Please enter a valid nickname", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add functionality to the cancel button
        cancelButton.addActionListener(e -> {
            loginDialog.dispose();
            System.exit(0);
        });
        
        // Allow pressing Enter to submit the form
        nicknameField.addActionListener(e -> loginButton.doClick());
        
        // Show the dialog and focus on the nickname field
        loginDialog.setVisible(true);
        nicknameField.requestFocus();
    }
    
    /**
     * A helper class that makes a window draggable.
     * This lets users move the login dialog by clicking and dragging.
     */
    private static class ComponentMover extends MouseAdapter {
        private final Component component;    // The component to move (our dialog)
        private final Component dragSource;   // The component to drag from (our panel)
        private Point dragStart;              // Where the drag started
        
        /**
         * Creates a new component mover.
         * 
         * @param component The window to move
         * @param dragSource The panel to drag from
         */
        public ComponentMover(Component component, Component dragSource) {
            this.component = component;
            this.dragSource = dragSource;
            dragSource.addMouseListener(this);
            dragSource.addMouseMotionListener(this);
        }
        
        /**
         * Records where the mouse was pressed to start dragging.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }
        
        /**
         * Moves the window as the mouse is dragged.
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragStart != null) {
                // Calculate how far the mouse has moved
                Point location = component.getLocation();
                Point newLocation = new Point(
                    location.x + e.getX() - dragStart.x,
                    location.y + e.getY() - dragStart.y
                );
                // Move the window to the new location
                component.setLocation(newLocation);
            }
        }
    }
}
