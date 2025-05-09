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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);

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
        log.debug("Fetching active goals for user: {}", user.getEmail());
        return getActiveUnarchivedGoals(user);
    }

    public List<Goal> getActiveUnarchivedGoals(User user) {
        return goalRepository.findByUserAndArchivedFalse(user);
    }

    public List<Goal> getAllGoalsForUser(User user) {
        log.debug("Fetching ALL goals for user: {}", user.getEmail());
        return goalRepository.findByUserOrderByStartDateDesc(user);
    }

    public long getActiveCompletedGoalCount(User user) {
        List<Goal> completedGoals = goalRepository.findByUserAndStatusAndArchivedFalse(user, "COMPLETED");
        long count = completedGoals.size();
        log.debug("Found {} active (non-archived) completed goals for user: {}", count, user.getEmail());
        return count;
    }

    public long getTotalCompletedGoalCount(User user) {
        List<Goal> allCompletedGoals = goalRepository.findByUserAndStatus(user, "COMPLETED");
        long count = allCompletedGoals.size();
        log.debug("Found {} total completed goals (including archived) for user: {}", count, user.getEmail());
        return count;
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
        
        // Set initial current value and start value based on goal type
        if (goalType.equals("WEIGHT_LOSS") || goalType.equals("WEIGHT_GAIN")) {
            BodyMeasurement latestMeasurement = bodyMeasurementService.getLatestMeasurement(user);
            double currentWeight = latestMeasurement != null ? latestMeasurement.getWeight() : 0.0;
            goal.setCurrentValue(currentWeight);
            goal.setStartValue(currentWeight);
        } else {
            goal.setCurrentValue(0.0);
            goal.setStartValue(0.0);
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
        if (goal.getGoalType().equalsIgnoreCase("WEIGHT_LOSS")) {
            double startingWeight = goal.getStartValue() != null ? goal.getStartValue() : 0.0;
            double targetWeight = goal.getTargetValue();
            double currentWeight = goal.getCurrentValue();
            if (startingWeight == targetWeight) return 100;
            return Math.max(0, Math.min(100, ((startingWeight - currentWeight) / (startingWeight - targetWeight)) * 100));
        }
        if (goal.getTargetValue() == 0) return 0;
        return (goal.getCurrentValue() / goal.getTargetValue()) * 100;
    }

    @Transactional
    public List<Goal> updateGoalsBasedOnActivity(User user) {
        List<Goal> activeGoals = getActiveGoals(user);
        List<Goal> newlyCompletedGoals = new ArrayList<>();
        if (activeGoals.isEmpty()) return newlyCompletedGoals;

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
            Goal completedGoal = null;
            switch (goal.getGoalType().toUpperCase()) {
                case "WEIGHT_LOSS":
                case "WEIGHT_GAIN":
                    if (latestMeasurement != null) {
                        completedGoal = updateWeightGoal(goal, latestMeasurement);
                    }
                    break;
                case "EXERCISE":
                    completedGoal = updateExerciseGoal(goal, workoutFrequency, avgDailyExercise);
                    break;
                case "NUTRITION":
                    completedGoal = updateNutritionGoal(goal, avgDailyCalories);
                    break;
            }
            if (completedGoal != null) {
                newlyCompletedGoals.add(completedGoal);
            }
        }
        return newlyCompletedGoals;
    }

    private Goal updateWeightGoal(Goal goal, BodyMeasurement latestMeasurement) {
        if (latestMeasurement == null || latestMeasurement.getWeight() == null) return null;
        
        Double currentWeight = latestMeasurement.getWeight();
        boolean justCompleted = false;
        String originalStatus = goal.getStatus();

        goal.setCurrentValue(currentWeight);
        
        // Check goal type directly
        if (goal.getGoalType().equals("WEIGHT_LOSS")) {
            if (currentWeight <= goal.getTargetValue()) {
                if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                    justCompleted = true;
                    goal.setCompletionDate(latestMeasurement.getDateTime());
                }
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getGoalType().equals("WEIGHT_GAIN")) {
            if (currentWeight >= goal.getTargetValue()) {
                 if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                    justCompleted = true;
                    goal.setCompletionDate(latestMeasurement.getDateTime());
                }
                goal.setStatus("COMPLETED");
            }
        }
        
        goalRepository.save(goal);
        return justCompleted ? goal : null;
    }

    private Goal updateExerciseGoal(Goal goal, int workoutFrequency, double avgDailyExercise) {
        boolean justCompleted = false;
        String originalStatus = goal.getStatus();

        // Update based on workout frequency
        goal.setCurrentValue((double) workoutFrequency);
        
        // If goal is about calories burned
        if (goal.getDescription().toLowerCase().contains("calories")) {
            goal.setCurrentValue(avgDailyExercise);
        }
        
        if (goal.getCurrentValue() >= goal.getTargetValue()) {
            if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                justCompleted = true;
            }
            goal.setStatus("COMPLETED");
        }
        goalRepository.save(goal);
        return justCompleted ? goal : null;
    }

    private Goal updateNutritionGoal(Goal goal, double avgDailyCalories) {
        boolean justCompleted = false;
        String originalStatus = goal.getStatus();

        goal.setCurrentValue(avgDailyCalories);
        
        // Check if goal is about maintaining, increasing, or decreasing calories
        if (goal.getDescription().toLowerCase().contains("maintain")) {
            // Allow for 5% variance
            double variance = Math.abs(goal.getTargetValue() - avgDailyCalories) / goal.getTargetValue();
            if (variance <= 0.05) {
                 if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                    justCompleted = true;
                }
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getDescription().toLowerCase().contains("reduce")) {
            if (avgDailyCalories <= goal.getTargetValue()) {
                if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                    justCompleted = true;
                }
                goal.setStatus("COMPLETED");
            }
        } else if (goal.getDescription().toLowerCase().contains("increase")) {
            if (avgDailyCalories >= goal.getTargetValue()) {
                 if (!"COMPLETED".equalsIgnoreCase(originalStatus)) {
                    justCompleted = true;
                }
                goal.setStatus("COMPLETED");
            }
        }
        goalRepository.save(goal);
        return justCompleted ? goal : null;
    }

    @Transactional
    public void archiveGoal(Long goalId, User currentUser) {
        log.info("Attempting to archive goal with id: {} for user: {}", goalId, currentUser.getEmail());
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> {
                 log.warn("Archive failed: Goal not found with id: {}", goalId);
                 return new RuntimeException("Goal not found with id: " + goalId);
             });

        // Security check: Ensure the goal belongs to the current user
        if (!goal.getUser().getId().equals(currentUser.getId())) { // Compare by ID for safety
             log.warn("Archive failed: User {} attempted to archive goal {} owned by user {}", 
                     currentUser.getEmail(), goalId, goal.getUser().getEmail());
             throw new SecurityException("User not authorized to archive this goal.");
        }

        if (goal.isArchived()) {
             log.info("Goal {} already archived for user {}. No action needed.", goalId, currentUser.getEmail());
             return; // Already archived
        }

        goal.setArchived(true);
        goalRepository.save(goal);
        log.info("Successfully archived goal {} for user {}", goalId, currentUser.getEmail());
    }
} 