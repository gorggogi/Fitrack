package com.app.fitrack.service;

import com.app.fitrack.config.NutritionixConfig;
import com.app.fitrack.model.MealFoodItem;
import com.app.fitrack.model.NutritionixResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service

public class NutritionixService {

    private final NutritionixConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public NutritionixService(NutritionixConfig config) {
        this.config = config;
    }

    public int getCalories(MealFoodItem foodItem) {

        String query = foodItem.getQuantity() + " " + (foodItem.getUnit()) + " of " + foodItem.getFoodItem();

        String url = "https://trackapi.nutritionix.com/v2/natural/nutrients";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-app-id", config.getAppId());
        headers.set("x-app-key", config.getAppKey());
    
        Map<String, String> body = new HashMap<>();
        body.put("query", query);
    
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
    
        System.out.println("Sending API Request: " + body);
        
        ResponseEntity<NutritionixResponse> response = restTemplate.postForEntity(url, entity, NutritionixResponse.class);
    
        System.out.println("API Response: " + response.getBody());
    
        if (response.getBody() != null && !response.getBody().getFoods().isEmpty()) {
            return (int) Math.round (response.getBody().getFoods().get(0).getNf_calories()) ;
        }
    
        return 0;
    }
}
    
