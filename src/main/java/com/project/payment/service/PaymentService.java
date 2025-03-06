package com.project.payment.service;

import com.project.payment.constant.StatusEnum;
import com.project.payment.dao.entity.Merchant;
import com.project.payment.dao.entity.Transaction;
import com.project.payment.dao.entity.User;
import com.project.payment.dao.repository.MerchantRepository;
import com.project.payment.dao.repository.TransactionRepository;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.dto.request.FundAccountReq;
import com.project.payment.dto.request.PaymentReq;
import com.project.payment.dto.response.ApiResponse;
import com.project.payment.dto.response.PaymentRes;
import com.project.payment.exception.AccountException;
import com.project.payment.exception.UnauthorizedException;
import com.project.payment.exception.UserNotFoundException;
import com.project.payment.util.DateUtil;
import com.project.payment.security.JwtUtil;
import com.project.payment.util.RandomUtil;
import com.project.payment.util.StringUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;

    /**
     * Processes a payment transaction between a user and a merchant.
     *
     * @param req the payment request containing details such as user ID, merchant ID, and amount.
     * @return an ApiResponse containing a PaymentRes object that represents the result of the transaction.
     * @throws UserNotFoundException if the user or merchant is not found.
     * @throws AccountException if the user is unauthorized or has insufficient funds.
     */
    @Transactional
    public ApiResponse<PaymentRes> makePayment(PaymentReq req) {

        // Extract User
        User user = userRepository.findByUsernameWithLock(String.valueOf(jwtUtil.getAuthenticatedUser().getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User with username "
                        + jwtUtil.getAuthenticatedUser().getUsername() + " not found!"));

        // Validate that the user owns request
        if (!user.getId().equals(Long.valueOf(req.getUserId()))) {
            throw new UnauthorizedException("Unauthorized: You did initiate this request!");
        }

        // Validate that the Merchant Exists
        Merchant merchant = merchantRepository.findByMerchantId(req.getMerchantId())
                .orElseThrow(() -> new UserNotFoundException("Merchant with ID " + req.getMerchantId() + " not found!"));

        // Check Insufficient Funds
        if (hasInsufficientFunds(user.getBalance(), req.getAmount())) {
            throw new AccountException("Insufficient funds in your account");
        }

        // Build Transaction Object
        final var transaction = buildTransaction(req,user,merchant);
        log.info("Transaction initialized: {}", transaction);

        // Perform Atomic Balance Updates with Locking
        BigDecimal newUserBalance = updateUserBalance(user.getAccountNumber(), req.getAmount().negate());  // Debit
        BigDecimal newMerchantBalance = updateMerchantBalance(merchant.getAccountNumber(), req.getAmount());   // Credit

        // Mark Transaction as Successful
        transaction.setUserBalanceAfter(newUserBalance);
        transaction.setMerchantBalanceAfter(newMerchantBalance);
        transaction.setStatus(StatusEnum.SUCCESSFUL);
        transaction.setStatusMessage("Transaction Successful");
        transactionRepository.save(transaction);

        log.info("Transaction completed successfully. Source Account = {}, Reference = {}",
                user.getAccountNumber(), transaction.getTransactionId());

        return ApiResponse.success(new PaymentRes(transaction));
    }

    /**
     * Retrieves a paginated list of payment transactions for the authenticated user, filtered by merchant ID and date range.
     *
     * @param merchantId the ID of the merchant to filter transactions by. Can be null or empty to include all merchants.
     * @param startDate the start date of the date range to filter transactions. Can be null to include all dates.
     * @param endDate the end date of the date range to filter transactions. Can be null to include all dates.
     * @param pageable the pagination information, including page number and size.
     * @return an ApiResponse containing a Page of PaymentRes objects representing the filtered transactions.
     */
    public ApiResponse<Page<PaymentRes>> getUserPayments(String merchantId, String startDate, String endDate, Pageable pageable) {
        log.info("Fetching user transactions with filters - merchantId: {}, startDate: {}, endDate: {}", merchantId, startDate, endDate);

        User user = userRepository.getUserByUsername(jwtUtil.getAuthenticatedUser().getUsername());

        Specification<Transaction> spec = buildTransactionSpecification(String.valueOf(user.getId()), merchantId, startDate, endDate);

        Page<Transaction> pagedResults = transactionRepository.findAll(spec, pageable);

        if (pagedResults.isEmpty()) {
            log.info("No transactions found for user: {} with filters merchantId: {}, startDate: {}, endDate: {}",
                    user.getId(), merchantId, startDate, endDate);
        }

        List<PaymentRes> transactionResponses = pagedResults.getContent()
                .stream()
                .map(PaymentRes::new)
                .toList();

        log.info("Fetched {} transactions for user {}", transactionResponses.size(), user.getId());

        return ApiResponse.success(new PageImpl<>(transactionResponses, pageable, pagedResults.getTotalElements()));
    }


    /**
     * Retrieves a paginated list of payment transactions for the authenticated merchant, filtered by user ID and date range.
     *
     * @param userId    The ID of the user to filter transactions by. Can be null or empty to include all users.
     * @param startDate The start date of the date range to filter transactions. Can be null to include all dates from the beginning.
     * @param endDate   The end date of the date range to filter transactions. Can be null to include all dates up to the present.
     * @param pageable  The pagination information, including page number and size.
     * @return An ApiResponse containing a Page of PaymentRes objects representing the filtered transactions for the merchant.
     */
    public ApiResponse<Page<PaymentRes>> getMerchantPayments(String userId, String startDate, String endDate, Pageable pageable) {
        log.info("Fetching merchant transactions with filters - userId: {}, startDate: {}, endDate: {}", userId, startDate, endDate);

        Merchant merchant = merchantRepository.getMerchantByUsername(jwtUtil.getAuthenticatedMerchant().getUsername());
        Specification<Transaction> spec = buildTransactionSpecification(userId, String.valueOf(merchant.getId()), startDate, endDate);

        Page<Transaction> pagedResults = transactionRepository.findAll(spec, pageable);

        if (pagedResults.isEmpty()) {
            log.info("No transactions found for merchant: {} with filters userId: {}, startDate: {}, endDate: {}",
                    merchant.getId(), userId, startDate, endDate);
        }

        List<PaymentRes> transactionResponses = pagedResults.getContent()
                .stream()
                .map(PaymentRes::new)
                .toList();

        log.info("Fetched {} transactions for merchant {}", transactionResponses.size(), merchant.getId());

        return ApiResponse.success(new PageImpl<>(transactionResponses, pageable, pagedResults.getTotalElements()));
    }


    @Transactional
    public ApiResponse<PaymentRes> fundUserAccount(FundAccountReq req){

        // Extract User
        User user = userRepository.findByUsernameWithLock(String.valueOf(jwtUtil.getAuthenticatedUser().getUsername()))
                .orElseThrow(() -> new UserNotFoundException("User with username "
                        + jwtUtil.getAuthenticatedUser().getUsername() + " not found!"));

        // Validate that the user owns request
        if (!user.getId().equals(Long.valueOf(req.getUserId()))) {
            throw new UnauthorizedException("Unauthorized: You did initiate this request!");
        }

        final var transaction = buildTransaction(req,user);
        log.info("Funding Transaction initialized: {}", transaction);

        // Perform Atomic Balance Updates with Locking
        BigDecimal newUserBalance = updateUserBalance(user.getAccountNumber(), req.getAmount());  // Credit

        // Mark Transaction as Successful
        transaction.setUserBalanceAfter(newUserBalance);
        transaction.setStatus(StatusEnum.SUCCESSFUL);
        transaction.setStatusMessage("Funding Successful");
        transactionRepository.save(transaction);

        log.info("Funding completed successfully. Source Account = {}, Reference = {}",
                user.getAccountNumber(), transaction.getTransactionId());

        return ApiResponse.success(new PaymentRes(transaction));
    }

    /**
     * Helper method to build dynamic transaction filtering specifications.
     */
    private Specification<Transaction> buildTransactionSpecification(String userId, String merchantId, String startDate, String endDate) {
        Specification<Transaction> specification = Specification.where(null);

        if (StringUtil.hasValue(userId)) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }

        if (StringUtil.hasValue(merchantId)) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("merchantId"), merchantId));
        }

        LocalDateTime start = DateUtil.parseDate(startDate);
        LocalDateTime end = DateUtil.parseDate(endDate);

        if (startDate != null && endDate != null) {
            specification = specification.and((root, query, cb) -> cb.between(root.get("createdAt"), startDate, endDate));
        } else if (start != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        } else if (end != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), end));
        }

        return specification;
    }


    @Transactional
    public BigDecimal updateUserBalance(String accountNumber, BigDecimal amount) {
        log.info("Updating balance for User Account: {} with amount: {}", accountNumber, amount);

        // Fetch with Lock
        User user = userRepository.getByAccountNumberWithLock(accountNumber);

        // Perform the update
        userRepository.updateUserBalance(accountNumber, amount);

        log.info("User Account {} updated successfully.", accountNumber);
        return user.getBalance().add(amount); // Return updated balance
    }

    @Transactional
    public BigDecimal updateMerchantBalance(String accountNumber, BigDecimal amount) {
        log.info("Updating balance for Merchant Account: {} with amount: {}", accountNumber, amount);

        //  Fetch with Lock
        Merchant merchant = merchantRepository.getByAccountNumberWithLock(accountNumber);

        //  Perform the update
        merchantRepository.updateMerchantBalance(accountNumber, amount);

        log.info("Merchant Account {} updated successfully.", accountNumber);
        return merchant.getBalance().add(amount);
    }

    private boolean hasInsufficientFunds(BigDecimal accountBalance, BigDecimal requiredAmount) {
        return accountBalance.compareTo(requiredAmount) < 0;
    }

    private Transaction buildTransaction(PaymentReq req, User user,Merchant merchant) {
        return Transaction.builder()
                .transactionId(RandomUtil.generateUniqueRef())
                .userBalanceBefore(user.getBalance())
                .merchantBalanceBefore(merchant.getBalance())
                .merchantId(String.valueOf(merchant.getId()))
                .userId(String.valueOf(user.getId()))
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .description(req.getDescription())
                .build();
    }

    private Transaction buildTransaction(FundAccountReq req, User user) {
        return Transaction.builder()
                .transactionId(RandomUtil.generateUniqueRef())
                .userBalanceBefore(user.getBalance())
                .userId(String.valueOf(user.getId()))
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .description("FUNDING")
                .build();
    }
}