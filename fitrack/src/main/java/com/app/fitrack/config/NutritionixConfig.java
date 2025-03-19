package com.app.fitrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "nutritionix")
@Getter
@Setter

public class NutritionixConfig {

    private String appId;
    private String appKey;

    @PostConstruct
    public void init() {
        System.out.println("Nutritionix Config Loaded: appId=" + appId + ", appKey=" + appKey);
    }

}
