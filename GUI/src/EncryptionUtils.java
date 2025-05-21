import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * This class handles the encryption and decryption of our chat messages.
 * It uses AES-128 encryption in CBC mode to keep messages secure.
 * 
 * @author LC32 Team
 * @version 1.0
 */
public class EncryptionUtils {
    // The encryption settings
    private static final String ALGORITHM      = "AES";                  // Advanced Encryption Standard
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // CBC mode with padding
    private static final String KEY            = "1234567890123456";     // 16-byte encryption key
    private static final int IV_SIZE           = 16;                     // 16 bytes for AES initialization vector
    private static final int MAX_MESSAGE_SIZE  = 8192;                   // 8KB max message size

    /**
     * Encrypts a message so it can be sent securely.
     * 
     * @param message The plain text message to encrypt
     * @return The encrypted message as a Base64 string
     * @throws Exception If encryption fails
     */
    public static String encrypt(String message) throws Exception {
        // First, check if the message is too long
        if (message.length() > MAX_MESSAGE_SIZE) {
            // If it's too long, cut it off and add a note
            message = message.substring(0, MAX_MESSAGE_SIZE) + "... [Message truncated due to size]";
        }
        
        // Create a random initialization vector (IV) for security
        // Using a different IV for each message prevents pattern analysis
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Set up the encryption key
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        
        // Initialize the encryption engine with our key and IV
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        
        // Encrypt the message
        byte[] encrypted = cipher.doFinal(message.getBytes());
        
        // Combine the IV and encrypted data
        // We need to include the IV with the message so it can be decrypted later
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);
        
        // Convert to Base64 for safe transmission
        // Base64 ensures the binary data can be sent as text
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    /**
     * Decrypts a message that was encrypted with our system.
     * 
     * @param encryptedMessage The Base64 encoded encrypted message
     * @return The decrypted plain text message
     * @throws Exception If decryption fails
     */
    public static String decrypt(String encryptedMessage) throws Exception {
        try {
            // Convert from Base64 back to binary
            byte[] encryptedData = Base64.getDecoder().decode(encryptedMessage);
            
            // Extract the IV from the beginning of the data
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);
            
            // Extract the actual encrypted message (everything after the IV)
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            
            // Set up the decryption parameters
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            
            // Initialize the decryption engine
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // Decrypt the message
            byte[] decrypted = cipher.doFinal(cipherText);
            
            // Convert from bytes back to a string
            return new String(decrypted);
        } catch (Exception e) {
            // If something goes wrong, log it and return an error message
            System.err.println("Decryption error: " + e.getMessage());
            return "[Decryption failed: " + e.getMessage() + "]";
        }
    }
}
