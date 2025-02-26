package com.project.payment.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Data
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan("com.project.payment.service")
public class ExternalRequestProperties {
    private String secretKey;
    private String authSecretKey;
    private String pathPrefix;
    private String[] permitAllPaths;
    private long jwtExpiresAt;
    private String jwtIssuer;
}
