package com.project.payment.util;


import com.project.payment.configuration.ExternalRequestProperties;
import com.project.payment.dao.entity.Merchant;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtEncoder encoder;
    private final JwtDecoder jwtDecoder;
    private final ExternalRequestProperties authenticationProperties;

    public String createToken(AuthenticationReq request, User user) {
        final var nowInstant = Instant.now();

        final var jwsHeader = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        final var roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();

        final var claims = JwtClaimsSet.builder()
                .issuer(this.authenticationProperties.getJwtIssuer())
                .issuedAt(nowInstant)
                .expiresAt(nowInstant.plusSeconds(this.authenticationProperties.getJwtExpiresAt()))
                .subject(request.getUsername())
                .claim("roles",roles)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }

    public String createToken(AuthenticationReq request, Merchant merchant) {
        final var nowInstant = Instant.now();

        final var jwsHeader = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        final var roles = merchant.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .toList();

        final var claims = JwtClaimsSet.builder()
                .issuer(this.authenticationProperties.getJwtIssuer())
                .issuedAt(nowInstant)
                .expiresAt(nowInstant.plusSeconds(this.authenticationProperties.getJwtExpiresAt()))
                .subject(request.getUsername())
                .claim("roles",roles)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();
    }

    public String getUsernameFromToken(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    public boolean isTokenExpired(String token) {
        Jwt jwt = this.jwtDecoder.decode(token);
        Instant expirationTime = jwt.getExpiresAt();

        assert expirationTime != null;
        return expirationTime.isBefore(Instant.now());
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