package com.project.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessPaymentReq {

    @NotBlank(message = "encrypted Data cannot be null or empty")
    private String encryptedData;
}
