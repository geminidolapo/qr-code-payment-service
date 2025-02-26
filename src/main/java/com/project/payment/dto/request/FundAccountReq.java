package com.project.payment.dto.request;

import com.project.payment.constant.CurrencyEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FundAccountReq {

    @NotBlank(message = "user id is required")
    private String userId;

    @Min(value = 10, message = "Transaction amount must be at least 10")
    @NotNull(message = "Transaction amount is required")
    @Positive(message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction currency is required")
    private CurrencyEnum currency;
}
