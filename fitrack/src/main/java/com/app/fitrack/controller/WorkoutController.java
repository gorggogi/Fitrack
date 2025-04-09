package com.app.fitrack.controller;

import com.app.fitrack.model.Workout;
import com.app.fitrack.service.WorkoutService;
import com.app.fitrack.service.ExerciseService;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private ExerciseService exerciseService;

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
    public ResponseEntity<Map<String, Integer>> estimateCalories(@RequestBody Map<String, Object> request) {
        String workoutName = (String) request.get("workoutName");
        double duration = Double.parseDouble(request.get("duration").toString());
        
        int calories = exerciseService.getBurnedCalories(workoutName, duration);
        
        return ResponseEntity.ok(Map.of("calories", calories));
    }
}


