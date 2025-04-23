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
import java.util.HashMap;
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
    private UserRepository userRepository;

    @Autowired
    private GoalService goalService;

    @Autowired
    private NutritionixService nutritionixService;

    public Map<String, Object> generateRecommendations(Long userId) {
        // Get user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("recommendations", "User not found.");
            error.put("actualWeeklyTrend", 0.0);
            return error;
        }

        // Get user's latest measurement
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserOrderByDateTimeDesc(user);
        BodyMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);
        if (latestMeasurement == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("recommendations", "No body measurements found. Please add your initial measurements.");
            error.put("actualWeeklyTrend", 0.0);
            return error;
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

        // Calculate daily balance and weight changes
        double calorieBalance = avgDailyCalories - tdee;
        Map<String, Object> projection = calculateWeeklyWeightChange(calorieBalance, avgDailyExercise, goalType);
        double projectedWeeklyChange = (double) projection.get("projection");
        double confidence = (double) projection.get("confidence");
        double[] range = (double[]) projection.get("range");
        
        // Calculate actual weight trend (4-week average)
        double actualWeeklyTrend = calculateActualWeightTrend(user);
        
        // Calculate weight loss progress if goal exists
        double weightLossProgress = 0;
        if (!activeGoals.isEmpty()) {
            for (Goal goal : activeGoals) {
                if (goal.getGoalType().equalsIgnoreCase("WEIGHT_LOSS")) {
                    double currentWeight = latestMeasurement.getWeight();
                    double targetWeight = goal.getTargetValue();
                    double initialWeight = getInitialWeight(user); // Need to implement this
                    if (initialWeight > targetWeight) {
                        weightLossProgress = (initialWeight - currentWeight) / (initialWeight - targetWeight);
                        weightLossProgress = Math.max(0, Math.min(1, weightLossProgress)); // Clamp between 0-1
                    }
                    break;
                }
            }
        }
        
        // Generate recommendations
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("Current Status:\n");
        recommendations.append(String.format("- Current Weight: %.1f kg\n", latestMeasurement.getWeight()));
        recommendations.append(String.format("- Average Daily Calories: %.0f kcal\n", avgDailyCalories));
        recommendations.append(String.format("- Average Daily Exercise: %.0f kcal\n", avgDailyExercise));
        recommendations.append(String.format("- Estimated TDEE: %.0f kcal\n", tdee));
        recommendations.append(String.format("- Calorie Balance: %+.0f kcal/day\n", calorieBalance));
        recommendations.append(String.format("- Projected Change: %.1f to %.1f kg/week (from calories)\n", range[0], range[1]));
        recommendations.append(String.format("- Confidence Level: %.0f%%\n", confidence * 100));
        recommendations.append(String.format("- Actual Trend: %.1f kg/week (4-week average)\n", actualWeeklyTrend));
        
        // Add detailed nutrition recommendations
        recommendations.append("\nNutrition Analysis:\n");
        recommendations.append(String.format("- Current Protein Intake: %.1f g (%.1f%% of calories)\n", 
            calculateProteinIntake(recentMeals), calculateProteinPercentage(recentMeals)));
        recommendations.append(String.format("- Current Carb Intake: %.1f g (%.1f%% of calories)\n", 
            calculateCarbIntake(recentMeals), calculateCarbPercentage(recentMeals)));
        recommendations.append(String.format("- Current Fat Intake: %.1f g (%.1f%% of calories)\n", 
            calculateFatIntake(recentMeals), calculateFatPercentage(recentMeals)));
        
        // Compare with recommended macros
        if (proteinNeeds > 0) {
            double currentProtein = calculateProteinIntake(recentMeals);
            if (currentProtein < proteinNeeds * 0.9) {
                recommendations.append(String.format("- Consider increasing protein intake to %.1f g/day for better muscle maintenance\n", proteinNeeds));
            }
        }
        
        // Add meal timing recommendations
        recommendations.append("\nMeal Timing Analysis:\n");
        Map<String, Double> mealDistribution = analyzeMealDistribution(recentMeals);
        if (mealDistribution.get("breakfast") < 0.2) {
            recommendations.append("- Consider adding a protein-rich breakfast to support metabolism\n");
        }
        if (mealDistribution.get("dinner") > 0.4) {
            recommendations.append("- Try to distribute calories more evenly throughout the day\n");
        }
        
        // Add exercise-specific recommendations
        recommendations.append("\nExercise Analysis:\n");
        Map<String, Double> exerciseDistribution = analyzeExerciseDistribution(recentWorkouts);
        if (exerciseDistribution.get("strength") < 0.3 && goalType.equals("gain")) {
            recommendations.append("- Increase strength training frequency for better muscle development\n");
        }
        if (exerciseDistribution.get("cardio") < 0.2 && goalType.equals("loss")) {
            recommendations.append("- Add more cardio sessions to support fat loss\n");
        }
        
        // Add progress tracking and milestones
        if (weightLossProgress > 0) {
            recommendations.append("\nProgress Tracking:\n");
            recommendations.append(String.format("- You've completed %.1f%% of your weight loss journey\n", weightLossProgress * 100));
            if (weightLossProgress < 0.25) {
                recommendations.append("- Focus on establishing consistent habits in the early phase\n");
            } else if (weightLossProgress < 0.75) {
                recommendations.append("- You're in the middle phase - stay consistent with your routine\n");
            } else {
                recommendations.append("- You're in the final phase - focus on sustainable habits\n");
            }
        }
        
        // Update recommendations with more detailed projection information
        recommendations.append("\nWeight Change Projection:\n");
        recommendations.append(String.format("- Projected Change: %.1f to %.1f kg/week (from calories)\n", range[0], range[1]));
        recommendations.append(String.format("- Confidence Level: %.0f%%\n", confidence * 100));
        recommendations.append(String.format("- Actual Trend: %.1f kg/week (4-week average)\n", actualWeeklyTrend));
        
        if (Math.abs(projectedWeeklyChange - actualWeeklyTrend) > 0.2) {
            recommendations.append("- Note: There's a significant difference between projected and actual weight change.\n");
            recommendations.append("  This could be due to:\n");
            recommendations.append("  * Water retention\n");
            recommendations.append("  * Muscle gain from exercise\n");
            recommendations.append("  * Measurement timing variations\n");
        }
        
        // Enhanced goal-specific recommendations
        recommendations.append("\nGoal-Specific Recommendations:\n");
        if (!activeGoals.isEmpty()) {
            for (Goal goal : activeGoals) {
                switch (goal.getGoalType().toUpperCase()) {
                    case "WEIGHT_LOSS":
                        recommendations.append(String.format("- Weight Loss Goal: %.1f kg by %s\n", 
                            goal.getTargetValue(), goal.getTargetDate().toLocalDate()));
                        
                        // Calculate time remaining and required rate
                        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDateTime.now(), goal.getTargetDate());
                        double requiredWeeklyRate = (latestMeasurement.getWeight() - goal.getTargetValue()) / 
                            (daysRemaining / 7.0);
                        
                        if (projectedWeeklyChange > -0.5) {
                            recommendations.append("- Consider increasing your calorie deficit slightly\n");
                            recommendations.append(String.format("- Target a %.1f kg/week loss for your timeline\n", 
                                Math.min(-0.5, requiredWeeklyRate)));
                            recommendations.append("- Focus on high-protein meals to preserve muscle\n");
                            recommendations.append("- Include strength training 2-3 times per week\n");
                        } else if (projectedWeeklyChange < -1.0) {
                            recommendations.append("- Current rate is too aggressive\n");
                            recommendations.append("- Consider a more moderate approach for sustainability\n");
                            recommendations.append("- Ensure adequate protein intake to prevent muscle loss\n");
                        }
                        break;
                    case "WEIGHT_GAIN":
                        recommendations.append(String.format("- Weight Gain Goal: %.1f kg by %s\n", 
                            goal.getTargetValue(), goal.getTargetDate().toLocalDate()));
                        
                        double currentSurplus = avgDailyCalories - tdee;
                        long gainDaysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                            LocalDateTime.now(), goal.getTargetDate());
                        double requiredWeeklyGain = (goal.getTargetValue() - latestMeasurement.getWeight()) / 
                            (gainDaysRemaining / 7.0);
                        
                        if (currentSurplus < 200) {
                            recommendations.append("- Current surplus is too low for optimal gains\n");
                            recommendations.append(String.format("- Target a %.1f kg/week gain for your timeline\n", 
                                Math.min(0.5, requiredWeeklyGain)));
                            recommendations.append("- Focus on calorie-dense foods and protein shakes\n");
                            recommendations.append("- Consider pre/post-workout nutrition timing\n");
                        } else if (currentSurplus > 750 && projectedWeeklyChange > 0.7) {
                            recommendations.append("- Current rate might lead to excess fat gain\n");
                            recommendations.append("- Consider a more moderate surplus\n");
                            recommendations.append("- Focus on progressive overload in training\n");
                            recommendations.append("- Monitor body composition changes weekly\n");
                        }
                        break;
                    case "EXERCISE":
                        recommendations.append(String.format("- Exercise Goal: %s\n", goal.getDescription()));
                        if (avgDailyExercise < goal.getTargetValue()) {
                            recommendations.append("- Increase workout frequency or intensity\n");
                            recommendations.append("- Consider adding 1-2 more sessions per week\n");
                            recommendations.append("- Focus on progressive overload\n");
                            recommendations.append("- Ensure proper rest between sessions\n");
                        }
                        break;
                    case "NUTRITION":
                        recommendations.append(String.format("- Nutrition Goal: %s\n", goal.getDescription()));
                        if (Math.abs(avgDailyCalories - goal.getTargetValue()) > 100) {
                            recommendations.append(String.format("- Adjust daily calories to %.0f kcal\n", goal.getTargetValue()));
                            recommendations.append("- Focus on meal timing and distribution\n");
                            recommendations.append("- Consider meal prepping for consistency\n");
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
            if (goalType != null && goalType.equals("WEIGHT_LOSS")) {
                recommendations.append("- Current weight loss rate is faster than recommended\n");
                recommendations.append("- Consider increasing calories by " + Math.abs(Math.round(calorieBalance - 500)) + " kcal for a safer rate\n");
            } else {
                recommendations.append("- Consider increasing calories by " + Math.abs(Math.round(calorieBalance)) + " kcal to maintain weight\n");
            }
        } else if (projectedWeeklyChange < -0.1) {
            recommendations.append("- Moderate weight loss detected\n");
            if (goalType != null && goalType.equals("WEIGHT_LOSS")) {
                recommendations.append("- Current rate is sustainable\n");
            } else {
                recommendations.append("- Consider increasing calories if weight loss is not intended\n");
            }
        } else if (projectedWeeklyChange > 0.5) {
            recommendations.append("- Rapid weight gain detected (>0.5 kg/week)\n");
            if (goalType != null && goalType.equals("WEIGHT_GAIN")) {
                recommendations.append("- Current rate exceeds optimal range for lean gains\n");
                recommendations.append(String.format("- Consider reducing to %.0f-%.0f kcal above maintenance\n", 300.0, 500.0));
            } else {
                recommendations.append("- Consider reducing calories by " + Math.round(calorieBalance) + " kcal to maintain weight\n");
            }
        } else if (projectedWeeklyChange > 0.1) {
            recommendations.append("- Moderate weight gain detected\n");
            if (goalType != null && goalType.equals("WEIGHT_GAIN")) {
                recommendations.append("- Current rate is within optimal range\n");
                recommendations.append("- Focus on progressive overload in training\n");
            } else {
                recommendations.append("- Consider reducing calories if weight gain is not intended\n");
            }
        } else {
            recommendations.append("- Weight appears stable (±0.1 kg/week)\n");
            if (goalType != null) {
                if (goalType.equals("WEIGHT_GAIN")) {
                    recommendations.append("- Consider increasing calories to support your goal\n");
                } else if (goalType.equals("WEIGHT_LOSS")) {
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
        if (goalType != null) {
            if (goalType.equals("WEIGHT_LOSS")) {
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
            } else if (goalType.equals("WEIGHT_GAIN")) {
                double surplus = avgDailyCalories - tdee;
                
                recommendations.append("Nutrition Strategy for Muscle Gain:\n");
                if (projectedWeeklyChange < 0.2) {
                    recommendations.append("- Weight gain rate is below optimal\n");
                    recommendations.append(String.format("- Consider increasing to %.0f-%.0f kcal per day\n", tdee + 300, tdee + 500));
                    recommendations.append("Key focus areas:\n");
                    recommendations.append("1. Increase calorie-dense foods:\n");
                    recommendations.append("   • Nuts, nut butters, olive oil\n");
                    recommendations.append("   • Whole grains, oats, rice\n");
                    recommendations.append("   • Lean proteins, dairy\n");
                } else if (projectedWeeklyChange > 0.7 && surplus > 750) {
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
                recommendations.append(String.format("- Aim for %.0f g protein daily\n", latestMeasurement.getWeight() * 2.2));
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
        if (goalType != null && goalType.equals("WEIGHT_GAIN")) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("recommendations", recommendations.toString());
        result.put("actualWeeklyTrend", actualWeeklyTrend);
        result.put("projection", projection);
        return result;
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
                Collectors.summingDouble(WorkoutLog::getCaloriesBurned)
            ));

        // Calculate average over the number of days with data
        return dailyExercise.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0);
    }

    private Map<String, Object> calculateWeeklyWeightChange(double dailyBalance, double avgDailyExercise, String goalType) {
        final double BASE_ENERGY_DENSITY = 7700; // kcal per kg
        final double MAX_WEEKLY_CHANGE = 2.0; // kg
        
        // Adjust energy density based on exercise level
        double adjustedEnergyDensity = BASE_ENERGY_DENSITY;
        if (avgDailyExercise > 300) {
            adjustedEnergyDensity *= 0.9; // More efficient energy use with regular exercise
        }
        
        // Adjust for goal type
        if (goalType != null) {
            if (goalType.equals("WEIGHT_GAIN")) {
                adjustedEnergyDensity *= 1.1; // More efficient for muscle gain
            } else if (goalType.equals("WEIGHT_LOSS")) {
                adjustedEnergyDensity *= 0.9; // Less efficient for fat loss
            }
        }
        
        // Calculate base weekly change
        double weeklyChange = (dailyBalance * 7) / adjustedEnergyDensity;
        
        // Apply reasonable limits
        weeklyChange = Math.max(-MAX_WEEKLY_CHANGE, Math.min(MAX_WEEKLY_CHANGE, weeklyChange));
        
        // Calculate confidence level
        double confidence = 0.7; // Base confidence
        
        // Adjust confidence based on data quality
        if (avgDailyExercise > 300) {
            confidence *= 0.9; // Less confident with high exercise
        }
        if (Math.abs(dailyBalance) > 500) {
            confidence *= 0.8; // Less confident with large calorie differences
        }
        
        // Calculate range
        double lowerBound = weeklyChange * 0.8;
        double upperBound = weeklyChange * 1.2;
        
        Map<String, Object> result = new HashMap<>();
        result.put("projection", Math.round(weeklyChange * 10) / 10.0);
        result.put("confidence", Math.round(confidence * 100) / 100.0);
        result.put("range", new double[]{Math.round(lowerBound * 10) / 10.0, Math.round(upperBound * 10) / 10.0});
        
        return result;
    }

    private double calculateActualWeightTrend(User user) {
        List<BodyMeasurement> measurements = bodyMeasurementRepository
            .findByUserOrderByDateTimeDesc(user);
            
        if (measurements.size() < 2) return 0;
        
        // Get first and last measurement in last 4 weeks
        BodyMeasurement newest = measurements.get(0);
        BodyMeasurement oldest = measurements.stream()
            .filter(m -> m.getDateTime().isAfter(newest.getDateTime().minusWeeks(4)))
            .reduce((first, last) -> last)
            .orElse(measurements.get(measurements.size()-1));
            
        double weightDiff = newest.getWeight() - oldest.getWeight();
        long daysBetween = java.time.Duration.between(oldest.getDateTime(), newest.getDateTime()).toDays();
        long weeks = daysBetween / 7;
        weeks = weeks == 0 ? 1 : weeks; // prevent division by zero
        
        return weightDiff / weeks;
    }

    private double getInitialWeight(User user) {
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserOrderByDateTimeDesc(user);
        if (measurements.size() > 1) {
            // Get the oldest measurement by taking the last one in the descending list
            return measurements.get(measurements.size()-1).getWeight();
        }
        return measurements.isEmpty() ? 0 : measurements.get(0).getWeight();
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

    private double calculateProteinIntake(List<Meal> meals) {
        return meals.stream()
            .mapToDouble(meal -> meal.getTotalProtein() != null ? meal.getTotalProtein() : 0)
            .sum() / 7.0;
    }

    private double calculateProteinPercentage(List<Meal> meals) {
        double totalCalories = calculateAverageDailyCalories(meals);
        double proteinCalories = calculateProteinIntake(meals) * 4;
        return totalCalories > 0 ? (proteinCalories / totalCalories) * 100 : 0;
    }

    private double calculateCarbIntake(List<Meal> meals) {
        return meals.stream()
            .mapToDouble(meal -> meal.getTotalCarbs() != null ? meal.getTotalCarbs() : 0)
            .sum() / 7.0;
    }

    private double calculateCarbPercentage(List<Meal> meals) {
        double totalCalories = calculateAverageDailyCalories(meals);
        double carbCalories = calculateCarbIntake(meals) * 4;
        return totalCalories > 0 ? (carbCalories / totalCalories) * 100 : 0;
    }

    private double calculateFatIntake(List<Meal> meals) {
        return meals.stream()
            .mapToDouble(meal -> meal.getTotalFat() != null ? meal.getTotalFat() : 0)
            .sum() / 7.0;
    }

    private double calculateFatPercentage(List<Meal> meals) {
        double totalCalories = calculateAverageDailyCalories(meals);
        double fatCalories = calculateFatIntake(meals) * 9;
        return totalCalories > 0 ? (fatCalories / totalCalories) * 100 : 0;
    }

    private Map<String, Double> analyzeMealDistribution(List<Meal> meals) {
        Map<String, Double> distribution = new HashMap<>();
        distribution.put("breakfast", 0.0);
        distribution.put("lunch", 0.0);
        distribution.put("dinner", 0.0);
        distribution.put("snacks", 0.0);

        double totalCalories = calculateAverageDailyCalories(meals);
        if (totalCalories > 0) {
            for (Meal meal : meals) {
                String mealType = determineMealType(meal.getDateTime());
                double current = distribution.get(mealType);
                distribution.put(mealType, current + meal.getTotalCalories());
            }
            
            // Convert to percentages
            for (String key : distribution.keySet()) {
                distribution.put(key, distribution.get(key) / (totalCalories * 7));
            }
        }
        
        return distribution;
    }

    private String determineMealType(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if (hour >= 4 && hour < 11) return "breakfast";
        if (hour >= 11 && hour < 16) return "lunch";
        if (hour >= 16 && hour < 22) return "dinner";
        return "snacks";
    }

    private Map<String, Double> analyzeExerciseDistribution(List<WorkoutLog> workouts) {
        Map<String, Double> distribution = new HashMap<>();
        distribution.put("strength", 0.0);
        distribution.put("cardio", 0.0);
        distribution.put("flexibility", 0.0);

        double totalExercise = calculateAverageDailyExercise(workouts);
        if (totalExercise > 0) {
            for (WorkoutLog workout : workouts) {
                String workoutType = workout.getWorkoutType();
                double calories = workout.getCaloriesBurned();
                
                if (workoutType != null) {
                    if (workoutType.contains("strength") || workoutType.contains("weight")) {
                        distribution.put("strength", distribution.get("strength") + calories);
                    } else if (workoutType.contains("cardio") || workoutType.contains("run") || 
                              workoutType.contains("bike") || workoutType.contains("swim")) {
                        distribution.put("cardio", distribution.get("cardio") + calories);
                    } else {
                        distribution.put("flexibility", distribution.get("flexibility") + calories);
                    }
                } else {
                    // If workoutType is null, categorize based on workout name
                    String workoutName = workout.getWorkoutName();
                    if (workoutName != null) {
                        if (workoutName.contains("strength") || workoutName.contains("weight")) {
                            distribution.put("strength", distribution.get("strength") + calories);
                        } else if (workoutName.contains("cardio") || workoutName.contains("run") || 
                                  workoutName.contains("bike") || workoutName.contains("swim")) {
                            distribution.put("cardio", distribution.get("cardio") + calories);
                        } else {
                            distribution.put("flexibility", distribution.get("flexibility") + calories);
                        }
                    } else {
                        // If both workoutType and workoutName are null, default to flexibility
                        distribution.put("flexibility", distribution.get("flexibility") + calories);
                    }
                }
            }
            
            // Convert to percentages
            for (String key : distribution.keySet()) {
                distribution.put(key, distribution.get(key) / (totalExercise * 7));
            }
        }
        
        return distribution;
    }
} 