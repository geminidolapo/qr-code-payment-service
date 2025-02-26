package com.project.payment.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantRes extends BaseRes {
    private String merchantId;
}
