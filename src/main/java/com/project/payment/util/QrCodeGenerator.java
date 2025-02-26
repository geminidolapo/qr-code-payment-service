package com.project.payment.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.project.payment.dao.entity.User;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.dto.request.PaymentReq;
import com.project.payment.dto.response.QrCodeGeneratorRes;
import com.project.payment.exception.QrCodeGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCodeGenerator {

    private final EncryptionUtil encryptionUtil;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private static final String CHARSET = "UTF-8";
    private static final int IMG_WIDTH = 200;
    private static final int IMG_HEIGHT = 200;

    public QrCodeGeneratorRes generateQRCode(final PaymentReq request) {
        try {
            log.info("Starting QRCode generation");

            User user = userRepository.getUserByUsername(jwtUtil.getAuthenticatedUser().getUsername());

            String qrData = request.getAmount() + "," + request.getCurrency() + "," + request.getMerchantId()
                    + "," + request.getDescription() + "," + user.getId();

            String encryptedData = encryptionUtil.encryptData(qrData);
            log.info("Encrypted data: {}" , encryptedData);

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(encryptedData, BarcodeFormat.QR_CODE, IMG_WIDTH, IMG_HEIGHT, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", outputStream);
            String base64QRCode = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            log.info("QR Code successfully generated for data: {}", qrData);
            return new QrCodeGeneratorRes(base64QRCode);
        } catch (WriterException|IOException e) {
            throw new QrCodeGenerationException("Failed to generate QR code", e);
        }
    }
}
