import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-128/CBC/PKCS5Padding encryption helper.
 * Improved to handle large messages and use CBC mode with IV.
 */
public class EncryptionUtils {
    private static final String ALGORITHM      = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // Changed to CBC mode
    private static final String KEY            = "1234567890123456"; // 16-byte
    private static final int IV_SIZE           = 16; // 16 bytes for AES
    private static final int MAX_MESSAGE_SIZE  = 8192; // 8KB max message size

    /**
     * Encrypt a message using AES-128/CBC with a random IV
     */
    public static String encrypt(String message) throws Exception {
        // Check message size to prevent issues
        if (message.length() > MAX_MESSAGE_SIZE) {
            // Truncate very long messages to prevent encryption issues
            message = message.substring(0, MAX_MESSAGE_SIZE) + "... [Message truncated due to size]";
        }
        
        // Generate a random IV for each encryption
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Create key spec
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        
        // Initialize cipher with IV
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        
        // Encrypt the message
        byte[] encrypted = cipher.doFinal(message.getBytes());
        
        // Combine IV and encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
        byteBuffer.put(iv);
        byteBuffer.put(encrypted);
        
        // Encode as Base64 for transmission
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    /**
     * Decrypt a message using AES-128/CBC
     */
    public static String decrypt(String encryptedMessage) throws Exception {
        try {
            // Decode from Base64
            byte[] encryptedData = Base64.getDecoder().decode(encryptedMessage);
            
            // Extract IV from the beginning of the encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[IV_SIZE];
            byteBuffer.get(iv);
            
            // Extract the encrypted content (remaining bytes)
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            
            // Create IV and key specs
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            // Decrypt the message
            byte[] decrypted = cipher.doFinal(cipherText);
            
            // Convert to string and return
            return new String(decrypted);
        } catch (Exception e) {
            // Provide more helpful error message
            System.err.println("Decryption error: " + e.getMessage());
            return "[Decryption failed: " + e.getMessage() + "]";
        }
    }
}
