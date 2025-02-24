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



@Controller
public class MealController {

    @Autowired
    private MealService mealService; // Inject MealService

    @GetMapping("/user/addmeal")
    public String addMeal(Model model) {
        model.addAttribute("meal", new Meal()); 
        return "addmeal";
    }
    
    @PostMapping("/meals/add")
    public String addMeal(@ModelAttribute Meal meal) {
        // Set meal dateTime if not set
        if (meal.getDateTime() == null) {
            meal.setDateTime(LocalDateTime.now());
        }
        
        // Set meal for each food item
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
        List<Meal> meals = mealService.getMealsForCurrentDate(); // Use mealService
        int totalCalories = mealService.getTotalCaloriesForCurrentDate(); // Use mealService
        model.addAttribute("meals", meals);
        model.addAttribute("totalCalories", totalCalories); // Add total calories to model
        return "dashboard"; 
    }
}
