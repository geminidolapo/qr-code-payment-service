package com.project.payment.filter;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.project.payment.configuration.ExternalRequestProperties;
import com.project.payment.util.JwtUtil;
import com.project.payment.util.UserInfoUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private final ExternalRequestProperties authenticationProperties;
    private final UserInfoUtil userDetailsService;
    private final JwtUtil jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("starting auth filter");
            final String authHeader = request.getHeader("Authorization");
            final String token = authHeader != null ? authHeader.replace("Bearer ", "").trim() : "";
            final String url = request.getRequestURL().toString();
            log.debug("auth token: {}", token);
            log.info("url: {}", url);

            boolean notWhiteListed = Arrays.stream(authenticationProperties.getPermitAllPaths())
                    .noneMatch(url::contains);
            log.debug("WhiteListed paths: {}", Arrays.toString(authenticationProperties.getPermitAllPaths()));

            if (url.contains(authenticationProperties.getPathPrefix()) && notWhiteListed && !token.isBlank()) {
                log.trace("entering authenticated section");
                final var username = jwtService.getUsernameFromToken(token);
                final var notExpiredToken = !jwtService.isTokenExpired(token);
                var authentication = SecurityContextHolder.getContext();

                if (notExpiredToken && username != null && authentication.getAuthentication() == null) {

                    final var userDetails = userDetailsService.loadUserByUsername(username);
                    var authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, userDetails, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    log.info("Authenticated user {}, setting security context", username);
                    authentication.setAuthentication(authenticationToken);
                    log.info("User [{}] authenticated", username);
                }
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            handleException(response, "Invalid or expired token", HttpServletResponse.SC_UNAUTHORIZED);
        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            handleException(response, "Authentication failed", HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            handleException(response, "An unexpected error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error",message);
        jsonObject.addProperty("msg","UNAUTHORIZED");
        jsonObject.addProperty("code","99");
        jsonObject.addProperty("status","failure");
        jsonObject.addProperty("timestamp", LocalDateTime.now().toString());

        response.getWriter().write(jsonObject.toString());
    }
}
