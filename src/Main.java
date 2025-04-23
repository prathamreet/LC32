import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        
        // Prompt user once for a nickname before launching the chat UI
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your nickname: ");
        String nickname = scanner.nextLine().trim();

        if (!nickname.isEmpty()) {
            new ChatWindow(nickname);
        } else {
            System.out.println("Nickname cannot be empty. Exiting.");
            System.exit(0);
        }

        scanner.close();
    }
}