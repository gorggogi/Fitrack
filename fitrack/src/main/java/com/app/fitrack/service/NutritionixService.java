package com.app.fitrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.app.fitrack.model.MealFoodItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;
import java.util.HashMap;

@Service
public class NutritionixService {
    private static final Logger logger = LoggerFactory.getLogger(NutritionixService.class);

    private final String apiKey;
    private final String appId;
    private final String BASE_URL = "https://trackapi.nutritionix.com/v2";
    private final RestTemplate restTemplate;

    public NutritionixService(@Value("${nutritionix.api.key}") String apiKey,
                              @Value("${nutritionix.api.appId}") String appId,
                              RestTemplateBuilder restTemplateBuilder) {
        this.apiKey = apiKey;
        this.appId = appId;
        this.restTemplate = restTemplateBuilder.build();
        // Log to confirm that keys are being loaded (or not, in case of issues)
        // Be cautious about logging actual keys in production environments
        logger.info("NutritionixService initialized. App ID loaded: {}, API Key loaded: {}", 
                    (this.appId != null && !this.appId.isEmpty()), 
                    (this.apiKey != null && !this.apiKey.isEmpty()));
        if (this.appId == null || this.appId.isEmpty() || this.apiKey == null || this.apiKey.isEmpty()) {
            logger.error("CRITICAL: Nutritionix API ID or Key is missing from properties!");
            // Consider throwing an exception here if these are absolutely critical for app startup
            // throw new IllegalStateException("Nutritionix API credentials are not configured.");
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public double calculateExerciseCalories(String exerciseQuery, double weightKg, int durationMin, Integer age, Double heightCm, String gender) {
        try {
            String url = BASE_URL + "/natural/exercise";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", exerciseQuery);
            
            if (weightKg > 0) {
                requestBody.put("weight_kg", weightKg);
            }
            if (gender != null && !gender.trim().isEmpty()) {
                requestBody.put("gender", gender.toLowerCase());
            }
            if (age != null && age > 0) {
                requestBody.put("age", age);
            }
            if (heightCm != null && heightCm > 0) {
                requestBody.put("height_cm", heightCm);
            }

            logger.info("Sending request to Nutritionix for exercise calorie estimation:");
            logger.info("URL: {}", url);
            logger.info("Request Body: {}", requestBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            logger.info("Nutritionix Response: {}", response.getBody().toString());

            if (response.getBody() != null && response.getBody().has("exercises")) {
                JsonNode exercises = response.getBody().get("exercises");
                if (exercises.isArray() && exercises.size() > 0) {
                    JsonNode exerciseData = exercises.get(0);
                    if (exerciseData.has("nf_calories")) {
                        double calories = exerciseData.get("nf_calories").asDouble();
                        logger.info("Calculated calories: {}", calories);
                        return calories;
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error calculating exercise calories", e);
            return 0;
        }
    }

    public int getCalories(MealFoodItem foodItem) {
        try {
            String url = BASE_URL + "/natural/nutrients";
            String query = String.format("%s %s of %s", 
                foodItem.getQuantity(), 
                foodItem.getUnit(), 
                foodItem.getFoodItem());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);

            logger.info("Sending request to Nutritionix:");
            logger.info("URL: {}", url);
            logger.info("Query: {}", query);
            logger.info("Headers: x-app-id: {}", appId);
            logger.info("Request Body: {}", requestBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            logger.info("Nutritionix Response Status: {}", response.getStatusCode());
            logger.info("Nutritionix Response Body: {}", response.getBody());

            if (response.getBody() != null && response.getBody().has("foods")) {
                JsonNode foods = response.getBody().get("foods");
                if (foods.isArray() && foods.size() > 0) {
                    JsonNode food = foods.get(0);
                    // Set calories
                    double rawCalories = food.get("nf_calories").asDouble();
                    int calories = (int) Math.round(rawCalories);
                    foodItem.setCalories(calories);
                    
                    logger.info("Food item: {}, Quantity: {}, Unit: {}, Raw calories: {}, Rounded calories: {}", 
                        foodItem.getFoodItem(), 
                        foodItem.getQuantity(), 
                        foodItem.getUnit(), 
                        rawCalories, 
                        calories);
                    
                    if (food.has("nf_protein")) {
                        foodItem.setProtein(food.get("nf_protein").asDouble());
                    }
                    if (food.has("nf_total_carbohydrate")) {
                        foodItem.setCarbs(food.get("nf_total_carbohydrate").asDouble());
                    }
                    if (food.has("nf_total_fat")) {
                        foodItem.setFat(food.get("nf_total_fat").asDouble());
                    }
                    return calories;
                } else {
                    logger.warn("No foods found in response for query: {}", query);
                }
            } else {
                logger.warn("Invalid response format from Nutritionix API for query: {}", query);
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error getting food calories for item: {}", foodItem.getFoodItem(), e);
            return 0;
        }
    }
}
