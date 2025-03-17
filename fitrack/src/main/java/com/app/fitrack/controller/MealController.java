package com.app.fitrack.controller;
import java.time.LocalDateTime;
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
    private MealService mealService;

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

   
}
