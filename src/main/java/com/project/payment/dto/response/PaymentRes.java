package com.project.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.payment.constant.CurrencyEnum;
import com.project.payment.constant.StatusEnum;
import com.project.payment.dao.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRes implements Serializable {
    private final String transactionId;
    private final BigDecimal amount;
    private CurrencyEnum currency;
    private final String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    private final StatusEnum status;
    private final String statusMessage;
    private BigDecimal userBalanceBefore;
    private BigDecimal userBalanceAfter;
    private BigDecimal merchantBalanceBefore;
    private BigDecimal merchantBalanceAfter;
    private String merchantId;

    public PaymentRes(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.description = transaction.getDescription();
        this.timestamp = transaction.getCreatedAt();
        this.status = transaction.getStatus();
        this.statusMessage = transaction.getStatusMessage();
        this.userBalanceBefore = transaction.getUserBalanceBefore();
        this.userBalanceAfter = transaction.getUserBalanceAfter();
        this.merchantBalanceBefore = transaction.getMerchantBalanceBefore();
        this.merchantBalanceAfter = transaction.getMerchantBalanceAfter();
        this.merchantId = transaction.getMerchantId();
    }
}
