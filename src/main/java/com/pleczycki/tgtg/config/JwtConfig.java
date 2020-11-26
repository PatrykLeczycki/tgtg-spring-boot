package com.pleczycki.tgtg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class JwtConfig {

    private String jwtSecret;
    private int jwtExpirationInMs;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public int getJwtExpirationInMs() {
        return jwtExpirationInMs;
    }

}
