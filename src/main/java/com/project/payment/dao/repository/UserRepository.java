package com.project.payment.dao.repository;

import com.project.payment.dao.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT m FROM User m WHERE m.username = :userName")
    Optional<User> findByUsernameWithLock(String userName);

    Optional<User> findByUsername(String userName);

    User getUserByUsername(String userName);

    boolean existsByUsername(String username);

    boolean existsByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.accountNumber = :accountNumber")
    User getByAccountNumberWithLock(String accountNumber);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.accountNumber = :accountNumber")
    int updateUserBalance(@Param("accountNumber") String accountNumber, @Param("amount") BigDecimal amount);
}