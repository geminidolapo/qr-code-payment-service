package com.project.payment.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.payment.dto.request.FundAccountReq;
import com.project.payment.dto.request.PaymentReq;
import com.project.payment.dto.request.ProcessPaymentReq;
import com.project.payment.dto.response.ApiResponse;
import com.project.payment.dto.response.PaymentRes;
import com.project.payment.dto.response.QrCodeGeneratorRes;
import com.project.payment.service.PaymentService;
import com.project.payment.util.ProcessPaymentUtil;
import com.project.payment.util.QrCodeGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final QrCodeGenerator qrCodeGenerator;
    private final PaymentService paymentService;
    private final ProcessPaymentUtil processPaymentUtil;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/generate-qrcode")
    public ResponseEntity<ApiResponse<QrCodeGeneratorRes>> generateQRCode(@Valid @RequestBody PaymentReq request){
        log.info("Received request to generate QR Code: {}", request);

        final var res = qrCodeGenerator.generateQRCode(request);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    // create a util to handle

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentRes>> processPayment(@Valid @RequestBody ProcessPaymentReq request){
        log.info("processing payment request");

        final var paymentResponse = processPaymentUtil.processPayment(request);
        return ResponseEntity.ok(paymentResponse);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user-payment")
    public ResponseEntity<ApiResponse<Page<PaymentRes>>> getUserPayment(

            @RequestParam(required = false) String merchantId,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String startDate,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String endDate,

            Pageable pageable
    ){
        log.info("Getting user's payment history");
        final var transactions = paymentService.getUserPayments(merchantId, startDate, endDate, pageable);

        return ResponseEntity.ok(transactions);
    }


    @PreAuthorize("hasRole('MERCHANT')")
    @GetMapping("/merchant-payment")
    public ResponseEntity<ApiResponse<Page<PaymentRes>>> getMerchantPayment(

            @RequestParam(required = false) String userId,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String startDate,

            @RequestParam(required = false)
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            String endDate,

            Pageable pageable
    ){
        log.info("Getting merchant's payment history");
        final var transactions = paymentService.getMerchantPayments(userId, startDate, endDate, pageable);

        return ResponseEntity.ok(transactions);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/fund")
    public ResponseEntity<ApiResponse<PaymentRes>> fund(@Valid @RequestBody FundAccountReq req){
        log.info("Received request to fund user account: {}", req);
        final var funded = paymentService.fundUserAccount(req);

        return ResponseEntity.ok(funded);
    }
}
