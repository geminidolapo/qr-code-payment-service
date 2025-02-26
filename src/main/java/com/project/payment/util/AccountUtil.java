package com.project.payment.util;

import com.project.payment.dao.repository.MerchantRepository;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountUtil {

    private static final String NUBAN = "373373373373";
    private static final int MAX_ATTEMPTS = 1000;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public String generateMerchantVirtualAccount(String prefix) {
        log.info("Generating virtual account for merchant");

        Set<String> existingNumbers = new HashSet<>();
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            String vFid = prefix + (100_000_000 + random.nextInt(900_000_000));

            // Check cache before hitting the database
            if (!existingNumbers.contains(vFid) && !merchantRepository.existsByAccountNumber(vFid)) {
                log.info("Generated unique virtual account: {}", vFid);
                return vFid;
            }
            existingNumbers.add(vFid);
            attempts++;
        }

        throw new AccountException("Failed to generate a unique virtual account after " + MAX_ATTEMPTS + " attempts");
    }

    public String generateUserVirtualAccount(String prefix) {
        log.info("Generating virtual account for user");

        Set<String> existingNumbers = new HashSet<>();
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            String vFid = prefix + (100_000_000 + random.nextInt(900_000_000));

            if (!existingNumbers.contains(vFid) && !userRepository.existsByAccountNumber(vFid)) {
                log.info("Generated unique user virtual account: {}", vFid);
                return vFid;
            }
            existingNumbers.add(vFid);
            attempts++;
        }

        throw new AccountException("Failed to generate a unique user virtual account after " + MAX_ATTEMPTS + " attempts");
    }
}
