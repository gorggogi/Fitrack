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

import java.util.Map;
import java.util.HashMap;

@Service
public class NutritionixService {

    @Value("${nutritionix.app.id}")
    private String appId;

    @Value("${nutritionix.app.key}")
    private String appKey;

    private final String BASE_URL = "https://trackapi.nutritionix.com/v2";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NutritionixService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-app-id", appId);
        headers.set("x-app-key", appKey);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    public double calculateExerciseCalories(String exercise, double weightKg, int durationMin, String gender) {

            String url = BASE_URL + "/natural/exercise";
            String query = String.format("%s %d minutes", exercise, durationMin);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            requestBody.put("weight_kg", weightKg);
            requestBody.put("gender", gender);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            if (response.getBody() != null && response.getBody().has("exercises")) {
                JsonNode exercises = response.getBody().get("exercises");
                if (exercises.isArray() && exercises.size() > 0) {
                    return exercises.get(0).get("nf_calories").asDouble();
                }
            }
            return 0;
        }
    public int getCalories(MealFoodItem foodItem) {
     
            String url = BASE_URL + "/natural/nutrients";
            String query = String.format("%s %s of %s", 
                foodItem.getQuantity(), 
                foodItem.getUnit(), 
                foodItem.getFoodItem());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            if (response.getBody() != null && response.getBody().has("foods")) {
                JsonNode foods = response.getBody().get("foods");
                if (foods.isArray() && foods.size() > 0) {
                    return (int) Math.round(foods.get(0).get("nf_calories").asDouble());
                }
            }
            return 0;
        }
    }
