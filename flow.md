
## 🗂️ File-by-File Breakdown 

### 🔹 `Main.java`

**Role**: Entry point of the application.

**What It Does**:

* Initializes the theme, networking, and GUI.
* Instantiates the `ChatWindow` class.
* Starts background listener threads (from `MulticastManager`).

**Connected To**:

* `ThemeManager` (for dark/light UI)
* `MulticastManager` (for network joining)
* `ChatWindow` (main GUI)

**Think of it as**: The “startup script” that glues everything together.

---

### 🔹 `ChatWindow.java`

**Role**: Main GUI controller.

**What It Does**:

* Hosts the chat panel, message box, and user list.
* Listens for send events from the user.
* Passes messages to `MulticastManager` after encryption.
* Listens for new incoming messages (from `MulticastManager`) and displays them.

**Connected To**:

* `ChatPanel.java` (holds text area + send button)
* `UserListRenderer.java` (updates active users)
* `ChatBubbleRenderer.java` (displays messages nicely)

**Think of it as**: The **"View + Controller"** in MVC.

---

### 🔹 `ChatPanel.java`

**Role**: Sub-panel for sending messages.

**What It Does**:

* Captures user input (text box, send button).
* Triggers `MulticastManager.sendMessage()`, but only **after encryption** via `EncryptionUtils`.

**Connected To**:

* `EncryptionUtils.java`
* `MulticastManager.java`

---

### 🔹 `MulticastManager.java`

**Role**: Handles all networking.

**What It Does**:

* Creates and joins a multicast group.
* Sends messages (after encrypting them).
* Listens for messages in a background thread.
* On receiving: decrypts and dispatches to `ChatWindow`.

**Connected To**:

* `EncryptionUtils.java` (encrypt/decrypt)
* `Utils.java` (any common helper functions)
* `ChatWindow.java` (to update chat on new messages)

**Think of it as**: The “Model” that handles logic.

---

### 🔹 `EncryptionUtils.java`

**Role**: AES encryption/decryption toolkit.

**What It Does**:

* Uses `Cipher` for AES/ECB or AES/CBC (depending on your implementation).
* Static utility methods for encrypting and decrypting Strings.
* May use random IVs and Base64 for encoding.

**Connected To**:

* `MulticastManager.java`
* `ChatPanel.java`

**Think of it as**: The security engine.

---

### 🔹 `ThemeManager.java`

**Role**: Manages theme (Dark/Light mode).

**What It Does**:

* Applies CSS based on user/system preferences.
* Uses JavaFX styling hooks.

**Connected To**:

* `Main.java` (initializes themes)
* `ChatWindow.java` (applies styles)

---

### 🔹 `ChatBubbleRenderer.java`

**Role**: Renders chat bubbles for each message.

**What It Does**:

* Styles the message (left/right aligned).
* Applies rounded borders, colors, font sizes.
* Handles long messages, emoji display.

**Connected To**:

* `ChatWindow.java` (message gets rendered here)

**Think of it as**: UI sugar for better readability.

---

### 🔹 `UserListRenderer.java`

**Role**: Manages the display of currently active users.

**What It Does**:

* Renders the user list sidebar.
* Dynamically adds/removes users based on message origin (can extract user from message payload or IP).

**Connected To**:

* `ChatWindow.java`

---

### 🔹 `Utils.java`

**Role**: Miscellaneous helpers.

**What It Might Include**:

* Timestamp formatting
* Random string generators
* IP parsing
* Logging helpers

**Connected To**:

* Any file needing general-purpose functions.

---

## 🔁 Full Message Flow

Here’s the **actual sequence** that happens when a user sends and receives a message:

#### 📤 Sending

1. User types a message in `ChatPanel.java`.
2. Message is passed to `EncryptionUtils.encrypt(message)`.
3. Encrypted message goes to `MulticastManager.sendMessage(encryptedMsg)`.
4. Message is sent using a `DatagramPacket` over the multicast group.

#### 📥 Receiving

1. `MulticastManager` is constantly listening on a background thread.
2. When a packet is received:

   * Decrypt it using `EncryptionUtils.decrypt()`
   * Extract user + message content.
3. Pass the result to `ChatWindow.addMessage()`
4. Message is displayed using `ChatBubbleRenderer` in the GUI.
5. If the sender is new, `UserListRenderer` adds them to the list.

---

## 🔐 Encryption Path

* **Encrypt Before Send** → `EncryptionUtils.encrypt(plaintext)`
* **Decrypt After Receive** → `EncryptionUtils.decrypt(ciphertext)`
* This ensures **nobody sniffing the multicast packets** can read anything without the key.

---

## 🔄 UI and Networking Run in Parallel

* **JavaFX GUI Thread** handles visuals (`ChatWindow`, `ChatPanel`)
* **Background Thread** (inside `MulticastManager`) listens for incoming messages.
* Communication between them is typically handled using `Platform.runLater()` to avoid thread crashes.

---
