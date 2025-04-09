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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.model.Workout;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.WorkoutService;
import com.app.fitrack.service.DuplicateEmailException;
import com.app.fitrack.service.MealService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.service.BodyMeasurementService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import com.app.fitrack.repository.WorkoutLogRepository;
import com.app.fitrack.model.WorkoutLog;


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
    private WorkoutLogRepository workoutLogRepository;

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
public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    String email = userDetails.getUsername();
    User user = userService.findByEmail(email);

    String fullName = user.getFirstName() + " " + user.getLastName();
    

    List<Meal> meals = mealService.getMealsForCurrentDate();
    List<Workout> workouts = workoutService.getWorkoutsForCurrentDate();

    int totalCalories = mealService.getTotalCaloriesForCurrentDate();
    
   
    model.addAttribute("meals", meals);
    model.addAttribute("workouts", workouts);
    model.addAttribute("totalCalories", totalCalories);
    model.addAttribute("fullName", fullName);
    model.addAttribute("user", user);

    if (meals.isEmpty()) {
        model.addAttribute("placeholderMessage", "You haven't had any meals today yet. Grab something to eat!");
    }

    if (workouts.isEmpty()) {
        model.addAttribute("workoutPlaceholder", "No workouts scheduled today. Stay active!");
    }

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
    String result = userService.createUserProfile(email, age, gender, height, weight);
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

@GetMapping("/user/analytics")
public String showAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    String email = userDetails.getUsername();
    logger.info("[showAnalytics] Request received for user email: {}", email);
    User user = userService.findByEmail(email);
    
    if (user == null) {
        logger.warn("[showAnalytics] User not found for email: {}. Redirecting to login.", email);
        return "redirect:/login";
    }
    logger.info("[showAnalytics] User fetched. Weight from User object: {}", user.getWeight());
    
    // --- Calculate Workout Frequency & Calories from LOGS for the last 7 days ---
    LocalDateTime endDateTime = LocalDateTime.now();
    LocalDateTime startDateTime = endDateTime.minusDays(7);
    List<WorkoutLog> recentWorkoutLogs = workoutLogRepository.findByUserAndCompletedAtBetween(user, startDateTime, endDateTime);
    
    // Initialize arrays for chart data
    int[] workoutCounts = new int[7]; // Index 0 = 6 days ago, ..., Index 6 = today
    int[] caloriesBurned = new int[7];
    LocalDate today = LocalDate.now();

    // Calculate workout frequency and calories burned for each day from logs
    for (WorkoutLog log : recentWorkoutLogs) {
        LocalDate logDate = log.getCompletedAt().toLocalDate();
        long daysAgo = ChronoUnit.DAYS.between(logDate, today);
        
        if (daysAgo >= 0 && daysAgo < 7) {
            int index = 6 - (int)daysAgo; // Map daysAgo (0-6) to index (6-0)
            workoutCounts[index]++;
            // burnedCalories is a primitive double, cannot be null. Direct addition is fine.
            caloriesBurned[index] += log.getBurnedCalories(); 
        }
    }
    // --- End Workout Log Processing ---

    // Get body measurements (descending order initially)
    List<BodyMeasurement> measurementsDesc = bodyMeasurementService.getMeasurementsForUser(user);
    BodyMeasurement latestMeasurement = measurementsDesc.isEmpty() ? null : measurementsDesc.get(0);

    // Create a copy and reverse for chronological charting
    List<BodyMeasurement> measurementsChronological = new ArrayList<>(measurementsDesc);
    java.util.Collections.reverse(measurementsChronological);

    // Calculate progress (uses descending list internally)
    double weightProgress = bodyMeasurementService.calculateProgress(user, "weight");
    double bmiProgress = bodyMeasurementService.calculateProgress(user, "bmi");

    // --- Start Projection Calculation ---
    List<Map<String, Object>> projectionData = new ArrayList<>();
    final int PROJECTION_DAYS = 14; // Project for 2 weeks
    final double KCAL_PER_KG = 7700.0; // Approx kcal deficit/surplus for 1kg change
    final double DEFAULT_ACTIVITY_FACTOR = 1.725; // Very active
    final int AVG_PERIOD_DAYS = 7; // Use last 7 days for averages

    if (latestMeasurement != null) { // Need a starting point for projection
        double tdee = userService.calculateTDEE(user, DEFAULT_ACTIVITY_FACTOR);
        double avgIntake = mealService.getAverageDailyCalories(user, AVG_PERIOD_DAYS);
        double avgExerciseBurn = workoutService.getAverageDailyExerciseCalories(user, AVG_PERIOD_DAYS);
        
        logger.info("[showAnalytics] TDEE: {}, Avg Intake: {}, Avg Exercise Burn: {}", tdee, avgIntake, avgExerciseBurn);

        if (tdee > 0) { // Avoid division by zero or projecting if TDEE is unknown
            double dailyBalance = avgIntake - (tdee + avgExerciseBurn);
            double dailyWeightChangeKg = dailyBalance / KCAL_PER_KG;
            
            logger.info("[showAnalytics] Daily Balance: {} kcal, Daily Weight Change: {} kg", dailyBalance, dailyWeightChangeKg);

            double currentWeight = latestMeasurement.getWeight();
            LocalDateTime currentDate = latestMeasurement.getDateTime();

            // Add the latest actual point to projection series start
            Map<String, Object> startPoint = new HashMap<>();
            startPoint.put("date", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            startPoint.put("weight", currentWeight);
            projectionData.add(startPoint);

            for (int i = 1; i <= PROJECTION_DAYS; i++) {
                currentDate = currentDate.plusDays(1);
                currentWeight += dailyWeightChangeKg;
                
                Map<String, Object> projectedPoint = new HashMap<>();
                projectedPoint.put("date", currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                 // Format weight to 1 decimal place for consistency
                projectedPoint.put("weight", Math.round(currentWeight * 10.0) / 10.0); 
                projectionData.add(projectedPoint);
            }
             logger.info("[showAnalytics] Generated {} projection points.", projectionData.size());
        } else {
            logger.warn("[showAnalytics] Cannot calculate projection because TDEE is zero or invalid.");
        }
    } else {
        logger.warn("[showAnalytics] Cannot calculate projection because there are no measurements.");
    }
    // --- End Projection Calculation ---

    // --- Prepare data specifically for the JavaScript Chart --- 
    List<Map<String, Object>> chartMeasurementData = measurementsChronological.stream()
        .map(m -> {
            Map<String, Object> point = new HashMap<>();
            point.put("dateTime", m.getDateTime().toString()); // Pass as ISO String
            point.put("weight", m.getWeight());
            // Include user height directly in this map for JS BMI calculation
            point.put("userHeight", (m.getUser() != null) ? m.getUser().getHeight() : null); 
            return point;
        })
        .collect(java.util.stream.Collectors.toList());
    
    // Note: projectionData is already a List<Map<String, Object>> with simple types

    // --- Prepare data specifically for the Timeline Display --- 
    DateTimeFormatter timelineDateFormatter = DateTimeFormatter.ofPattern("MMM dd");
    List<Map<String, Object>> timelineData = measurementsDesc.stream() // Use descending list for timeline (newest first)
        .map(m -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", m.getDateTime().format(timelineDateFormatter));
            entry.put("weight", m.getWeight());
            entry.put("notes", m.getNotes());
            return entry;
        })
        .collect(java.util.stream.Collectors.toList());

    // --- End Data Prep ---

    // --- Add attributes to the model --- 
    // Data for non-script parts (only primitives or safe objects)
    model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
    model.addAttribute("currentUserWeight", user.getWeight()); // Pass user's weight explicitly
    model.addAttribute("currentUserHeight", user.getHeight()); // Pass user's height explicitly
    model.addAttribute("latestMeasurementWeight", (latestMeasurement != null) ? latestMeasurement.getWeight() : null);
    model.addAttribute("latestMeasurementDateTime", (latestMeasurement != null) ? latestMeasurement.getDateTime() : null);
    model.addAttribute("weightProgress", weightProgress);
    model.addAttribute("bmiProgress", bmiProgress);
    // Pass calculated BMI and category instead of the service/full user
    double currentBmiValue = bodyMeasurementService.calculateCurrentBMI(user);
    model.addAttribute("currentBmiValue", currentBmiValue);
    model.addAttribute("currentBmiCategory", bodyMeasurementService.getBMICategory(currentBmiValue));

    // Data for JavaScript inlining (already simplified)
    model.addAttribute("workoutData", workoutCounts); 
    model.addAttribute("caloriesData", caloriesBurned); 
    model.addAttribute("chartMeasurementData", chartMeasurementData); 
    model.addAttribute("projectionData", projectionData); // Pass projection data regardless
    model.addAttribute("showProjectionDefault", false); // Control default visibility

    // Data for Timeline Display (simplified)
    model.addAttribute("timelineData", timelineData);

    // --- Remove complex objects from model that aren't strictly needed by template display --- 
    // model.addAttribute("user", user); // REMOVED - Pass primitives instead
    // model.addAttribute("latestMeasurement", latestMeasurement); // REMOVED - Pass primitives instead
    // model.addAttribute("measurements", measurementsChronological); // Already removed
    // model.addAttribute("bodyMeasurementService", bodyMeasurementService); // Already removed
    
    logger.info("[showAnalytics] Passing data to template. chartMeasurementData size: {}, projectionData size: {}, timelineData size: {}", 
                chartMeasurementData.size(), projectionData.size(), timelineData.size());
                
    return "analytics";
}

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
                             RedirectAttributes redi) {
    String email = userDetails.getUsername();
    User user = userService.findByEmail(email);
    logger.info("[saveMeasurements] User fetched. Current weight from User object: {}", user.getWeight());
    
    // Create and save the new measurement record
    BodyMeasurement measurement = new BodyMeasurement();
    measurement.setUser(user);
    measurement.setDateTime(LocalDateTime.now());
    measurement.setWeight(weight);
    measurement.setNotes(notes);
    bodyMeasurementService.saveMeasurement(measurement);

    // Also update the main user profile weight
    logger.info("[saveMeasurements] Updating User object weight to: {}", weight);
    user.setWeight(weight);
    try {
        User savedUser = userService.saveUser(user); // Assuming a method like this exists in UserService
        logger.info("[saveMeasurements] User saved via userService.saveUser. Weight on returned User object: {}", savedUser.getWeight());
    } catch (Exception e) {
        logger.error("[saveMeasurements] Error saving user via userService.saveUser", e);
        // Decide how to handle error - maybe add error message to redi?
    }
    
    // Optional: Re-fetch user to confirm DB state immediately after save (for debugging)
    // User userAfterSave = userService.findByEmail(email);
    // logger.info("[saveMeasurements] User re-fetched after save. Weight: {}", userAfterSave.getWeight());

    redi.addFlashAttribute("successMessage", "Measurement saved successfully!");
    logger.info("[saveMeasurements] Redirecting after saving measurement.");
    return "redirect:/user/analytics"; // Redirect directly to analytics
}

}
    
