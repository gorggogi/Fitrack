package com.app.fitrack.controller;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.MealFoodItem;
import com.app.fitrack.service.MealService;
import com.app.fitrack.service.NutritionixService;

@Controller
public class MealController {

    @Autowired
    private MealService mealService;

    @Autowired
    private NutritionixService nutritionixService; 

    @GetMapping("/user/addmeal")
    public String addMeal(Model model) {
        model.addAttribute("meal", new Meal()); 
        return "addmeal";
    }

    @PostMapping("/meals/estimate-calories")
    public ResponseEntity<List<Double>> estimateCalories(@RequestBody List<Map<String, String>> foodItems) {
        List<Double> estimatedCalories = new ArrayList<>();
        for (Map<String, String> item : foodItems) {
            MealFoodItem foodItem = new MealFoodItem();
            foodItem.setFoodItem(item.get("foodName"));
            foodItem.setQuantity(Double.parseDouble(item.get("quantity")));
            foodItem.setUnit(item.get("unit"));

            double calories = nutritionixService.getCalories(foodItem);
            estimatedCalories.add(calories);
        }
        return ResponseEntity.ok(estimatedCalories);  
    }

    @PostMapping("/meals/add")
    public String addMeal(@ModelAttribute Meal meal) {
        if (meal.getDateTime() == null) {
            meal.setDateTime(LocalDateTime.now());
        }

        if (meal.getFoodItems() != null) {
            for (MealFoodItem item : meal.getFoodItems()) {
                item.setMeal(meal);
                
            
                if (item.getQuantity() <= 0) {
                    item.setQuantity(1); 
                }
                if (item.getUnit() == null || item.getUnit().isEmpty()) {
                    item.setUnit("g"); 
                }

             
                int estimatedCalories = nutritionixService.getCalories(item);
                item.setCalories(estimatedCalories);
            }
        }

        mealService.saveMeal(meal);
        return "redirect:/user/dashboard";
    }
}
