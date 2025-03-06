package com.project.payment;

import com.project.payment.constant.StatusEnum;
import com.project.payment.dao.entity.Merchant;
import com.project.payment.dao.entity.Transaction;
import com.project.payment.dao.entity.User;
import com.project.payment.dao.repository.MerchantRepository;
import com.project.payment.dao.repository.TransactionRepository;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.dto.request.PaymentReq;
import com.project.payment.dto.response.ApiResponse;
import com.project.payment.dto.response.PaymentRes;
import com.project.payment.service.PaymentService;
import com.project.payment.security.JwtUtil;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(merchantRepository, userRepository, transactionRepository, jwtUtil);
    }

    @Test
    public void shouldSuccessfullyProcessTransactionWhenUserAndMerchantExistAndFundsAreSufficient() {
        // Arrange
        PaymentReq paymentReq = new PaymentReq();
        paymentReq.setUserId("1");
        paymentReq.setMerchantId("2");
        paymentReq.setAmount(new BigDecimal("100.00"));

        User user = new User();
        user.setId(1L);
        user.setBalance(new BigDecimal("200.00"));
        user.setAccountNumber("user-account-123");

        Merchant merchant = new Merchant();
        merchant.setId(2L);
        merchant.setAccountNumber("merchant-account-456");

        Transaction transaction = new Transaction();
        transaction.setTransactionId("txn-123");
        transaction.setUserBalanceBefore(user.getBalance());
        transaction.setMerchantBalanceBefore(merchant.getBalance());

        when(jwtUtil.getAuthenticatedUser()).thenReturn(user);
        when(userRepository.findByUsernameWithLock(anyString())).thenReturn(Optional.of(user));
        when(merchantRepository.findByMerchantId("2")).thenReturn(Optional.of(merchant));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Act
        ApiResponse<PaymentRes> response = paymentService.makePayment(paymentReq);

        // Assert
        assertNotNull(response);
        assertEquals(StatusEnum.SUCCESSFUL, response.getData().getStatus());
        assertEquals("Transaction Successful", response.getData().getStatusMessage());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
