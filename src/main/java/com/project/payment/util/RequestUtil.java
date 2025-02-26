package com.project.payment.util;

import com.project.payment.constant.CurrencyEnum;
import com.project.payment.dto.request.PaymentReq;
import lombok.experimental.UtilityClass;
import java.math.BigDecimal;

@UtilityClass
public class RequestUtil {
    public PaymentReq getPaymentRequest(String str){
        return PaymentReq.builder()
                .amount(new BigDecimal(str.split(",")[0]))
                .currency(CurrencyEnum.valueOf(str.split(",")[1]))
                .merchantId(str.split(",")[2])
                .description(str.split(",")[3])
                .userId(str.split(",")[4])
                .build();
    }
}
