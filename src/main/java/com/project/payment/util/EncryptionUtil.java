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
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class EncryptionUtil {

    private final ExternalRequestProperties externalRequestProperties;
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public String encryptData(String data) {
        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(externalRequestProperties.getSecretKey());

            if (secretKeyBytes.length != 16 && secretKeyBytes.length != 24 && secretKeyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length: " + secretKeyBytes.length + " bytes. Expected 16, 24, or 32 bytes.");
            }

            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            byte[] iv = generateIV();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Encryption failed for data: {}", data, e);
            throw new QrCodeGenerationException("Encryption error");
        }
    }

    public String decryptData(String encryptedData) {
        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(externalRequestProperties.getSecretKey());

            if (secretKeyBytes.length != 16 && secretKeyBytes.length != 24 && secretKeyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length: " + secretKeyBytes.length + " bytes.");
            }

            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, "AES");

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[decodedBytes.length - 16];
            System.arraycopy(decodedBytes, 0, iv, 0, 16);
            System.arraycopy(decodedBytes, 16, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for encrypted data: {}", encryptedData, e);
            throw new QrCodeGenerationException("Decryption error");
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
