package com.app.fitrack.controller;

import com.app.fitrack.model.User;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
public class AnalyticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private MealService mealService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/user/analytics")
    public String showAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        // Get user's latest measurement
        List<BodyMeasurement> measurements = bodyMeasurementService.getMeasurementsForUser(user);
        BodyMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);

        // Get last 7 days of data
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        // Calculate average daily calories
        double avgDailyCalories = mealService.getAverageDailyCalories(user, 7);

        // Calculate average daily exercise
        double avgDailyExercise = workoutService.getAverageDailyExerciseCalories(user, 7);

        // Calculate TDEE
        double tdee = userService.calculateTDEE(user, userService.calculateDynamicActivityFactor(user, 7));

        // Get recommendations
        String recommendations = recommendationService.generateRecommendations(user.getId());
        
        // Split recommendations into sections
        String[] sections = recommendations.split("\n\n");
        String goalRecommendations = "";
        String weightRecommendation = "";
        String nutritionRecommendation = "";
        String exerciseRecommendation = "";

        for (String section : sections) {
            if (section.startsWith("Goal-Specific Recommendations:")) {
                goalRecommendations = section.replace("Goal-Specific Recommendations:\n", "");
            } else if (section.startsWith("Weight Change Projection:")) {
                weightRecommendation = section.replace("Weight Change Projection:\n", "");
            } else if (section.startsWith("Nutrition Recommendations:")) {
                nutritionRecommendation = section.replace("Nutrition Recommendations:\n", "");
            } else if (section.startsWith("Exercise Recommendations:")) {
                exerciseRecommendation = section.replace("Exercise Recommendations:\n", "");
            }
        }

        // Prepare workout frequency data
        int[] workoutCounts = new int[7];
        int[] caloriesBurned = new int[7];
        LocalDate today = LocalDate.now();

        // Get workout logs for the last 7 days
        List<WorkoutLog> recentLogs = workoutService.getWorkoutLogsForUser(user, startDate, endDate);
        for (WorkoutLog log : recentLogs) {
            LocalDate logDate = log.getCompletedAt().toLocalDate();
            long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(logDate, today);
            if (daysAgo >= 0 && daysAgo < 7) {
                int index = 6 - (int)daysAgo;
                workoutCounts[index]++;
                caloriesBurned[index] += log.getBurnedCalories();
            }
        }

        // Format measurement data for charts
        List<Map<String, Object>> chartMeasurementData = measurements.stream()
            .sorted((m1, m2) -> m1.getDateTime().compareTo(m2.getDateTime()))
            .map(m -> {
                Map<String, Object> point = new HashMap<>();
                point.put("dateTime", m.getDateTime().toString());
                point.put("weight", m.getWeight());
                point.put("userHeight", user.getHeight());
                return point;
            })
            .collect(Collectors.toList());

        // Format timeline data
        DateTimeFormatter timelineDateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        List<Map<String, Object>> timelineData = measurements.stream()
            .sorted((m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime()))
            .map(m -> {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", m.getDateTime().format(timelineDateFormatter));
                entry.put("weight", m.getWeight());
                entry.put("notes", m.getNotes() != null ? m.getNotes() : "");
                return entry;
            })
            .collect(Collectors.toList());

        // Calculate BMI and category
        double currentBmiValue = bodyMeasurementService.calculateCurrentBMI(user);
        String currentBmiCategory = bodyMeasurementService.getBMICategory(currentBmiValue);

        // Calculate weekly weight change
        double dailyBalance = avgDailyCalories - (tdee + avgDailyExercise);
        double weeklyWeightChange = (dailyBalance * 7) / 7700.0; // 7700 kcal per kg

        // Calculate macronutrient needs
        double proteinNeeds = latestMeasurement != null ? latestMeasurement.getWeight() * 1.6 : 0;
        double carbNeeds = tdee * 0.4 / 4; // 40% of calories from carbs
        double fatNeeds = tdee * 0.3 / 9; // 30% of calories from fat
        double waterNeeds = latestMeasurement != null ? latestMeasurement.getWeight() * 0.033 : 0;

        // Calculate weight projection
        List<Map<String, Object>> projectionData = new ArrayList<>();
        if (latestMeasurement != null) {
            double dailyWeightChangeKg = dailyBalance / 7700.0;
            double currentWeight = latestMeasurement.getWeight();
            LocalDateTime currentDate = latestMeasurement.getDateTime();

            // Add the latest actual point
            Map<String, Object> startPoint = new HashMap<>();
            startPoint.put("date", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            startPoint.put("weight", currentWeight);
            projectionData.add(startPoint);

            // Add 14 days of projection
            for (int i = 1; i <= 14; i++) {
                currentDate = currentDate.plusDays(1);
                currentWeight += dailyWeightChangeKg;
                
                Map<String, Object> projectedPoint = new HashMap<>();
                projectedPoint.put("date", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                projectedPoint.put("weight", Math.round(currentWeight * 10.0) / 10.0);
                projectionData.add(projectedPoint);
            }
        }

        // Add data to model
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("currentUserWeight", latestMeasurement != null ? latestMeasurement.getWeight() : null);
        model.addAttribute("avgDailyCalories", avgDailyCalories);
        model.addAttribute("avgDailyExercise", avgDailyExercise);
        model.addAttribute("tdee", tdee);
        model.addAttribute("goalRecommendations", goalRecommendations);
        model.addAttribute("weightRecommendation", weightRecommendation);
        model.addAttribute("nutritionRecommendation", nutritionRecommendation);
        model.addAttribute("exerciseRecommendation", exerciseRecommendation);

        // Add body composition data
        model.addAttribute("currentBmiValue", currentBmiValue);
        model.addAttribute("currentBmiCategory", currentBmiCategory);
        model.addAttribute("weeklyWeightChange", weeklyWeightChange);
        model.addAttribute("proteinNeeds", proteinNeeds);
        model.addAttribute("carbNeeds", carbNeeds);
        model.addAttribute("fatNeeds", fatNeeds);
        model.addAttribute("waterNeeds", waterNeeds);
        model.addAttribute("projectionData", projectionData);

        // Add chart data
        model.addAttribute("workoutData", workoutCounts);
        model.addAttribute("caloriesData", caloriesBurned);
        model.addAttribute("chartMeasurementData", chartMeasurementData);
        model.addAttribute("timelineData", timelineData);
        model.addAttribute("showProjectionDefault", true);

        return "analytics";
    }
} 