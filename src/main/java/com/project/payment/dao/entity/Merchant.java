package com.project.payment.dao.entity;

import com.project.payment.constant.CurrencyEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@ToString
@Validated
@SuperBuilder
@DynamicInsert
@DynamicUpdate
@Table(
        name="merchant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"firstname", "lastname"}),
        indexes =  {
                @Index(name = "idx_mch_id", columnList = "id",unique = true),
                @Index(name = "idx_username", columnList = "username",unique = true)
        })
@AllArgsConstructor(access= AccessLevel.PUBLIC)
@NoArgsConstructor(access= AccessLevel.PUBLIC)
@SQLDelete(sql="UPDATE security_user SET deleted=true WHERE id=?")
@EntityListeners({SoftDeleteListener.class,ActiveUserListener.class})
public class Merchant extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(length=100, unique=true, nullable=false)
    private String username;

    @Column(nullable=false)
    private String password;

    @Column(length=50, nullable=false)
    private String firstname;

    @Column(length=50, nullable=false)
    private String lastname;

    @Email
    @Column(length = 100, nullable=false)
    private String email;

    @Builder.Default
    private boolean active = Boolean.TRUE;

    @ToString.Exclude
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "merchant_roles",
            joinColumns = @JoinColumn(name = "merchant_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "account_number", nullable = false, unique = true, length = 15)
    private String accountNumber;

    @Builder.Default
    @Column(name = "balance", nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private CurrencyEnum currency = CurrencyEnum.NGN;

}
