package com.app.fitrack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.app.fitrack.model.MealFoodItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

@Service
public class NutritionixService {
    private static final Logger logger = LoggerFactory.getLogger(NutritionixService.class);

    @Value("${nutritionix.app-id}")
    private String appId;

    @Value("${nutritionix.app-key}")
    private String appKey;

    private final String BASE_URL = "https://trackapi.nutritionix.com/v2";
    private final RestTemplate restTemplate;
    public NutritionixService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", appKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public double calculateExerciseCalories(String exercise, double weightKg, int durationMin, String gender) {
        try {
            String url = BASE_URL + "/natural/exercise";
            String query = String.format("%s for %d minutes", exercise, durationMin);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("weight_kg", weightKg);
            requestBody.put("gender", gender);

            logger.info("Sending request to Nutritionix:");
            logger.info("URL: {}", url);
            logger.info("Query: {}", query);
            logger.info("Weight: {}", weightKg);
            logger.info("Gender: {}", gender);

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

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            logger.info("Nutritionix Response: {}", response.getBody().toString());

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
                    
                    // Set macronutrients
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
                }
            }
            return 0;
        } catch (Exception e) {
            logger.error("Error getting food calories", e);
            return 0;
        }
    }
}
