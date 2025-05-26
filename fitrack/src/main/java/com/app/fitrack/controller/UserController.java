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
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.service.BodyMeasurementService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.model.Goal;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.app.fitrack.service.CloudinaryService;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import org.springframework.validation.BindingResult;
import java.time.LocalDate;
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
    private GoalService goalService;

    @Autowired
    private Validator validator;

    @Autowired
    private CloudinaryService cloudinaryService;

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
        return "Registration-form";
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
        model.addAttribute("user", user);  // Add the entire user object to the model
    
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

    @GetMapping("/user/profile/edit")
    public String showEditProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        if (user == null) {
            // Handle case where user is not found, perhaps redirect to login
            return "redirect:/user/login?error=User not found";
        }
        model.addAttribute("user", user);
        return "edit-profile"; // Name of the Thymeleaf template for editing
    }

    @PostMapping("/user/profile/create")
    public String createProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) Integer age,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) Double height,
                              @RequestParam(required = false) Double weight,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              RedirectAttributes redi) {
        String email = userDetails.getUsername();
        String profilePictureUrl = null;

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                profilePictureUrl = cloudinaryService.uploadProfilePicture(profilePicture);
            } catch (IOException e) {
                logger.error("Failed to upload profile picture to Cloudinary", e);
                redi.addFlashAttribute("error", "Failed to upload profile picture. Please try again.");
                return "redirect:/user/profile";
            }
        } else {
            // Optionally set a default URL or leave as null if no picture is uploaded
            // profilePictureUrl = "/images/default-profile.png"; // Example if you want a default stored
        }

        userService.createUserProfile(email, age, gender, height, weight, profilePictureUrl);
        return "redirect:/user/dashboard";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @ModelAttribute("user") User updatedUserData,
                              BindingResult bindingResult,
                              @RequestParam(required = false) MultipartFile profilePicture,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        String currentEmail = userDetails.getUsername();
        User currentUser = userService.findByEmail(currentEmail);

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Could not find current user session.");
            return "redirect:/user/login";
        }

        // Manual validation logic (as added previously)
        Set<ConstraintViolation<User>> violations = validator.validate(updatedUserData); 
        if (!violations.isEmpty()) {
            logger.warn("Validation errors updating profile for user: {}", currentEmail);
            violations.forEach(violation -> 
                bindingResult.rejectValue(violation.getPropertyPath().toString(), "", violation.getMessage()));
            model.addAttribute("user", updatedUserData); 
            return "edit-profile"; 
        }

        // Email change logic (keep as is)
        if (!currentUser.getEmail().equalsIgnoreCase(updatedUserData.getEmail())) {
             logger.info("Email change detected from {} to {}. Verification process should be used.", currentUser.getEmail(), updatedUserData.getEmail());
             if (currentUser.getPendingEmail() == null || !currentUser.getPendingEmail().equalsIgnoreCase(updatedUserData.getEmail())) {
                 updatedUserData.setEmail(currentUser.getEmail()); 
             }
        }

        String newProfilePictureUrl = currentUser.getProfilePicture(); // Initialize with existing URL

        if (profilePicture != null && !profilePicture.isEmpty()) {
            logger.info("Attempting to upload new profile picture to Cloudinary for user {}", currentEmail);
            try {
                // Optional: Delete old picture from Cloudinary if you implement that
                // if (currentUser.getProfilePicture() != null && currentUser.getProfilePicture().startsWith("https://res.cloudinary.com")) {
                //    String publicId = extractPublicIdFromUrl(currentUser.getProfilePicture()); // You'd need a helper for this
                //    cloudinaryService.deleteImage(publicId); 
                // }
                newProfilePictureUrl = cloudinaryService.uploadProfilePicture(profilePicture);
                logger.info("New profile picture uploaded to Cloudinary: {}", newProfilePictureUrl);
            } catch (IOException e) {
                logger.error("IOException uploading profile picture to Cloudinary for user: {}. Update will proceed with the existing picture URL.", currentEmail, e);
                redirectAttributes.addFlashAttribute("errorMessage", "Error saving new profile picture. Other details updated if any.");
                // newProfilePictureUrl remains the currentUser.getProfilePicture() if upload fails
            }
        } else {
            logger.info("No new profile picture file provided for user {}. Retaining existing: {}", currentEmail, newProfilePictureUrl);
        }

        // Pass the newProfilePictureUrl (which is either the new Cloudinary URL or the existing one)
        userService.updateUserProfile(currentEmail, updatedUserData, newProfilePictureUrl);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/user/dashboard";
    }

    // Endpoint to send verification code for email change
    @PostMapping("/user/profile/send-verification")
    @ResponseBody
    public ResponseEntity<?> sendEmailChangeVerification(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestParam String newEmail) {
        String currentEmail = userDetails.getUsername();
        User user = userService.findByEmail(currentEmail);
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("success", false);
            response.put("message", "User session error.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        if (currentEmail.equalsIgnoreCase(newEmail)) {
             response.put("success", false);
            response.put("message", "Email address has not changed.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            userService.requestEmailChangeVerification(user, newEmail);
            response.put("success", true);
            response.put("message", "Verification code sent to " + newEmail);
            return ResponseEntity.ok(response);
        } catch (DuplicateEmailException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error sending email change verification for user {}: {}", currentEmail, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to send verification code. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint to verify the code and complete the email change
    @PostMapping("/user/profile/verify-email-change")
    @ResponseBody
    public ResponseEntity<?> verifyEmailChange(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam String code) {
        String currentEmail = userDetails.getUsername();
        User user = userService.findByEmail(currentEmail);
         Map<String, Object> response = new HashMap<>();

        if (user == null) {
             response.put("success", false);
            response.put("message", "User session error.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            boolean success = userService.verifyEmailChange(user, code);
            if (success) {
                response.put("success", true);
                response.put("message", "Email successfully verified and updated!");
                response.put("newEmail", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired verification code.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
             logger.error("Error verifying email change code for user {}: {}", user.getEmail(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to verify code. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    @ResponseBody
    public ResponseEntity<?> saveMeasurements(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam Double weight,
                                 @RequestParam(name = "measurementDateTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redi,
                                 HttpServletRequest request) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        logger.info("[saveMeasurements] User fetched. Current weight from User object: {}", user.getWeight());
        
        Map<String, Object> responseBody = new HashMap<>();
        String completedGoalName = null;

        try {
            // Create and save the new measurement record
            BodyMeasurement measurement = new BodyMeasurement();
            measurement.setUser(user);
            if (dateTime != null) {
                measurement.setDateTime(dateTime);
            } else {
                measurement.setDateTime(LocalDateTime.now());
            }
            measurement.setWeight(weight);
            measurement.setNotes(notes);
            bodyMeasurementService.saveMeasurement(measurement);

            // Update the main user profile weight
            logger.info("[saveMeasurements] Updating User object weight to: {}", weight);
            user.setWeight(weight);
            User savedUser = userService.saveUser(user);
            logger.info("[saveMeasurements] User saved via userService.saveUser. Weight on returned User object: {}", savedUser.getWeight());
            
            // Update goals based on the new measurement
            List<Goal> newlyCompletedGoals = goalService.updateGoalsBasedOnActivity(savedUser);
            if (newlyCompletedGoals != null && !newlyCompletedGoals.isEmpty()) {
                // Storing the first completed goal's name for the response
                completedGoalName = newlyCompletedGoals.get(0).getDescription();
            }

            boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

            if (isAjaxRequest) {
                responseBody.put("success", true);
                responseBody.put("message", "Measurement saved successfully!");
                if (completedGoalName != null) {
                    responseBody.put("completedGoalName", completedGoalName);
                }
                responseBody.put("redirectTo", "/user/analytics"); 
                return ResponseEntity.ok(responseBody);
            } else {
                redi.addFlashAttribute("successMessage", "Measurement saved successfully!");
                if (completedGoalName != null) {
                    redi.addFlashAttribute("completedGoalName", completedGoalName);
                }
                return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/user/measurements").build();
            }
            
        } catch (Exception e) {
            logger.error("[saveMeasurements] Error saving measurement", e);
            boolean isAjaxRequest = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
            if (isAjaxRequest) {
                responseBody.put("success", false);
                responseBody.put("message", "Failed to save measurement: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
            }
            redi.addFlashAttribute("errorMessage", "Failed to save measurement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", "/user/measurements").build();
        }
    }

    @GetMapping("/user/scheduledworkouts")
    public String showScheduledWorkouts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);

        // Get user's full name
        String fullName = user.getFirstName() + " " + user.getLastName();
        model.addAttribute("fullName", fullName);
        model.addAttribute("user", user); // Add user to model for profile picture

        // Get all scheduled workouts
        List<Workout> allWorkouts = workoutService.getAllUserWorkouts(user);
        
        // Separate workouts into daily and weekly categories
        List<Workout> dailyWorkouts = allWorkouts.stream()
            .filter(workout -> workout.getRepeatDays().contains("Daily"))
            .collect(Collectors.toList());
            
        List<Workout> weeklyWorkouts = allWorkouts.stream()
            .filter(workout -> !workout.getRepeatDays().contains("Daily"))
            .collect(Collectors.toList());

        model.addAttribute("dailyWorkouts", dailyWorkouts);
        model.addAttribute("weeklyWorkouts", weeklyWorkouts);

        return "scheduledworkouts";
    }

    @PostMapping("/user/workouts/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        try {
            workoutService.deleteWorkout(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting workout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Workout not found");
        } catch (IllegalStateException e) {
            logger.error("Unauthorized workout deletion attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Unauthorized to delete this workout");
        } catch (Exception e) {
            logger.error("Error deleting workout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting workout");
        }
    }

    @GetMapping("/user/workouts/{id}")
    @ResponseBody
    public ResponseEntity<?> getWorkout(@PathVariable Long id) {
        try {
            Workout workout = workoutService.getWorkoutById(id);
            if (workout == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(workout);
        } catch (Exception e) {
            logger.error("Error fetching workout details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching workout details");
        }
    }

    @PostMapping("/user/workouts/{id}/update")
    public String updateWorkout(@PathVariable Long id, 
                              @RequestParam(value = "repeatDays", required = false) List<String> repeatDays,
                              @ModelAttribute Workout workout) {
        try {
            if (repeatDays == null || repeatDays.isEmpty()) {
                repeatDays = List.of("Daily");
            }
            workout.setRepeatDays(repeatDays);
            workoutService.updateWorkout(id, workout);
            return "redirect:/user/scheduledworkouts";
        } catch (Exception e) {
            logger.error("Error updating workout", e);
            return "redirect:/user/scheduledworkouts?error=" + e.getMessage();
        }
    }

    @GetMapping("/user/workout-log")
    public String showWorkoutLogPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null) {
            return "redirect:/user/login?error=UserNotFound";
        }

        List<WorkoutLog> logs = workoutService.getAllWorkoutLogsSorted(user);

        // Group logs by date
        Map<LocalDate, List<WorkoutLog>> groupedLogs = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getCompletedAt().toLocalDate(),
                LinkedHashMap::new, // Preserve insertion order (effectively date order)
                Collectors.toList()
            ));

        model.addAttribute("user", user);
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("groupedLogs", groupedLogs);
        // model.addAttribute("pageTitle", "Workout Log"); // For consistency if you use a common layout

        return "workout-log"; // Name of the new Thymeleaf template
    }
}
    
