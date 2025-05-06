package com.app.fitrack.controller;

import com.app.fitrack.dto.AnalyticsPageDTO;
import com.app.fitrack.model.User;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.model.Meal;
import com.app.fitrack.service.*;
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
        User user = userService.findByEmail(email);

        if (user == null) {
            // Handle user not found case, perhaps redirect to an error page or login
            return "redirect:/user/login?error=UserNotFoundForAnalytics";
        }

        AnalyticsPageDTO analyticsData = analyticsService.getAnalyticsPageData(user);
        model.addAttribute("analyticsData", analyticsData);
        
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