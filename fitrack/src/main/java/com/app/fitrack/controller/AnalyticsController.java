package com.app.fitrack.controller;

import com.app.fitrack.dto.AnalyticsPageDTO;
import com.app.fitrack.model.User;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.model.Meal;
import com.app.fitrack.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

// DTOs for /api/meals/last-7-days - kept here as they are specific to this controller's API endpoint
// Could be moved to a general dto package if used elsewhere
class MealDTO { // Note: Made package-private or ensure it's accessible if moved
    private Long id;
    private LocalDateTime dateTime;
    private String mealName;
    private List<FoodItemDTO> foodItems;

    public MealDTO(Long id, LocalDateTime dateTime, String mealName, List<FoodItemDTO> foodItems) {
        this.id = id;
        this.dateTime = dateTime;
        this.mealName = mealName;
        this.foodItems = foodItems;
    }
    // Getters
    public Long getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getMealName() { return mealName; }
    public List<FoodItemDTO> getFoodItems() { return foodItems; }
}

class FoodItemDTO { // Note: Made package-private
    private String foodItem;
    private Double protein;
    private Double carbs;
    private Double fat;

    public FoodItemDTO(String foodItem, Double protein, Double carbs, Double fat) {
        this.foodItem = foodItem;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
    // Getters
    public String getFoodItem() { return foodItem; }
    public Double getProtein() { return protein; }
    public Double getCarbs() { return carbs; }
    public Double getFat() { return fat; }
}

@Controller
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class); // Ensure logger is defined

    @Autowired
    private UserService userService;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private MealService mealService;

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/user/analytics")
    public String showAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        log.debug("Analytics page requested for user: {}", email);
        User user = userService.findByEmail(email);

        if (user == null) {
            log.warn("User not found for email: {}. Redirecting.", email);
            return "redirect:/user/login?error=UserNotFoundForAnalytics";
        }

        AnalyticsPageDTO analyticsData = analyticsService.getAnalyticsPageData(user);
        
        // Check if DTO is null and log fullName just before adding to model
        if (analyticsData != null) { 
            log.debug("AnalyticsData DTO retrieved. FullName from DTO: {}", analyticsData.getFullName());
            // The explicit setFullName here is redundant if the service does it, but keep for now if needed.
            // analyticsData.setFullName(user.getFirstName() + " " + user.getLastName());
        } else {
            log.warn("AnalyticsService returned null DTO for user: {}", email);
            // Optionally create an empty DTO to avoid null pointer in template, 
            // or handle this case differently depending on requirements.
            analyticsData = new AnalyticsPageDTO(); 
        }
        
        model.addAttribute("analyticsData", analyticsData);
        log.debug("Added analyticsData to model for view.");
        
        return "analytics";
    }

    @GetMapping("/api/meals/last-7-days")
    @ResponseBody
    public List<MealDTO> getLast7DaysMeals(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        List<Meal> meals = mealService.findMealsByUserAndDateRange(user, startDate, endDate);
        return meals.stream()
            .map(meal -> new MealDTO(
                meal.getId(),
                meal.getDateTime(),
                meal.getMealName(),
                meal.getFoodItems().stream()
                    .map(food -> new FoodItemDTO(
                        food.getFoodItem(),
                        food.getProtein(),
                        food.getCarbs(),
                        food.getFat()
                    ))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }
} 