package com.project.payment.dao.repository;

import com.project.payment.dao.entity.Merchant;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

  @Lock(LockModeType.PESSIMISTIC_READ)
  @Query("SELECT m FROM Merchant m WHERE m.id = :merchantId")
  Optional<Merchant> findByMerchantId(String merchantId);

  Optional<Merchant> findByUsername(String username);

  Merchant getMerchantByUsername(String username);

  boolean existsByAccountNumber(String accountNumber);

  boolean existsByUsername(String username);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT m FROM Merchant m WHERE m.accountNumber = :accountNumber")
  Merchant getByAccountNumberWithLock(String accountNumber);

  @Transactional
  @Modifying
  @Query("UPDATE Merchant m SET m.balance = m.balance + :amount WHERE m.accountNumber = :accountNumber")
  int updateMerchantBalance(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);
}