package com.app.fitrack.service;

import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.model.Goal;
import com.app.fitrack.repository.BodyMeasurementRepository;
import com.app.fitrack.repository.MealRepository;
import com.app.fitrack.repository.WorkoutLogRepository;
import com.app.fitrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private GoalService goalService;

    @Autowired
    private NutritionixService nutritionixService;

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

        // Get user's active goals
        List<Goal> activeGoals = goalService.getActiveGoals(user);
        String goalType = "maintain";
        if (!activeGoals.isEmpty()) {
            for (Goal goal : activeGoals) {
                if (goal.getGoalType().equalsIgnoreCase("WEIGHT_LOSS")) {
                    goalType = "loss";
                    break;
                } else if (goal.getGoalType().equalsIgnoreCase("WEIGHT_GAIN")) {
                    goalType = "gain";
                    break;
                }
            }
        }

        // Get nutrition recommendations from Nutritionix API
        Map<String, Object> nutritionRecs = nutritionixService.getNutritionRecommendations(
            latestMeasurement.getWeight(),
            user.getHeight(),
            user.getAge(),
            user.getGender(),
            calculateActivityLevel(avgDailyExercise),
            goalType
        );

        double tdee = (double) nutritionRecs.get("tdee");
        double targetCalories = (double) nutritionRecs.get("target_calories");
        double proteinNeeds = (double) nutritionRecs.get("protein_g");
        double carbNeeds = (double) nutritionRecs.get("carbs_g");
        double fatNeeds = (double) nutritionRecs.get("fats_g");

        // Calculate daily balance and weight change
        double calorieBalance = avgDailyCalories - tdee;
        double projectedWeeklyChange = calculateWeeklyWeightChange(calorieBalance);

        // Generate recommendations
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Current Status:\n");
        recommendations.append(String.format("- Weight: %.1f kg\n", latestMeasurement.getWeight()));
        recommendations.append(String.format("- Average Daily Calories: %.0f kcal\n", avgDailyCalories));
        recommendations.append(String.format("- Average Daily Exercise: %.0f kcal\n", avgDailyExercise));
        recommendations.append(String.format("- Estimated TDEE: %.0f kcal\n", tdee));
        recommendations.append(String.format("- Current Calorie Surplus/Deficit: %+.0f kcal\n", calorieBalance));
        recommendations.append(String.format("- Projected Weekly Change: %+.1f kg/week\n", projectedWeeklyChange));

        // Get user's goal type (if any)
        String primaryGoalType = null;
        Double targetValue = null;
        if (!activeGoals.isEmpty()) {
            // Find weight-related goal if it exists
            for (Goal goal : activeGoals) {
                if (goal.getGoalType().equalsIgnoreCase("WEIGHT_LOSS") || 
                    goal.getGoalType().equalsIgnoreCase("WEIGHT_GAIN")) {
                    primaryGoalType = goal.getGoalType().toUpperCase();
                    targetValue = goal.getTargetValue();
                    break;
                }
            }
        }

        // Generate goal-specific recommendations
        recommendations.append("\nGoal-Specific Recommendations:\n");
        if (!activeGoals.isEmpty()) {
            for (Goal goal : activeGoals) {
                switch (goal.getGoalType().toUpperCase()) {
                    case "WEIGHT_LOSS":
                        recommendations.append(String.format("- Weight Loss Goal: %.1f kg by %s\n", 
                            goal.getTargetValue(), goal.getTargetDate().toLocalDate()));
                        if (projectedWeeklyChange > -0.5) {
                            recommendations.append("- Consider increasing your calorie deficit slightly\n");
                            recommendations.append("- Aim for a 300-500 kcal daily deficit\n");
                        }
                        break;
                    case "WEIGHT_GAIN":
                        recommendations.append(String.format("- Weight Gain Goal: %.1f kg by %s\n", 
                            goal.getTargetValue(), goal.getTargetDate().toLocalDate()));
                        
                        // Calculate surplus and weekly rate
                        double currentSurplus = avgDailyCalories - tdee;
                        double weeklyGainRate = projectedWeeklyChange;
                        
                        if (currentSurplus < 200) {
                            recommendations.append("- Current surplus is too low for optimal gains\n");
                            recommendations.append("- Consider increasing your calorie surplus\n");
                            recommendations.append(String.format("- Start with %.0f-%.0f kcal above maintenance\n", 300.0, 500.0));
                            recommendations.append("- Adjust based on weekly weight changes\n");
                        } else if (currentSurplus > 750 && weeklyGainRate > 0.7) {
                            // Only suggest reducing if gaining more than 0.7kg/week
                            recommendations.append("- Current rate of gain might lead to excess fat\n");
                            recommendations.append("- Consider a slightly smaller surplus for better results\n");
                            recommendations.append("- Focus on progressive overload in training\n");
                            recommendations.append("- Monitor body composition changes\n");
                        } else if (currentSurplus >= 300 && weeklyGainRate >= 0.2 && weeklyGainRate <= 0.7) {
                            recommendations.append("- Current progress is good for muscle gain\n");
                            recommendations.append("- Maintain current intake while monitoring progress\n");
                            recommendations.append("- Focus on progressive overload in training\n");
                            recommendations.append("- Ensure adequate rest between sessions\n");
                        } else {
                            recommendations.append("- Current intake is supporting your goal\n");
                            recommendations.append("- Monitor weekly weight changes\n");
                            recommendations.append("- Adjust calories if progress stalls\n");
                        }
                        break;
                    case "EXERCISE":
                        recommendations.append(String.format("- Exercise Goal: %s\n", goal.getDescription()));
                        if (avgDailyExercise < goal.getTargetValue()) {
                            recommendations.append("- Increase workout frequency or intensity\n");
                            recommendations.append("- Consider adding 1-2 more sessions per week\n");
                        }
                        break;
                    case "NUTRITION":
                        recommendations.append(String.format("- Nutrition Goal: %s\n", goal.getDescription()));
                        if (Math.abs(avgDailyCalories - goal.getTargetValue()) > 100) {
                            recommendations.append(String.format("- Adjust daily calories to %.0f kcal\n", goal.getTargetValue()));
                        }
                        break;
                }
            }
        } else {
            recommendations.append("- No active goals set. Consider setting specific goals for better recommendations.\n");
        }

        // General recommendations based on current status
        recommendations.append("\nWeight Change Projection:\n");
        if (projectedWeeklyChange < -0.5) {
            recommendations.append("- Rapid weight loss detected (>0.5 kg/week)\n");
            if (primaryGoalType != null && primaryGoalType.equals("WEIGHT_LOSS")) {
                recommendations.append("- Current weight loss rate is faster than recommended\n");
                recommendations.append("- Consider increasing calories by " + Math.abs(Math.round(calorieBalance - 500)) + " kcal for a safer rate\n");
            } else {
                recommendations.append("- Consider increasing calories by " + Math.abs(Math.round(calorieBalance)) + " kcal to maintain weight\n");
            }
        } else if (projectedWeeklyChange < -0.1) {
            recommendations.append("- Moderate weight loss detected\n");
            if (primaryGoalType != null && primaryGoalType.equals("WEIGHT_LOSS")) {
                recommendations.append("- Current rate is sustainable\n");
            } else {
                recommendations.append("- Consider increasing calories if weight loss is not intended\n");
            }
        } else if (projectedWeeklyChange > 0.5) {
            recommendations.append("- Rapid weight gain detected (>0.5 kg/week)\n");
            if (primaryGoalType != null && primaryGoalType.equals("WEIGHT_GAIN")) {
                recommendations.append("- Current rate exceeds optimal range for lean gains\n");
                recommendations.append(String.format("- Consider reducing to %.0f-%.0f kcal above maintenance\n", 300.0, 500.0));
            } else {
                recommendations.append("- Consider reducing calories by " + Math.round(calorieBalance) + " kcal to maintain weight\n");
            }
        } else if (projectedWeeklyChange > 0.1) {
            recommendations.append("- Moderate weight gain detected\n");
            if (primaryGoalType != null && primaryGoalType.equals("WEIGHT_GAIN")) {
                recommendations.append("- Current rate is within optimal range\n");
                recommendations.append("- Focus on progressive overload in training\n");
            } else {
                recommendations.append("- Consider reducing calories if weight gain is not intended\n");
            }
        } else {
            recommendations.append("- Weight appears stable (±0.1 kg/week)\n");
            if (primaryGoalType != null) {
                if (primaryGoalType.equals("WEIGHT_GAIN")) {
                    recommendations.append("- Consider increasing calories to support your goal\n");
                } else if (primaryGoalType.equals("WEIGHT_LOSS")) {
                    recommendations.append("- Consider decreasing calories to support your goal\n");
                }
            } else {
                recommendations.append("- Good for maintenance\n");
            }
        }

        // Exercise recommendations
        recommendations.append("\nExercise Recommendations:\n");
        if (avgDailyExercise < 100) {
            recommendations.append("- Consider increasing workout frequency\n");
            recommendations.append("- Aim for 3-4 sessions per week\n");
            recommendations.append("- Start with 30-minute sessions\n");
        } else if (avgDailyExercise > 500) {
            recommendations.append("- High exercise volume detected\n");
            recommendations.append("- Ensure adequate rest and recovery\n");
            recommendations.append("- Consider incorporating active recovery days\n");
        } else {
            recommendations.append("- Current exercise level is appropriate\n");
            recommendations.append("- Focus on maintaining consistency\n");
            recommendations.append("- Consider varying workout intensity\n");
        }

        // Nutrition recommendations
        recommendations.append("\nNutrition Recommendations:\n");
        if (primaryGoalType != null) {
            if (primaryGoalType.equals("WEIGHT_LOSS")) {
                if (avgDailyCalories > tdee - 200) {
                    recommendations.append("- To support your weight loss goal:\n");
                    recommendations.append(String.format("- Aim for %.0f-%.0f kcal per day\n", tdee - 500, tdee - 300));
                    recommendations.append("- Focus on protein-rich, filling foods\n");
                    recommendations.append("- Include plenty of vegetables for volume\n");
                } else if (avgDailyCalories < tdee - 750) {
                    recommendations.append("- Current deficit may be too aggressive:\n");
                    recommendations.append("- Consider increasing calories slightly\n");
                    recommendations.append("- Ensure adequate protein and nutrients\n");
                } else {
                    recommendations.append("- Current calorie level supports your goal\n");
                    recommendations.append("- Focus on food quality and meal timing\n");
                }
            } else if (primaryGoalType.equals("WEIGHT_GAIN")) {
                double surplus = avgDailyCalories - tdee;
                double weeklyGainRate = projectedWeeklyChange;
                double optimalProtein = latestMeasurement.getWeight() * 2.2; // Increased to 2.2g/kg for optimal gains
                
                recommendations.append("Nutrition Strategy for Muscle Gain:\n");
                if (weeklyGainRate < 0.2) {
                    recommendations.append("- Weight gain rate is below optimal\n");
                    recommendations.append(String.format("- Consider increasing to %.0f-%.0f kcal per day\n", tdee + 300, tdee + 500));
                    recommendations.append("Key focus areas:\n");
                    recommendations.append("1. Increase calorie-dense foods:\n");
                    recommendations.append("   • Nuts, nut butters, olive oil\n");
                    recommendations.append("   • Whole grains, oats, rice\n");
                    recommendations.append("   • Lean proteins, dairy\n");
                } else if (weeklyGainRate > 0.7 && surplus > 750) {
                    recommendations.append("- Current progress indicates rapid gains\n");
                    recommendations.append("- This can work if you're:\n");
                    recommendations.append("   • New to training\n");
                    recommendations.append("   • Returning after a break\n");
                    recommendations.append("   • Naturally lean/hard gainer\n");
                    recommendations.append("Otherwise, consider:\n");
                    recommendations.append(String.format("- Gradual reduction to %.0f-%.0f kcal per day\n", tdee + 300, tdee + 500));
                    recommendations.append("- Increasing training intensity\n");
                } else {
                    recommendations.append("- Current nutrition plan is supporting growth\n");
                    recommendations.append("Optimization tips:\n");
                    recommendations.append("1. Meal timing:\n");
                    recommendations.append("   • Pre-workout: Complex carbs\n");
                    recommendations.append("   • Post-workout: Protein + fast carbs\n");
                    recommendations.append("2. Recovery:\n");
                    recommendations.append("   • 7-9 hours sleep\n");
                    recommendations.append("   • Proper hydration\n");
                }
                
                recommendations.append("\nProtein Requirements:\n");
                recommendations.append(String.format("- Aim for %.0f g protein daily\n", optimalProtein));
                recommendations.append("- Split across 4-6 meals\n");
                recommendations.append("- Include complete protein sources\n");
            }
        } else {
            // No weight-related goal, give general recommendations
            if (avgDailyCalories < tdee * 0.8) {
                recommendations.append("- Calorie intake appears too low\n");
                recommendations.append(String.format("- Consider increasing by %.0f kcal\n", tdee * 0.2));
                recommendations.append("- Focus on nutrient-dense foods\n");
            } else if (avgDailyCalories > tdee * 1.2) {
                recommendations.append("- Calorie intake appears high\n");
                recommendations.append(String.format("- Consider reducing by %.0f kcal\n", avgDailyCalories - tdee));
                recommendations.append("- Focus on portion control\n");
            } else {
                recommendations.append("- Current calorie intake is appropriate\n");
                recommendations.append("- Focus on food quality and timing\n");
            }
        }

        // Macronutrient recommendations
        recommendations.append("\nMacronutrient Targets:\n");
        if (primaryGoalType != null && primaryGoalType.equals("WEIGHT_GAIN")) {
            double optimalProtein = latestMeasurement.getWeight() * 2.0;
            double optimalFat = (targetCalories * 0.25) / 9; // 25% from fats
            double optimalCarbs = (targetCalories - (optimalProtein * 4) - (optimalFat * 9)) / 4;
            
            recommendations.append(String.format("- Protein: %.0f g per day (2g per kg for muscle gain)\n", optimalProtein));
            recommendations.append(String.format("- Carbohydrates: %.0f g per day (for training energy)\n", optimalCarbs));
            recommendations.append(String.format("- Fats: %.0f g per day (25%% of calories)\n", optimalFat));
        } else {
            recommendations.append(String.format("- Protein: %.0f g per day\n", proteinNeeds));
            recommendations.append(String.format("- Carbohydrates: %.0f g per day\n", carbNeeds));
            recommendations.append(String.format("- Fats: %.0f g per day\n", fatNeeds));
        }

        return recommendations.toString();
    }

    private double calculateAverageDailyCalories(List<Meal> meals) {
        if (meals.isEmpty()) return 0;
        
        // Group meals by date and sum calories for each day
        Map<LocalDateTime, Double> dailyCalories = meals.stream()
            .collect(Collectors.groupingBy(
                meal -> meal.getDateTime().toLocalDate().atStartOfDay(),
                Collectors.summingDouble(Meal::getTotalCalories)
            ));

        // Calculate average over the number of days with data
        return dailyCalories.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    private double calculateAverageDailyExercise(List<WorkoutLog> workouts) {
        if (workouts.isEmpty()) return 0;
        
        // Group workouts by date and sum calories for each day
        Map<LocalDateTime, Double> dailyExercise = workouts.stream()
            .collect(Collectors.groupingBy(
                workout -> workout.getCompletedAt().toLocalDate().atStartOfDay(),
                Collectors.summingDouble(WorkoutLog::getBurnedCalories)
            ));

        // Calculate average over the number of days with data
        return dailyExercise.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    private double calculateWeeklyWeightChange(double dailyBalance) {
        // Use a standard energy density since we don't have body composition data
        double energyDensity = 7700; // Standard estimate of kcal per kg
        return (dailyBalance * 7) / energyDensity;
    }

    private String calculateActivityLevel(double avgDailyExercise) {
        if (avgDailyExercise < 100) {
            return "sedentary";
        } else if (avgDailyExercise < 300) {
            return "light";
        } else if (avgDailyExercise < 500) {
            return "moderate";
        } else if (avgDailyExercise < 800) {
            return "very";
        } else {
            return "extra";
        }
    }
} 