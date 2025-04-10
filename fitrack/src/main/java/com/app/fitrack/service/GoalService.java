package com.app.fitrack.service;

import com.app.fitrack.model.Goal;
import com.app.fitrack.model.User;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.repository.GoalRepository;
import com.app.fitrack.repository.WorkoutLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private MealService mealService;

    public List<Goal> getUserGoals(User user) {
        return goalRepository.findByUserOrderByTargetDateDesc(user);
    }

    public List<Goal> getActiveGoals(User user) {
        return goalRepository.findByUserAndStatus(user, "IN_PROGRESS");
    }

    @Transactional
    public Goal createGoal(User user, String goalType, String description, 
                         Double targetValue, LocalDateTime targetDate) {
        Goal goal = new Goal();
        goal.setUser(user);
        goal.setGoalType(goalType);
        goal.setDescription(description);
        goal.setTargetValue(targetValue);
        
        // Set initial current value based on goal type
        if (goalType.equals("WEIGHT_LOSS") || goalType.equals("WEIGHT_GAIN")) {
            BodyMeasurement latestMeasurement = bodyMeasurementService.getLatestMeasurement(user);
            goal.setCurrentValue(latestMeasurement != null ? latestMeasurement.getWeight() : 0.0);
        } else {
            goal.setCurrentValue(0.0);
        }
        
        goal.setStartDate(LocalDateTime.now());
        goal.setTargetDate(targetDate);
        goal.setStatus("IN_PROGRESS");
        
        return goalRepository.save(goal);
    }

    @Transactional
    public Goal updateGoalProgress(Long goalId, Double newCurrentValue) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
        
        goal.setCurrentValue(newCurrentValue);
        
        // Update status based on progress
        if (newCurrentValue >= goal.getTargetValue()) {
            goal.setStatus("COMPLETED");
        } else if (LocalDateTime.now().isAfter(goal.getTargetDate())) {
            goal.setStatus("FAILED");
        }
        
        return goalRepository.save(goal);
    }

    @Transactional
    public void deleteGoal(Long goalId) {
        goalRepository.deleteById(goalId);
    }

    public double calculateProgress(Goal goal) {
        if (goal.getTargetValue() == 0) return 0;
        return (goal.getCurrentValue() / goal.getTargetValue()) * 100;
    }

    @Transactional
    public void updateGoalsBasedOnActivity(User user) {
        List<Goal> activeGoals = getActiveGoals(user);
        if (activeGoals.isEmpty()) return;

        // Get latest measurements and activity data
        BodyMeasurement latestMeasurement = bodyMeasurementService.getLatestMeasurement(user);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        // Get workout data
        double avgDailyExercise = workoutService.getAverageDailyExerciseCalories(user, 7);
        List<WorkoutLog> recentWorkouts = workoutLogRepository.findByUserAndCompletedAtBetween(user, startDate, endDate);
        int workoutFrequency = recentWorkouts.size();
        
        // Get nutrition data
        double avgDailyCalories = mealService.getAverageDailyCalories(user, 7);

        // Update each active goal
        for (Goal goal : activeGoals) {
            switch (goal.getGoalType().toUpperCase()) {
                case "WEIGHT_LOSS":
                case "WEIGHT_GAIN":
                    if (latestMeasurement != null) {
                        updateWeightGoal(goal, latestMeasurement.getWeight());
                    }
                    break;
                case "EXERCISE":
                    updateExerciseGoal(goal, workoutFrequency, avgDailyExercise);
                    break;
                case "NUTRITION":
                    updateNutritionGoal(goal, avgDailyCalories);
                    break;
            }
        }
    }

    private void updateWeightGoal(Goal goal, Double currentWeight) {
        if (currentWeight == null) return;
        
        goal.setCurrentValue(currentWeight);
        
        // Check goal type directly
        if (goal.getGoalType().equals("WEIGHT_LOSS")) {
            if (currentWeight <= goal.getTargetValue()) {
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getGoalType().equals("WEIGHT_GAIN")) {
            if (currentWeight >= goal.getTargetValue()) {
                goal.setStatus("COMPLETED");
            }
        }
        
        goalRepository.save(goal);
    }

    private void updateExerciseGoal(Goal goal, int workoutFrequency, double avgDailyExercise) {
        // Update based on workout frequency
        goal.setCurrentValue((double) workoutFrequency);
        
        // If goal is about calories burned
        if (goal.getDescription().toLowerCase().contains("calories")) {
            goal.setCurrentValue(avgDailyExercise);
        }
        
        if (goal.getCurrentValue() >= goal.getTargetValue()) {
            goal.setStatus("COMPLETED");
        }
        goalRepository.save(goal);
    }

    private void updateNutritionGoal(Goal goal, double avgDailyCalories) {
        goal.setCurrentValue(avgDailyCalories);
        
        // Check if goal is about maintaining, increasing, or decreasing calories
        if (goal.getDescription().toLowerCase().contains("maintain")) {
            // Allow for 5% variance
            double variance = Math.abs(goal.getTargetValue() - avgDailyCalories) / goal.getTargetValue();
            if (variance <= 0.05) {
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getDescription().toLowerCase().contains("reduce")) {
            if (avgDailyCalories <= goal.getTargetValue()) {
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getDescription().toLowerCase().contains("increase")) {
            if (avgDailyCalories >= goal.getTargetValue()) {
                goal.setStatus("COMPLETED");
            }
        }
        goalRepository.save(goal);
    }
} 