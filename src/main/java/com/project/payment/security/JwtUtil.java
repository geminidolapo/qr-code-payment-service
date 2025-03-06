package com.project.payment.security;


import com.project.payment.configuration.ExternalRequestProperties;
import com.project.payment.dao.entity.Merchant;
import com.project.payment.dao.entity.Role;
import com.project.payment.dao.entity.User;
import com.project.payment.dto.request.AuthenticationReq;
import com.project.payment.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtEncoder encoder;
    private final JwtDecoder jwtDecoder;
    private final ExternalRequestProperties authenticationProperties;

    public String createToken(AuthenticationReq request, User user) {
        return generateToken(request.getUsername(), user.getRoles());
    }

    public String createToken(AuthenticationReq request, Merchant merchant) {
        return generateToken(request.getUsername(), merchant.getRoles());
    }

    private String generateToken(String username, Set<Role> roles) {
        Instant nowInstant = Instant.now();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        List<String> roleNames = roles.stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authenticationProperties.getJwtIssuer())
                .issuedAt(nowInstant)
                .expiresAt(nowInstant.plusSeconds(authenticationProperties.getJwtExpiresAt()))
                .subject(username)
                .claim("roles", roleNames)
                .build();

        return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String getUsernameFromToken(String token) {
        return decodeJwt(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        Instant expirationTime = decodeJwt(token).getExpiresAt();
        return expirationTime != null && expirationTime.isBefore(Instant.now());
    }

    private Jwt decodeJwt(String token) {
        return jwtDecoder.decode(token);
    }

    public User getAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                return user;
            }
        } catch (Exception e) {
            log.info("Failed to retrieve authenticated user: {}", e.getMessage());
        }
        throw new UnauthorizedException("User is not authenticated");
    }

    public Merchant getAuthenticatedMerchant() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Merchant merchant) {
                return merchant;
            }
        } catch (Exception e) {
            log.info("Failed to retrieve authenticated merchant: {}", e.getMessage());
        }
        throw new UnauthorizedException("Merchant is not authenticated");
    }
}
