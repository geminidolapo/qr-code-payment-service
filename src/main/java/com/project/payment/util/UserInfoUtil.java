package com.project.payment.util;


import com.project.payment.dao.repository.MerchantRepository;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.model.AuthMerchant;
import com.project.payment.model.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoUtil implements UserDetailsService {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final var userDetails = userRepository.findByUsername(username)
                .map(AuthUser::new);

        final var merchantDetails = merchantRepository
                        .findByUsername(username)
                        .map(AuthMerchant::new);

        if(userDetails.isEmpty() && merchantDetails.isEmpty()){
            throw new UsernameNotFoundException("User not found: " + username);
        }

        if(userDetails.isPresent() && merchantDetails.isEmpty()){
            return userDetails.get();
        }

        if(userDetails.isEmpty()){
            return merchantDetails.get();
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
