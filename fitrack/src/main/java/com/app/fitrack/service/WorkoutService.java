package com.app.fitrack.service;

import com.app.fitrack.model.User;
import com.app.fitrack.model.Workout;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.repository.WorkoutLogRepository;
import com.app.fitrack.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class WorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired 
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private UserService userService;

    public Workout saveWorkout(Workout workout) {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found.");
        }

        workout.setUser(currentUser);

        if (workout.getDateTime() == null) {
            workout.setDateTime(LocalDateTime.now());
        }

        // Ensure caloriesBurned is set
        if (workout.getCaloriesBurned() == null) {
            workout.setCaloriesBurned(0.0);
        }

        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkoutsForCurrentDate() {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<Workout> allUserWorkouts = workoutRepository.findByUser(currentUser);

        // Filter workouts scheduled for today AND NOT already logged today
        return allUserWorkouts.stream()
            .filter(workout -> workout.getRepeatDays().contains(dayOfWeek) || workout.getRepeatDays().contains("Daily"))
            .filter(workout -> !workoutLogRepository.existsByUserAndWorkoutNameAndDate(
                                    currentUser, workout.getWorkoutName(), today
                                 ))
            .collect(Collectors.toList());
    }

    public void logWorkout(Long workoutId) {
        Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new IllegalArgumentException("Workout not found with ID: " + workoutId));
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found.");
        }

        // Check if already logged today
        if (workoutLogRepository.existsByUserAndWorkoutNameAndDate(currentUser, workout.getWorkoutName(), LocalDate.now())) {
            System.out.println("Workout already logged today: " + workout.getWorkoutName());
            return; 
        }

        WorkoutLog log = new WorkoutLog();
        log.setUser(currentUser);
        log.setWorkoutName(workout.getWorkoutName());
        log.setDuration(workout.getDuration());
        log.setCaloriesBurned(workout.getCaloriesBurned() != null ? workout.getCaloriesBurned() : 0.0);
        log.setWorkoutType("GENERAL");
        log.setCompletedAt(LocalDateTime.now());

        try {
            workoutLogRepository.save(log);
            System.out.println("Workout logged: " + workout.getWorkoutName());
        } catch (Exception e) {
            System.err.println("Error saving workout log: " + e.getMessage());
            throw e;
        }
    }
    
    public List<Workout> getWorkoutsForLast7Days(String email) {
        User user = userService.findByEmail(email);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);
        // Assuming repository method exists - check/create if needed
        return workoutRepository.findByUserAndDateTimeBetween(user, start, end);
    }

    public double getAverageDailyExerciseCalories(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        // Use WorkoutLog for actual completed workouts
        List<WorkoutLog> recentLogs = workoutLogRepository.findByUserAndCompletedAtBetween(user, startDate, endDate);
        
        if (recentLogs.isEmpty()) {
            return 0.0;
        }
        
        // Sort for calculating days spanned
        recentLogs.sort(Comparator.comparing(WorkoutLog::getCompletedAt));
        
        double totalCaloriesBurned = recentLogs.stream()
            .mapToDouble(WorkoutLog::getCaloriesBurned)
            .sum();
        // Calculate the actual number of days spanned by the logs, minimum 1
        long daysSpanned = java.time.temporal.ChronoUnit.DAYS.between(recentLogs.get(0).getCompletedAt().toLocalDate(), endDate.toLocalDate()) + 1;
        
        return totalCaloriesBurned / daysSpanned;
    }

    public List<WorkoutLog> getWorkoutLogsForUser(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return workoutLogRepository.findByUserAndCompletedAtBetween(user, startDate, endDate);
    }
}
