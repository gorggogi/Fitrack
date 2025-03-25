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
import java.util.List;
import java.util.Locale;

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

        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkoutsForCurrentDate() {
    User currentUser = userService.getAuthenticatedUser();
    if (currentUser == null) {
        throw new IllegalStateException("No authenticated user found.");
    }


    String today = LocalDateTime.now().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

    return workoutRepository.findByUser(currentUser)
            .stream()
            .filter(workout -> 
                (workout.getRepeatDays().contains("Daily") || workout.getRepeatDays().contains(today)) &&
                !workoutLogRepository.existsByUserAndWorkoutNameAndDate(
                    currentUser, workout.getWorkoutName(), LocalDate.now() 
                )
            )
            .toList();
}

    

public void logWorkout(Long workoutId) {
    User currentUser = userService.getAuthenticatedUser();
    if (currentUser == null) {
        throw new IllegalStateException("No authenticated user found.");
    }

    Workout workout = workoutRepository.findById(workoutId)
            .orElseThrow(() -> new IllegalArgumentException("Workout not found"));

    WorkoutLog log = new WorkoutLog(
            currentUser,
            workout.getWorkoutName(),
            workout.getDuration(),
            workout.getBurnedCalories()
    );

    workoutLogRepository.save(log);
}

}
