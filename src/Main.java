import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Main {
    public static void main(String[] args) {
        // Apply the theme before creating any UI components
        ThemeManager.applyTheme();
        
        // Show login dialog directly without splash screen
        SwingUtilities.invokeLater(Main::showLoginDialog);
    }
    
    private static void showSplashScreen() {
        // Create a splash screen window
        JWindow splashScreen = new JWindow();
        splashScreen.setSize(500, 300);
        splashScreen.setLocationRelativeTo(null);
        
        // Create splash panel with gradient background
        JPanel splashPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
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
        
        // App logo/title
        JLabel titleLabel = new JLabel("LAN Chat", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Secure P2P Communication", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(240, 240, 240));
        
        // Version info
        JLabel versionLabel = new JLabel("Version 1.0", JLabel.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(220, 220, 220));
        
        // Progress indicator
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(255, 255, 255, 50));
        progressBar.setForeground(Color.WHITE);
        
        // Layout components
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel);
        centerPanel.add(subtitleLabel);
        centerPanel.add(versionLabel);
        
        splashPanel.add(centerPanel, BorderLayout.CENTER);
        splashPanel.add(progressBar, BorderLayout.SOUTH);
        
        splashScreen.setContentPane(splashPanel);
        splashScreen.setVisible(true);
        
        // Close splash after a delay
        Timer timer = new Timer(1500, e -> splashScreen.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    private static void showLoginDialog() {
        // Create custom login dialog with rounded corners
        JDialog loginDialog = new JDialog((Frame) null, "LAN Chat Login", true);
        loginDialog.setSize(450, 350);
        loginDialog.setLocationRelativeTo(null);
        loginDialog.setResizable(false);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setUndecorated(true); // Remove window decorations for custom look
        
        // Create main panel with shadow border
        JPanel mainPanel = ThemeManager.createCardPanel();
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Header panel with app logo and name
        JPanel headerPanel = new JPanel(new BorderLayout(0, 10));
        headerPanel.setOpaque(false);
        
        // App logo (using a simple colored circle as logo)
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circular logo
                g2d.setColor(ThemeManager.getCurrentTheme().primaryColor);
                g2d.fillOval(0, 0, 60, 60);
                
                // Draw "LC" text
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
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("LAN Chat");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeManager.getCurrentTheme().primaryColor);
        
        JLabel subtitleLabel = new JLabel("Connect with peers on your local network");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(ThemeManager.getCurrentTheme().textSecondaryColor);
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        JPanel logoTitlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.add(logoPanel);
        logoTitlePanel.add(titlePanel);
        
        headerPanel.add(logoTitlePanel, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        JLabel nicknameLabel = new JLabel("Enter your nickname:");
        nicknameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nicknameLabel.setForeground(ThemeManager.getCurrentTheme().textPrimaryColor);
        
        JTextField nicknameField = ThemeManager.createThemedTextField();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        inputPanel.add(nicknameLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(nicknameField, gbc);
        
        // No theme selector as we only use dark mode
        
        // Skip to next grid position
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 0, 5, 0);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton loginButton = ThemeManager.createThemedButton("Join Chat", true);
        loginButton.setPreferredSize(new Dimension(120, 40));
        
        JButton cancelButton = ThemeManager.createThemedButton("Cancel", false);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        // Close button in top-right corner
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
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(closeButton, BorderLayout.EAST);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Make the dialog draggable
        new ComponentMover(loginDialog, mainPanel);
        
        loginDialog.add(mainPanel);
        
        // Action listeners
        loginButton.addActionListener(e -> {
            String nickname = nicknameField.getText().trim();
            if (!nickname.isEmpty()) {
                loginDialog.dispose();
                new ChatWindow(nickname);
            } else {
                JOptionPane.showMessageDialog(loginDialog, 
                        "Please enter a valid nickname", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> {
            loginDialog.dispose();
            System.exit(0);
        });
        
        // Handle Enter key
        nicknameField.addActionListener(e -> loginButton.doClick());
        
        // Show dialog and focus on text field
        loginDialog.setVisible(true);
        nicknameField.requestFocus();
    }
    
    /**
     * Utility class to make a component draggable
     */
    private static class ComponentMover extends MouseAdapter {
        private final Component component;
        private final Component dragSource;
        private Point dragStart;
        
        public ComponentMover(Component component, Component dragSource) {
            this.component = component;
            this.dragSource = dragSource;
            dragSource.addMouseListener(this);
            dragSource.addMouseMotionListener(this);
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragStart != null) {
                Point location = component.getLocation();
                Point newLocation = new Point(
                    location.x + e.getX() - dragStart.x,
                    location.y + e.getY() - dragStart.y
                );
                component.setLocation(newLocation);
            }
        }
    }
}
