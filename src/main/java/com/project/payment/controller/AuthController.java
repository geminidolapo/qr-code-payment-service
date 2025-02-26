package com.project.payment.controller;


import com.project.payment.dto.request.AuthenticationReq;
import com.project.payment.dto.request.RegisterReq;
import com.project.payment.dto.response.ApiResponse;
import com.project.payment.dto.response.AuthenticationRes;
import com.project.payment.dto.response.RegisterMerchantRes;
import com.project.payment.dto.response.RegisterUserRes;
import com.project.payment.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/user-login")
    public ResponseEntity<ApiResponse<AuthenticationRes>> userLogin(@Valid @RequestBody AuthenticationReq authenticationReq) {
        log.info("Login attempt for user: {}", authenticationReq.getUsername());

        final var loginResponse = authenticationService.loginUser(authenticationReq);
        log.info("Login successful for user: {}", authenticationReq.getUsername());

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/merchant-login")
    public ResponseEntity<ApiResponse<AuthenticationRes>> merchantLogin(@Valid @RequestBody AuthenticationReq authenticationReq) {
        log.info("Login attempt for merchant: {}", authenticationReq.getUsername());

        final var loginResponse = authenticationService.loginMerchant(authenticationReq);
        log.info("Login successful for merchant: {}", authenticationReq.getUsername());

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/register-user")
    public ResponseEntity<ApiResponse<RegisterUserRes>> registerUser(@Valid @RequestBody RegisterReq registerUserReq) {
        log.info("Registering new user: {}", registerUserReq.getUsername());

        final var registerUserResponse = authenticationService.registerUser(registerUserReq);
        log.info("User registered successfully: {}", registerUserReq.getUsername());

        return ResponseEntity.ok(ApiResponse.success(registerUserResponse));
    }

    @PostMapping("/register-merchant")
    public ResponseEntity<ApiResponse<RegisterMerchantRes>> registerMerchant(@Valid @RequestBody RegisterReq registerMerchantReq) {
        log.info("Registering new merchant: {}", registerMerchantReq.getUsername());

        final var registerMerchantResponse = authenticationService.registerMerchant(registerMerchantReq);
        log.info("Merchant registered successfully: {}", registerMerchantReq.getUsername());

        return ResponseEntity.ok(ApiResponse.success(registerMerchantResponse));
    }
}
