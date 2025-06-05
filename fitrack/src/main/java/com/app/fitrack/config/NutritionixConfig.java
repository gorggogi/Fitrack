package com.app.fitrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "nutritionix")
public class NutritionixConfig {

    private String appId;
    private String appKey;

    @PostConstruct
    public void init() {
        System.out.println("Nutritionix Config Loaded: appId=" + appId + ", appKey=" + appKey);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
