package com.app.fitrack.service;

import com.app.fitrack.config.NutritionixConfig;
import com.app.fitrack.model.ExerciseResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExerciseService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);
    private final NutritionixConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public ExerciseService(NutritionixConfig config) {
        this.config = config;
    }

    public int getBurnedCalories(String workoutName, double duration) {
        String query = duration + " minutes of " + workoutName;
        String url = "https://trackapi.nutritionix.com/v2/natural/exercise";
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-app-id", config.getAppId());
        headers.set("x-app-key", config.getAppKey());
    
        Map<String, String> body = new HashMap<>();
        body.put("query", query);
    
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
    
        logger.info("Sending Exercise API Request: {}", body);
        
        try {
            ResponseEntity<ExerciseResponse> response = restTemplate.postForEntity(url, entity, ExerciseResponse.class);
            logger.info("Exercise API Response: {}", response.getBody());
    
            if (response.getBody() != null && !response.getBody().getExercises().isEmpty()) {
                return (int) Math.round(response.getBody().getExercises().get(0).getNf_calories());
            }
            
            logger.warn("No exercise data found in response");
            return estimateBasicCalories(workoutName, duration);
            
        } catch (RestClientException e) {
            logger.error("Error calling Nutritionix API: {}", e.getMessage());
            return estimateBasicCalories(workoutName, duration);
        }
    }

    private int estimateBasicCalories(String workoutName, double duration) {
        // Basic MET-based calculation as fallback
        double MET;
        String workout = workoutName.toLowerCase();
        
        if (workout.contains("run") || workout.contains("jog")) {
            MET = 8.0; // Running/jogging
        } else if (workout.contains("walk")) {
            MET = 3.5; // Walking
        } else if (workout.contains("swim")) {
            MET = 6.0; // Swimming
        } else if (workout.contains("bike") || workout.contains("cycling")) {
            MET = 7.0; // Cycling
        } else if (workout.contains("weight") || workout.contains("strength")) {
            MET = 4.0; // Weight training
        } else {
            MET = 4.0; // Default moderate intensity
        }

        // Calories = MET × weight (kg) × time (hours)
        // Using 70kg as default weight if user weight is not available
        double hours = duration / 60.0;
        return (int) Math.round(MET * 70 * hours);
    }
} 