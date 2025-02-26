package com.project.payment.configuration;

import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.project.payment.exception.GenericException;
import com.project.payment.util.UserInfoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final ExternalRequestProperties authenticationProperties;
    private final UserInfoUtil userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,HandlerMappingIntrospector introspector) throws Exception {
        http
                // default all security checks to /auth path
                //.securityMatcher(authenticationProperties.getPathPrefix() + "/**")
                .securityMatcher(authenticationProperties.getPathPrefix())
                .authorizeHttpRequests(authorize -> {
                    if (Objects.nonNull(authenticationProperties.getPermitAllPaths())) {
                        Arrays.stream(authenticationProperties.getPermitAllPaths()).forEach(
                                path -> authorize.requestMatchers(path).permitAll()
                        );
                    } else authorize.anyRequest().authenticated();
                })
                //.httpBasic(Customizer.withDefaults())
                .cors(value -> value.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            jwt.decoder(jwtDecoder());  // Set JWT decoder
                            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());  // Set JWT authentication converter
                        })
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(userDetailsService)
                //.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public AuthenticationManager authManager() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    private CorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();
        var configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
//        configuration.setAllowCredentials(true);
        source.registerCorsConfiguration(authenticationProperties.getPathPrefix() + "/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() throws KeyLengthException {
        MACSigner macSigner = new MACSigner(authenticationProperties.getAuthSecretKey());
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(macSigner.getSecretKey());

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            MACSigner macSigner = new MACSigner(authenticationProperties.getAuthSecretKey());
            final var withClockSkew = new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ZERO));
            NimbusJwtDecoder build = NimbusJwtDecoder.withSecretKey(macSigner.getSecretKey())
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();

            build.setJwtValidator(withClockSkew);

            return build;
        } catch (KeyLengthException e) {
            throw new GenericException(e);
        }
    }
}