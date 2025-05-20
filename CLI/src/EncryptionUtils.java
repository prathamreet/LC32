import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility for AES-128 encryption/decryption of chat packets.
 * Uses ECB mode with PKCS5 padding. In real systems, use a more secure mode!
 */

public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    // AES mode: ECB + PKCS5 padding for block alignment
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String KEY = "1234567890123456"; // 16-byte key for AES-128


    // Encrypts a plaintext string and returns Base64-encoded ciphertext
    public static String encrypt(String message) throws Exception {
        // Prepare symmetric key from static 16-byte secret
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        // Cipher instance with specified mode and padding
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        // Initialize for encryption
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        // Encrypt the plaintext message (as bytes)
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        // Convert binary ciphertext into a string-safe Base64 format
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    
    // Decrypts a Base64-encoded ciphertext back into plaintext
    public static String decrypt(String encryptedMessage) throws Exception {
        // Prepare symmetric key from static 16-byte secret
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        // Cipher instance for decryption with same configuration
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        // Initialize cipher for decryption mode
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        // Decode from Base64 to raw encrypted bytes
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
        // Perform decryption operation
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        // Return decrypted text
        return new String(decryptedBytes);
    }
}