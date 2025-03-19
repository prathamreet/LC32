import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatWindow {
    private JTextArea chatArea;
    private JTextField messageField;
    private String nickname;
    private MulticastManager multicastManager;

    public ChatWindow(String nickname) {
        this.nickname = nickname;
        multicastManager = new MulticastManager(nickname, this);
        setupUI();
        new Thread(multicastManager::receiveMessages).start();
    }

    private void setupUI() {
        JFrame frame = new JFrame("LAN Chat - " + nickname);
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                multicastManager.sendMessage(messageField.getText());
                messageField.setText("");
            }
        });
        frame.add(messageField, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }
}
