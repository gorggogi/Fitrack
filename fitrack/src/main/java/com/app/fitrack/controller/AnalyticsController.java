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

        // Calculate macronutrient totals for the last 7 days
        double totalProtein = mealService.getAverageDailyProtein(user, 7);
        double totalCarbs = mealService.getAverageDailyCarbs(user, 7);
        double totalFats = mealService.getAverageDailyFats(user, 7);

        // Get weight trend
        Map<String, Object> recommendations = recommendationService.generateRecommendations(user.getId());
        double actualWeeklyTrend = (double) recommendations.get("actualWeeklyTrend");

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
                caloriesBurned[index] += log.getCaloriesBurned();
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

        // Calculate weight progress
        Double weightProgress = null;
        Double weightTrend = null;
        String weightTrendDescription = null;
        String weightRecommendation = null;
        
        if (measurements.size() >= 2) {
            BodyMeasurement previousMeasurement = measurements.get(1);
            weightProgress = latestMeasurement.getWeight() - previousMeasurement.getWeight();
            
            // Calculate weight trend over the last 4 weeks
            if (measurements.size() >= 4) {
                BodyMeasurement fourWeeksAgo = measurements.get(3);
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    fourWeeksAgo.getDateTime().toLocalDate(),
                    latestMeasurement.getDateTime().toLocalDate()
                );
                
                if (daysBetween > 0) {
                    double totalWeightChange = latestMeasurement.getWeight() - fourWeeksAgo.getWeight();
                    weightTrend = (totalWeightChange / daysBetween) * 7; // Convert to weekly rate
                    
                    // Generate trend description and recommendations
                    if (Math.abs(weightTrend) < 0.1) {
                        weightTrendDescription = "Stable";
                        weightRecommendation = "Your weight is stable. Consider setting specific goals for better progress tracking.";
                    } else if (weightTrend > 0) {
                        if (weightTrend > 0.5) {
                            weightTrendDescription = "Rapid gain";
                            weightRecommendation = "Rapid weight gain detected. Consider adjusting your calorie intake if this wasn't intended.";
                        } else {
                            weightTrendDescription = "Gradual gain";
                            weightRecommendation = "Steady weight gain observed. This is a healthy rate for muscle building.";
                        }
                    } else {
                        if (weightTrend < -0.5) {
                            weightTrendDescription = "Rapid loss";
                            weightRecommendation = "Rapid weight loss detected. Consider increasing calories if this wasn't intended.";
                        } else {
                            weightTrendDescription = "Gradual loss";
                            weightRecommendation = "Steady weight loss observed. This is a healthy rate for fat loss.";
                        }
                    }
                }
            } else {
                // Not enough measurements for trend analysis
                weightTrendDescription = "Insufficient data";
                weightRecommendation = "Keep tracking your measurements! We need at least 4 weeks of data to analyze your weight trend.";
            }
        } else {
            // Only one or no measurements
            weightTrendDescription = "Getting started";
            weightRecommendation = "Welcome to Fitrack! Add more measurements to start tracking your progress.";
        }

        // Calculate macronutrient needs
        double proteinNeeds = latestMeasurement != null ? latestMeasurement.getWeight() * 1.6 : 0;
        double carbNeeds = tdee * 0.4 / 4; // 40% of calories from carbs
        double fatNeeds = tdee * 0.3 / 9; // 30% of calories from fat
        double waterNeeds = latestMeasurement != null ? latestMeasurement.getWeight() * 0.033 : 0;

        // Add data to model
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("currentUserWeight", latestMeasurement != null ? latestMeasurement.getWeight() : null);
        model.addAttribute("weightProgress", weightProgress);
        model.addAttribute("avgDailyCalories", avgDailyCalories);
        model.addAttribute("avgDailyExercise", avgDailyExercise);
        model.addAttribute("tdee", tdee);
        model.addAttribute("totalProtein", totalProtein);
        model.addAttribute("totalCarbs", totalCarbs);
        model.addAttribute("totalFats", totalFats);

        // Add body composition data
        model.addAttribute("currentBmiValue", currentBmiValue);
        model.addAttribute("currentBmiCategory", currentBmiCategory);
        model.addAttribute("weightTrend", weightTrend);
        model.addAttribute("weightTrendDescription", weightTrendDescription);
        model.addAttribute("weightRecommendation", weightRecommendation);
        model.addAttribute("proteinNeeds", proteinNeeds);
        model.addAttribute("carbNeeds", carbNeeds);
        model.addAttribute("fatNeeds", fatNeeds);
        model.addAttribute("waterNeeds", waterNeeds);

        // Add chart data
        model.addAttribute("workoutData", workoutCounts);
        model.addAttribute("caloriesData", caloriesBurned);
        model.addAttribute("chartMeasurementData", chartMeasurementData);
        model.addAttribute("timelineData", timelineData);

        return "analytics";
    }
} 