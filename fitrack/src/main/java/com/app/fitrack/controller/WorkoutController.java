package com.app.fitrack.controller;

import com.app.fitrack.model.Workout;
import com.app.fitrack.service.WorkoutService;
import com.app.fitrack.service.ExerciseService;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.NutritionixService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private NutritionixService nutritionixService;

    @Autowired
    private GoalService goalService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/addworkout")
    public String addWorkout(Model model) {
        model.addAttribute("workout", new Workout());
        return "addworkout";  
    }

    @PostMapping("/user/saveworkout")
    public String saveWorkout(@RequestParam(value = "repeatDays", required = false) List<String> repeatDays, 
                              @ModelAttribute Workout workout) {
        if (repeatDays == null || repeatDays.isEmpty()) {
            repeatDays = List.of("Daily"); 
        }
        workout.setRepeatDays(repeatDays);
        
        // Ensure caloriesBurned is set
        if (workout.getCaloriesBurned() == null) {
            workout.setCaloriesBurned(0.0);
        }
        
        workoutService.saveWorkout(workout);  
        return "redirect:/user/dashboard";
    }

    @PostMapping("/user/workouts/{id}/mark-done")
    public ResponseEntity<Map<String, Object>> markWorkoutAsDone(@PathVariable Long id) {
        workoutService.logWorkout(id);
        
        // Get the current user and update their goals
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser != null) {
            goalService.updateGoalsBasedOnActivity(currentUser);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Workout logged successfully!");
        response.put("workoutId", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/workouts/estimate-calories")
    @ResponseBody
    public ResponseEntity<?> estimateCalories(@RequestBody Map<String, Object> request) {
        String workoutName = (String) request.get("workoutName");
        Object durationObj = request.get("duration");
        int durationMinutes;

        if (workoutName == null || workoutName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Workout name is required."));
        }
        if (durationObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Duration is required."));
        }

        try {
            durationMinutes = (int) Double.parseDouble(durationObj.toString());
            if (durationMinutes <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Duration must be positive."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid duration format."));
        }

        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated."));
        }

        if (currentUser.getWeight() == null || currentUser.getAge() == null || currentUser.getHeight() == null || currentUser.getGender() == null) {
            System.err.println("User profile data incomplete for calorie estimation. User ID: " + currentUser.getId());
            return ResponseEntity.badRequest().body(Map.of("error", "User profile data (weight, age, height, gender) is incomplete. Please update your profile."));
        }

        String exerciseQuery = durationMinutes + " minutes of " + workoutName;
        
        double calories = nutritionixService.calculateExerciseCalories(
            exerciseQuery, 
            currentUser.getWeight(), 
            durationMinutes, 
            currentUser.getAge(), 
            currentUser.getHeight(),
            currentUser.getGender().toString()
        );
        
        return ResponseEntity.ok(Map.of("calories", (int) Math.round(calories)));
    }
}


