package com.app.fitrack.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.MealFoodItem;
import com.app.fitrack.service.MealService;
import com.app.fitrack.service.UserService;

@Controller
public class MealController {

    @Autowired
    private MealService mealService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/addmeal")
    public String addMeal(Model model) {
        model.addAttribute("meal", new Meal()); 
        return "addmeal";
    }
    
    @PostMapping("/meals/add")
    public String addMeal(@ModelAttribute Meal meal) {
        if (meal.getDateTime() == null) {
            meal.setDateTime(LocalDateTime.now());
        }
        
        if (meal.getFoodItems() != null) {
            for (MealFoodItem item : meal.getFoodItems()) {
                item.setMeal(meal);
            }
        }
        
        mealService.saveMeal(meal);
        return "redirect:/user/dashboard";
    }

    @GetMapping("/user/dashboard")
public String dashboard(Model model) {
    String fullName = userService.getCurrentUserFullName();
    List<Meal> meals = mealService.getMealsForCurrentDate();
    int totalCalories = mealService.getTotalCaloriesForCurrentDate();
    model.addAttribute("meals", meals);
    model.addAttribute("totalCalories", totalCalories);
    model.addAttribute("fullName", fullName);
    
    // Add placeholder message if no meals are present
    if (meals.isEmpty()) {
        model.addAttribute("placeholderMessage", "You haven't had any meals today yet. Grab something to eat!");
    }
    
    return "dashboard"; 
}
}
