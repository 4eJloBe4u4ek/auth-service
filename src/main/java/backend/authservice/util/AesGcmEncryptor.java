package backend.authservice.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmEncryptor {
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int KEY_LENGTH_BIT = 256;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmEncryptor(byte[] keyBytes) {
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public static SecretKey generateDataKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(KEY_LENGTH_BIT);
        return kg.generateKey();
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] ivAndCipher = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, ivAndCipher, 0, iv.length);
            System.arraycopy(cipherText, 0, ivAndCipher, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(ivAndCipher);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка при шифровании", e);
        }
    }

    public String decrypt(String base64IvAndCipher) {
        try {
            byte[] ivAndCipher = Base64.getDecoder().decode(base64IvAndCipher);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[ivAndCipher.length - IV_LENGTH_BYTE];
            System.arraycopy(ivAndCipher, 0, iv, 0, iv.length);
            System.arraycopy(ivAndCipher, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Ошибка при дешифровке", e);
        }
    }
}
