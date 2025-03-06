package com.project.payment.dao.repository;

import com.project.payment.dao.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT m FROM User m WHERE m.username = :userName")
    Optional<User> findByUsernameWithLock(String userName);

    @EntityGraph(attributePaths = {"roles"})
    @Query("SELECT a FROM User a WHERE a.username = :username")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findByUsername(String userName);

    @Query("SELECT a FROM User a WHERE a.username = :username")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
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