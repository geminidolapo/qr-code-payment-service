package com.project.payment.service;


import com.project.payment.dao.entity.Merchant;
import com.project.payment.dao.entity.Role;
import com.project.payment.dao.entity.User;
import com.project.payment.dao.repository.MerchantRepository;
import com.project.payment.dao.repository.RoleRepository;
import com.project.payment.dao.repository.UserRepository;
import com.project.payment.dto.request.AuthenticationReq;
import com.project.payment.dto.request.RegisterReq;
import com.project.payment.dto.response.*;
import com.project.payment.exception.BadRequestException;
import com.project.payment.exception.UserNotFoundException;
import com.project.payment.util.AccountUtil;
import com.project.payment.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtService;
    private final AccountUtil accountService;
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("100");

    public AuthenticationRes loginUser(final AuthenticationReq request) {

        final var user = userRepository.findByUsername(request.getUsername())
                .filter(value -> passwordEncoder.matches(request.getPassword(), value.getPassword()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        final var tokenValue = jwtService.createToken(request, user);
        var userResponse = modelMapper.map(user, UserRes.class);
        return new AuthenticationRes(tokenValue, userResponse, null);
    }

    public AuthenticationRes loginMerchant(final AuthenticationReq request) {
        final var merchant = merchantRepository.findByUsername(request.getUsername())
                .filter(value -> passwordEncoder.matches(request.getPassword(), value.getPassword()))
                .orElseThrow(() -> new UserNotFoundException("Merchant not found"));

        final var tokenValue = jwtService.createToken(request, merchant);
        var merchantResponse = modelMapper.map(merchant, MerchantRes.class);
        return new AuthenticationRes(tokenValue, null, merchantResponse);
    }

    @Transactional
    public RegisterUserRes registerUser(final RegisterReq request) {
        checkExists(request.getUsername(), "User");

        String virtualAccount = accountService.generateUserVirtualAccount("2");

        final var newUser = createUser(request, virtualAccount);
        final var savedUser = userRepository.save(newUser);
        return new RegisterUserRes(modelMapper.map(savedUser, UserRes.class));
    }

    @Transactional
    public RegisterMerchantRes registerMerchant(final RegisterReq request) {
        checkExists(request.getUsername(), "Merchant");

        String virtualAccount = accountService.generateMerchantVirtualAccount("3");

        final var newMerchant = createMerchant(request, virtualAccount);
        final var savedMerchant = merchantRepository.save(newMerchant);
        return new RegisterMerchantRes(modelMapper.map(savedMerchant, MerchantRes.class));
    }

    private void checkExists(String username, String type){
        if (merchantRepository.existsByUsername(username) ||
                userRepository.existsByUsername(username) ) {
            throw new BadRequestException(type + " already exists, Kindly change username");
        }
    }

    private User createUser(RegisterReq request, String virtualAccount) {
        var user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(getValidRoles(request.getRoles()));
        user.setAccountNumber(virtualAccount);
        user.setBalance(INITIAL_BALANCE);
        return user;
    }

    private Merchant createMerchant(RegisterReq request, String virtualAccount) {
        var merchant = new Merchant();
        merchant.setFirstname(request.getFirstname());
        merchant.setLastname(request.getLastname());
        merchant.setUsername(request.getUsername());
        merchant.setEmail(request.getEmail());
        merchant.setPassword(passwordEncoder.encode(request.getPassword()));
        merchant.setRoles(getValidRoles(request.getRoles()));
        merchant.setAccountNumber(virtualAccount);
        return merchant;
    }

    private Set<Role> getValidRoles(Set<String> roles) {
        Set<Role> validRoles = new HashSet<>();
        for (String role : roles) {
            Role existingRole = roleRepository.findByName(role);
            if (existingRole == null) {
                throw new BadRequestException("Invalid role: " + role);
            }
            validRoles.add(existingRole);
        }
        return validRoles;
    }
}
