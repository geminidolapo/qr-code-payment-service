package com.project.payment.util;

import com.project.payment.dto.request.ProcessPaymentReq;
import com.project.payment.dto.response.ApiResponse;
import com.project.payment.dto.response.PaymentRes;
import com.project.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessPaymentUtil {

    private final EncryptionUtil encryptionUtil;
    private final PaymentService paymentService;

    public ApiResponse<PaymentRes> processPayment(ProcessPaymentReq request){
        var data = encryptionUtil.decryptData(request.getEncryptedData());
        log.info("decrypted payment request successfully");

        final var payload = RequestUtil.getPaymentRequest(data);
        log.info("decrypted payment request: {}", payload);

        return paymentService.makePayment(payload);
    }
}
