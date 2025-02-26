package com.project.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QrCodeGeneratorRes {
    private String qrCode;
}
