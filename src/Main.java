import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String nickname = JOptionPane.showInputDialog("Enter your nickname:");
        if (nickname != null && !nickname.trim().isEmpty()) {
            new ChatWindow(nickname);
        } else {
            System.exit(0);
        }
    }
}
