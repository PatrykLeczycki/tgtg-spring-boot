package com.pleczycki.tgtg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.env")
public class EnvConfig {

    private String emailPasswordFilePath;
    private String emailMessageFilePath;
    private String websiteUrl;

    public String getEmailPasswordFilePath() {
        return emailPasswordFilePath;
    }

    public String getEmailMessageFilePath() {
        return emailMessageFilePath;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
