package com.project.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterMerchantRes {
    private MerchantRes user;
}
