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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
        try {
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
        } catch (Exception e) {
            // Log error and fall back to basic calculation
            return calculateBasicExerciseCalories(weightKg, durationMin);
        }
        return 0.0;
    }

    public Map<String, Object> getNutritionRecommendations(double weightKg, double heightCm, int age, String gender, String activityLevel, String goal) {
        try {
            String url = BASE_URL + "/utils/nutrition_recommendations";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("weight_kg", weightKg);
            requestBody.put("height_cm", heightCm);
            requestBody.put("age", age);
            requestBody.put("gender", gender);
            requestBody.put("activity_level", activityLevel);
            requestBody.put("goal", goal);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, createHeaders());
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
            );

            if (response.getBody() != null) {
                return objectMapper.convertValue(response.getBody(), Map.class);
            }
        } catch (Exception e) {
            // Log error and fall back to basic calculations
        }
        return calculateBasicNutritionNeeds(weightKg, heightCm, age, gender, activityLevel, goal);
    }

    private double calculateBasicExerciseCalories(double weightKg, int durationMin) {
        // Basic MET-based calculation as fallback
        double MET = 3.0; // Moderate intensity
        return (MET * 3.5 * weightKg * durationMin) / 200.0;
    }

    private Map<String, Object> calculateBasicNutritionNeeds(double weightKg, double heightCm, int age, String gender, String activityLevel, String goal) {
        // Basic BMR using Mifflin-St Jeor Formula
        double bmr;
        if (gender.equalsIgnoreCase("male")) {
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }

        // Activity multiplier
        double activityMultiplier = switch(activityLevel.toLowerCase()) {
            case "sedentary" -> 1.2;
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "very" -> 1.725;
            case "extra" -> 1.9;
            default -> 1.2;
        };

        double tdee = bmr * activityMultiplier;
        double targetCalories = switch(goal.toLowerCase()) {
            case "loss" -> tdee - 500;
            case "gain" -> tdee + 500;
            default -> tdee;
        };

        return Map.of(
            "tdee", tdee,
            "target_calories", targetCalories,
            "protein_g", weightKg * 1.6,
            "carbs_g", (targetCalories * 0.4) / 4,
            "fats_g", (targetCalories * 0.3) / 9
        );
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
        } catch (Exception e) {
            // Log error and fall back to basic estimation
            return estimateBasicCalories(foodItem);
        }
        return 0;
    }

    private int estimateBasicCalories(MealFoodItem foodItem) {
        // Basic calorie estimation based on common food categories
        // This is a very rough estimate and should only be used as fallback
        String foodLower = foodItem.getFoodItem().toLowerCase();
        double quantity = foodItem.getQuantity();
        String unit = foodItem.getUnit().toLowerCase();

        // Convert to grams if necessary
        if (unit.equals("oz")) {
            quantity *= 28.35; // Convert oz to g
        } else if (unit.equals("lb")) {
            quantity *= 453.6; // Convert lb to g
        } else if (!unit.equals("g")) {
            // For other units, use a standard portion size
            quantity = 100;
        }

        // Very basic calorie estimation per 100g
        if (foodLower.contains("chicken") || foodLower.contains("fish")) {
            return (int) (quantity * 1.65); // ~165 kcal per 100g
        } else if (foodLower.contains("rice") || foodLower.contains("pasta")) {
            return (int) (quantity * 1.3); // ~130 kcal per 100g
        } else if (foodLower.contains("vegetable") || foodLower.contains("salad")) {
            return (int) (quantity * 0.25); // ~25 kcal per 100g
        } else if (foodLower.contains("fruit")) {
            return (int) (quantity * 0.5); // ~50 kcal per 100g
        } else {
            return (int) (quantity * 1.0); // Default 100 kcal per 100g
        }
    }
}
    
