package com.project.payment.util;

import com.project.payment.configuration.ExternalRequestProperties;
import com.project.payment.exception.QrCodeGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionUtil {

    private final ExternalRequestProperties externalRequestProperties;
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public String encryptData(String data) {
        try {
            SecretKeySpec secretKey = getSecretKey();
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, null);
            byte[] iv = cipher.getIV();

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            return encodeWithIV(iv, encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption failed for data: {}", data, e);
            throw new QrCodeGenerationException("Encryption error");
        }
    }

    public String decryptData(String encryptedData) {
        try {
            SecretKeySpec secretKey = getSecretKey();
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);

            byte[] iv = extractIV(decodedBytes);
            byte[] encryptedBytes = extractEncryptedData(decodedBytes, iv.length);

            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);
            return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for encrypted data: {}", encryptedData, e);
            throw new QrCodeGenerationException("Decryption error");
        }
    }

    private SecretKeySpec getSecretKey() {
        byte[] secretKeyBytes = Base64.getDecoder().decode(externalRequestProperties.getSecretKey());

        if (secretKeyBytes.length != 16 && secretKeyBytes.length != 24 && secretKeyBytes.length != 32) {
            throw new IllegalArgumentException("Invalid AES key length: " + secretKeyBytes.length + " bytes.");
        }

        return new SecretKeySpec(secretKeyBytes, "AES");
    }

    private Cipher initCipher(int mode, SecretKeySpec secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        if (iv == null) {
            iv = generateIV();
        }
        cipher.init(mode, secretKey, new IvParameterSpec(iv));
        return cipher;
    }

    private byte[] extractIV(byte[] decodedBytes) {
        return Arrays.copyOfRange(decodedBytes, 0, 16);
    }

    private byte[] extractEncryptedData(byte[] decodedBytes, int ivLength) {
        return Arrays.copyOfRange(decodedBytes, ivLength, decodedBytes.length);
    }

    private String encodeWithIV(byte[] iv, byte[] encryptedBytes) {
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    private byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
