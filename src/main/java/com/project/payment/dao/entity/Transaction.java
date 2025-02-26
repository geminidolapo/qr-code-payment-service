package com.project.payment.dao.entity;

import com.project.payment.constant.CurrencyEnum;
import com.project.payment.constant.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql="UPDATE transaction SET deleted=true WHERE id=?")
@EntityListeners({SoftDeleteListener.class})
@Table(name = "transaction")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, name = "transaction_id")
    private String transactionId;

    @Column(nullable = false,name = "user_id")
    private String userId;

    @Column(nullable = false,name = "user_Balance_Before")
    private BigDecimal userBalanceBefore;

    @Column(nullable = false,name = "user_Balance_After")
    private BigDecimal userBalanceAfter;

    @Column(name = "merchant_Balance_Before")
    private BigDecimal merchantBalanceBefore;

    @Column(name = "merchant_Balance_After")
    private BigDecimal merchantBalanceAfter;

    @Column(name="merchant_id")
    private String merchantId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private StatusEnum status; // SUCCESSFUL, INSUFFICIENT FUND, FAILED

    @Column(name = "status_message")
    private String statusMessage;
}