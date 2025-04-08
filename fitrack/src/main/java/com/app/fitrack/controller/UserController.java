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


@Controller
public class UserController {

    @Autowired
    private UserService userService; 

    @Autowired
    private MealService mealService;

    @Autowired
    private WorkoutService workoutService;

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
                redi.addFlashAttribute("user", user);
                return "redirect:/user/new";
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
    User user = userService.findByEmail(email);
    
    // Get workout data for the last 7 days
    List<Workout> workouts = workoutService.getWorkoutsForLast7Days(email);
    
    // Initialize arrays for chart data
    int[] workoutCounts = new int[7];
    int[] caloriesBurned = new int[7];
    
    // Calculate workout frequency and calories burned for each day
    for (Workout workout : workouts) {
        LocalDate workoutDate = workout.getDateTime().toLocalDate();
        LocalDate today = LocalDate.now();
        int daysAgo = (int) ChronoUnit.DAYS.between(workoutDate, today);
        
        if (daysAgo < 7) {
            int index = 6 - daysAgo; // 0 = today, 6 = 6 days ago
            workoutCounts[index]++;
            caloriesBurned[index] += workout.getBurnedCalories();
        }
    }
    
    model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
    model.addAttribute("workoutData", workoutCounts);
    model.addAttribute("caloriesData", caloriesBurned);
    
    return "analytics";
}

}
    
