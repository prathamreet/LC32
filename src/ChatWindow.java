import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Swing-based chat window. 
 * Sends via MulticastManager, displays decrypted messages.
 */
public class ChatWindow {
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final MulticastManager multicastManager;

    public ChatWindow(String nickname) {
        multicastManager = new MulticastManager(nickname, this);

        // Build UI
        JFrame frame = new JFrame("LAN Chat – " + nickname);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener((ActionEvent e) -> {
            String txt = messageField.getText().trim();
            if (!txt.isEmpty()) {
                multicastManager.sendMessage(txt);
                messageField.setText("");
            }
        });
        frame.add(messageField, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Start background receive
        new Thread(multicastManager::receiveMessages, "Receiver-Thread").start();
    }

    /** Appends one line of text to the chat area. */
    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }
}
