package com.app.fitrack.controller;

import com.app.fitrack.model.Workout;
import com.app.fitrack.service.WorkoutService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;

    @GetMapping("/user/addworkout")
    public String addWorkout(Model model) {
        model.addAttribute("workout", new Workout());
        return "addworkout";  
    }

    @PostMapping("/user/saveworkout")
public String saveWorkout(@RequestParam(value = "repeatDays", required = false) List<String> repeatDays, 
                          @ModelAttribute Workout workout) {
    if (repeatDays == null || repeatDays.isEmpty()) {
        repeatDays = List.of("Daily"); // Default to daily if none selected
    }
    workout.setRepeatDays(repeatDays);
    workoutService.saveWorkout(workout);  
    return "redirect:/user/dashboard";
}

@PostMapping("/user/workouts/{id}/mark-done")
public ResponseEntity<String> markWorkoutAsDone(@PathVariable Long id) {
    workoutService.logWorkout(id);
    return ResponseEntity.ok("Workout logged successfully!");
}


    
}


