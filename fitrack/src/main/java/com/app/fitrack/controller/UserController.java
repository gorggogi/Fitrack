package com.app.fitrack.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.model.Workout;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.WorkoutService;
import com.app.fitrack.service.DuplicateEmailException;
import com.app.fitrack.service.MealService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.service.BodyMeasurementService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.model.Goal;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;


@Controller
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService; 

    @Autowired
    private MealService mealService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @Autowired
    private GoalService goalService;

    @GetMapping("/user/success")
    public String showSuccessPage() {
        return "Success";
    }

    @GetMapping("/user/terms")
public String showTermsPage() {
    return "terms";  
}

    @GetMapping("/user/verify")
    public String showVerificationPage() {
        return "Verify";
    }

    @GetMapping("/user/resend-code")
public String resendVerificationPage(@RequestParam(value = "email", required = false) String email, Model model) {
    model.addAttribute("email", email); 
    return "Resend-code";
}

    
    @GetMapping("/user/login")
    public String showLoginPage(Model model) {
        if (model.containsAttribute("message")) {
            model.addAttribute("showProfilePrompt", true);
        }
        return "Login";  
    }

    @PostMapping("/user/resend-verification")
    public String resendVerification(@RequestParam String email, RedirectAttributes redi) {
        String message = userService.resendVerificationCode(email);
        redi.addFlashAttribute("message", message);
        return "redirect:/user/resend-code";
    }

    @GetMapping("/user/new")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "registration-form";
    }

    @PostMapping("/user/save")
    public String saveUserForm(@ModelAttribute("user") User user, @RequestParam String confirmPassword, RedirectAttributes redi) {
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                redi.addFlashAttribute("error", "Passwords do not match.");
            }

            userService.registerUser(user); 
            redi.addFlashAttribute("message", "Verify your account with the code we sent you to continue to Fitrack.");
            return "redirect:/user/verify";
        } catch (DuplicateEmailException e) {
            redi.addFlashAttribute("error", e.getMessage());
            redi.addFlashAttribute("user", user);
            return "redirect:/user/new";
        }
    }

    @PostMapping("/user/verify-code")
    public String verifyUser(@RequestParam String code, RedirectAttributes redi) {
        String result = userService.verifyUser(code); 

        if (result.equals("Email successfully verified!")) { 
            redi.addFlashAttribute("message", "Email verified successfully! Please log in to complete your profile.");
            return "redirect:/user/login";
        }

        redi.addFlashAttribute("error", result); 
        return "redirect:/user/verify";
    }

    @GetMapping("/user/reset-password")
    public String showResetPasswordPage() {
        return "Reset-password";  
    }

    @PostMapping("/user/reset-password")
    public String processResetPassword(@RequestParam String email, RedirectAttributes redi) {
        String message = userService.generatePasswordResetToken(email);
        redi.addFlashAttribute("message", message);
        return "redirect:/user/reset-password";
    }

    @GetMapping("/user/change-password")
    public String showChangePasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "Change-password";  
    }

    @PostMapping("/user/change-password")
    public String processChangePassword(@RequestParam String token, 
                                        @RequestParam String newPassword, 
                                        @RequestParam String confirmPassword, 
                                        RedirectAttributes redi) {
        if (!newPassword.equals(confirmPassword)) {
            redi.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/user/change-password?token=" + token;
        }

        String message = userService.resetPassword(token, newPassword);
        redi.addFlashAttribute("message", message);
        return "redirect:/user/login";
    }

    @GetMapping("/user/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    String email = userDetails.getUsername();
    User user = userService.findByEmail(email);

        // Get user's full name
    String fullName = user.getFirstName() + " " + user.getLastName();
        model.addAttribute("fullName", fullName);
    
        // Get workouts for today
        List<Workout> workouts = workoutService.getWorkoutsForCurrentDate();
        model.addAttribute("workouts", workouts);
        model.addAttribute("workoutPlaceholder", "No workouts scheduled for today. Click + to add one.");

        // Get meals for today
    List<Meal> meals = mealService.getMealsForCurrentDate();
        model.addAttribute("meals", meals);
        model.addAttribute("placeholderMessage", "No meals logged for today. Click + to add one.");
        
        // Get goals
        List<Goal> goals = goalService.getUserGoals(user);
        model.addAttribute("goals", goals);
        model.addAttribute("goalService", goalService);
        
        // Calculate total calories for today
        int totalCalories = meals.stream()
                .mapToInt(Meal::getTotalCalories)
                .sum();
        model.addAttribute("totalCalories", totalCalories);
        
        // Calculate today's workout summary
        int totalWorkoutDuration = workoutService.getTodayTotalWorkoutDuration(user);
        int completedWorkouts = workoutService.getTodayCompletedWorkoutCount(user);
        model.addAttribute("totalWorkoutDuration", totalWorkoutDuration);
        model.addAttribute("completedWorkouts", completedWorkouts);
        
        return "dashboard";
    }

    @GetMapping("/user/profile")
    public String showProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/user/profile/create")
    public String createProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Integer age,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) Double height,
                              @RequestParam(required = false) Double weight,
                              RedirectAttributes redi) {
        String email = userDetails.getUsername();
        userService.createUserProfile(email, age, gender, height, weight);
        redi.addFlashAttribute("successMessage", "Profile created successfully!");
        return "redirect:/user/dashboard";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Integer age,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) Double height,
                              @RequestParam(required = false) Double weight,
                              RedirectAttributes redi) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        
        if (user != null) {
            user.setAge(age);
            user.setGender(gender);
            user.setHeight(height);
            user.setWeight(weight);
            userService.createUserProfile(email, age, gender, height, weight);
            redi.addFlashAttribute("successMessage", "Profile updated successfully!");
        }
        
        return "redirect:/user/dashboard";
    }

    // Analytics endpoint has been moved to AnalyticsController for a more comprehensive implementation

    @GetMapping("/user/measurements")
    public String showMeasurementsForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("currentWeight", user.getWeight());
        return "body-measurements";
    }

    @PostMapping("/user/measurements/save")
    public String saveMeasurements(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam Double weight,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redi,
                                 HttpServletRequest request) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        logger.info("[saveMeasurements] User fetched. Current weight from User object: {}", user.getWeight());
        
        try {
        // Create and save the new measurement record
        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setUser(user);
        measurement.setDateTime(LocalDateTime.now());
        measurement.setWeight(weight);
        measurement.setNotes(notes);
        bodyMeasurementService.saveMeasurement(measurement);

        // Update the main user profile weight
        logger.info("[saveMeasurements] Updating User object weight to: {}", weight);
        user.setWeight(weight);
            User savedUser = userService.saveUser(user);
            logger.info("[saveMeasurements] User saved via userService.saveUser. Weight on returned User object: {}", savedUser.getWeight());
            
            // Update goals based on the new measurement
            goalService.updateGoalsBasedOnActivity(savedUser);
            
            // Check if this is an AJAX request
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return "redirect:/user/analytics";
            }
            
            redi.addFlashAttribute("successMessage", "Measurement saved successfully!");
            return "redirect:/user/measurements";
        } catch (Exception e) {
            logger.error("[saveMeasurements] Error saving measurement", e);
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save measurement: " + e.getMessage());
        }
            redi.addFlashAttribute("errorMessage", "Failed to save measurement: " + e.getMessage());
        return "redirect:/user/measurements";
        }
    }

}
    
