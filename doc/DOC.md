# LAN Chat Application Documentation

## Problem Statement
In many local network environments, such as offices, classrooms, or small communities, there is a need for a simple, secure, and efficient way to communicate without relying on external servers or internet connectivity. Existing solutions often require complex setup, lack privacy features, or are not tailored for small-scale, peer-to-peer communication. This project addresses the need for a lightweight, easy-to-use chat application that enables real-time messaging within a LAN while ensuring message confidentiality through encryption.

## Outcomes
The LAN Chat Application delivers the following outcomes:
- **Real-Time Communication**: Users can send and receive messages instantly within the local network.
- **Public and Private Messaging**: Supports both broadcast messages to all users and private messages to specific individuals.
- **User Presence Tracking**: Maintains an up-to-date list of active users based on periodic presence announcements.
- **Message Encryption**: Ensures privacy by encrypting all messages using AES, preventing unauthorized access to communication.
- **Command-Line Interface**: Provides a simple, text-based interface for ease of use and minimal resource consumption.
- **Cross-Platform Compatibility**: Built with Java, the application runs on any platform supporting the Java Runtime Environment (JRE).

## Details of the Java Program
The LAN Chat Application is implemented in Java and consists of the following key components:

1. **EncryptionUtils.java**:
   - Handles AES encryption and decryption of messages.
   - Uses a 128-bit key for secure communication (hardcoded for simplicity; in production, a secure key exchange mechanism should be used).

2. **MulticastManager.java**:
   - Manages multicast communication for sending and receiving messages.
   - Sends encrypted messages to the multicast group (e.g., 230.0.0.1:5000).
   - Receives and decrypts messages, filtering them based on message type (public, private, or presence).

3. **ChatWindow.java**:
   - Provides the user interface via the command-line.
   - Handles user input, including commands like `/exit`, `/users`, `/pm`, and `/help`.
   - Displays messages with color-coded nicknames and timestamps.
   - Manages the active user list based on presence messages.

4. **Main.java** (or directly in ChatWindow):
   - Entry point of the application, initializes the chat with a user-provided nickname.

The program uses Java's standard libraries for networking (`java.net`), cryptography (`javax.crypto`), and utility functions (`java.util`). It leverages multicast sockets for group communication and threading for handling concurrent tasks like sending presence messages and receiving incoming data.

## Software and Hardware Requirements

### Software Requirements:
- **Java Runtime Environment (JRE)**: Version 8 or higher.
- **Operating System**: Any OS that supports Java (e.g., Windows, macOS, Linux).
- **Network Configuration**: All users must be connected to the same local area network (LAN). The network should allow multicast traffic on the specified group and port (e.g., 230.0.0.1:5000).

### Hardware Requirements:
- **Computer**: Any standard computer or laptop with a network interface capable of joining a LAN.
- **Network Interface**: Ethernet or Wi-Fi adapter supporting multicast.
- **Memory**: Minimal memory usage; 512 MB RAM or more is sufficient.
- **Storage**: Negligible disk space; only the Java program files are needed.