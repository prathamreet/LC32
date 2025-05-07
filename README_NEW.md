# LAN Chat Application

A modern, secure P2P chat application for local area networks with an enhanced UI.

## Features

- Real-time messaging over local network using multicast
- Modern, customizable UI with multiple themes (Light, Dark, Material)
- Secure communication with AES encryption
- Emoji support and custom emoticons
- User presence detection
- Message bubbles with timestamps
- System notifications
- Customizable appearance

## Requirements

- Java 8 or higher
- Local network with multicast support

## How to Run

1. Compile the Java files:
   ```
   javac src/*.java -d out
   ```

2. Change to the output directory:
   ```
   cd out
   ```

3. Run the application:
   ```
   java Main
   ```

4. Enter your nickname in the login dialog and click "Join Chat"

5. Start chatting with other users on your local network

## Technical Details

- Built with Java Swing for the UI
- Uses UDP multicast for peer discovery and messaging
- Implements AES encryption for secure communication
- Custom UI components for modern look and feel
- Supports themes and appearance customization

## Project Structure

- `src/Main.java` - Application entry point and login dialog
- `src/ChatWindow.java` - Main chat window implementation
- `src/ChatPanel.java` - Custom panel for displaying chat messages
- `src/MulticastManager.java` - Handles network communication
- `src/EncryptionUtils.java` - Provides encryption/decryption functionality
- `src/ThemeManager.java` - Manages application themes and styling
- `src/Utils.java` - Utility methods for UI and formatting
- `src/UserListRenderer.java` - Custom renderer for the user list
- `src/EmojiPicker.java` - Emoji selection and insertion
- `src/ChatBubbleRenderer.java` - Custom renderer for chat bubbles