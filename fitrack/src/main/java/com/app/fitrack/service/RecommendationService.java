package com.app.fitrack.service;

import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.repository.BodyMeasurementRepository;
import com.app.fitrack.repository.MealRepository;
import com.app.fitrack.repository.WorkoutLogRepository;
import com.app.fitrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private BodyMeasurementRepository bodyMeasurementRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    public String generateRecommendations(Long userId) {
        // Get user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return "User not found.";
        }

        // Get user's latest measurement
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserOrderByDateTimeDesc(user);
        BodyMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);
        if (latestMeasurement == null) {
            return "No body measurements found. Please add your initial measurements.";
        }

        // Get last 7 days of data
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        // Calculate average daily calories
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        double avgDailyCalories = calculateAverageDailyCalories(recentMeals);

        // Calculate average daily exercise
        List<WorkoutLog> recentWorkouts = workoutLogRepository.findByUserAndCompletedAtBetween(user, startDate, endDate);
        double avgDailyExercise = calculateAverageDailyExercise(recentWorkouts);

        // Calculate TDEE
        double tdee = userService.calculateTDEE(user, userService.calculateDynamicActivityFactor(user, 7));

        // Generate recommendations
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Current Status:\n");
        recommendations.append(String.format("- Weight: %.1f kg\n", latestMeasurement.getWeight()));
        recommendations.append(String.format("- Average Daily Calories: %.0f kcal\n", avgDailyCalories));
        recommendations.append(String.format("- Average Daily Exercise: %.0f kcal\n", avgDailyExercise));
        recommendations.append(String.format("- Estimated TDEE: %.0f kcal\n", tdee));

        // Calculate daily balance
        double dailyBalance = avgDailyCalories - (tdee + avgDailyExercise);
        double weeklyWeightChange = (dailyBalance * 7) / 7700; // 7700 kcal per kg

        recommendations.append("\nWeight Change Projection:\n");
        if (weeklyWeightChange < -0.5) {
            recommendations.append("- Rapid weight loss detected (>0.5 kg/week)\n");
            recommendations.append("- Consider increasing calories by " + Math.abs(Math.round(dailyBalance)) + " kcal for safer rate\n");
        } else if (weeklyWeightChange < -0.1) {
            recommendations.append("- Moderate weight loss detected\n");
            recommendations.append("- Current rate is sustainable\n");
        } else if (weeklyWeightChange > 0.5) {
            recommendations.append("- Rapid weight gain detected (>0.5 kg/week)\n");
            recommendations.append("- Consider reducing calories by " + Math.round(dailyBalance) + " kcal for slower gain\n");
        } else if (weeklyWeightChange > 0.1) {
            recommendations.append("- Moderate weight gain detected\n");
            recommendations.append("- Current rate is sustainable\n");
        } else {
            recommendations.append("- Weight appears stable\n");
        }

        // Exercise recommendations
        recommendations.append("\nExercise Recommendations:\n");
        if (avgDailyExercise < 100) {
            recommendations.append("- Consider increasing workout frequency\n");
            recommendations.append("- Aim for 3-4 sessions per week\n");
        } else if (avgDailyExercise > 500) {
            recommendations.append("- High exercise volume detected\n");
            recommendations.append("- Ensure adequate rest and recovery\n");
        } else {
            recommendations.append("- Current exercise level is appropriate\n");
        }

        // Nutrition recommendations
        recommendations.append("\nNutrition Recommendations:\n");
        if (avgDailyCalories < tdee * 0.8) {
            recommendations.append("- Calorie intake is very low\n");
            recommendations.append("- Consider increasing by " + Math.round(tdee * 0.2) + " kcal\n");
        } else if (avgDailyCalories > tdee * 1.2) {
            recommendations.append("- Calorie intake is high\n");
            recommendations.append("- Consider reducing by " + Math.round(avgDailyCalories - tdee * 1.2) + " kcal\n");
        }

        // Protein recommendations
        double proteinNeeds = latestMeasurement.getWeight() * 1.6; // 1.6g per kg
        recommendations.append(String.format("- Target protein intake: %.0f g per day\n", proteinNeeds));

        return recommendations.toString();
    }

    private double calculateAverageDailyCalories(List<Meal> meals) {
        if (meals.isEmpty()) return 0;
        return meals.stream()
            .mapToDouble(Meal::getTotalCalories)
            .average()
            .orElse(0);
    }

    private double calculateAverageDailyExercise(List<WorkoutLog> workouts) {
        if (workouts.isEmpty()) return 0;
        return workouts.stream()
            .mapToDouble(WorkoutLog::getBurnedCalories)
            .average()
            .orElse(0);
    }
} 