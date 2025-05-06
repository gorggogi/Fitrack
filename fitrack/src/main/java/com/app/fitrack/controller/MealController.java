package com.app.fitrack.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.MealFoodItem;
import com.app.fitrack.service.MealService;
import com.app.fitrack.service.NutritionixService;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;

@Controller
public class MealController {

    private static final Logger logger = LoggerFactory.getLogger(MealController.class);

    @Autowired
    private MealService mealService;

    @Autowired
    private NutritionixService nutritionixService; 

    @Autowired
    private GoalService goalService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/addmeal")
    public String addMeal(Model model) {
        model.addAttribute("meal", new Meal()); 
        return "addmeal";
    }

    @PostMapping("/meals/estimate-calories")
    public ResponseEntity<?> estimateCalories(@RequestBody List<Map<String, Object>> foodItems) {
        try {
            if (foodItems == null || foodItems.isEmpty()) {
                return ResponseEntity.badRequest().body("No food items provided");
            }

            List<Double> estimatedCalories = new ArrayList<>();
            
            for (Map<String, Object> item : foodItems) {
                String foodName = (String) item.get("foodName");
                Double quantity = Double.valueOf(item.get("quantity").toString());
                String unit = (String) item.get("unit");

                if (foodName == null || quantity == null || unit == null) {
                    return ResponseEntity.badRequest().body("Invalid food item format");
                }

                MealFoodItem foodItem = new MealFoodItem();
                foodItem.setFoodItem(foodName);
                foodItem.setQuantity(quantity);
                foodItem.setUnit(unit);

                double calories = nutritionixService.getCalories(foodItem);
                estimatedCalories.add(calories);
            }

            return ResponseEntity.ok(estimatedCalories);
        } catch (Exception e) {
            logger.error("Error estimating calories", e);
            return ResponseEntity.internalServerError().body("Error estimating calories: " + e.getMessage());
        }
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

    @PostMapping("/user/meals/save")
    public String saveMeal(@AuthenticationPrincipal UserDetails userDetails,
                          @ModelAttribute Meal meal,
                          RedirectAttributes redi) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        meal.setUser(user);
        meal.setDateTime(LocalDateTime.now());
        
        mealService.saveMeal(meal);
        
        // Update goals based on the new meal
        goalService.updateGoalsBasedOnActivity(user);
        
        redi.addFlashAttribute("successMessage", "Meal saved successfully!");
        return "redirect:/user/meals";
    }

    @GetMapping("/user/meals")
    public String getLoggedMeals(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("fullName", user.getFullName());

        // Get today's meals
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Meal> todayMeals = mealService.findByUserAndDateTimeBetween(user, startOfDay, endOfDay);
        model.addAttribute("todayMeals", todayMeals);

        // Get past meals and group them by date
        List<Meal> pastMeals = mealService.findByUserAndDateTimeBefore(user, startOfDay);
        Map<LocalDate, List<Meal>> pastMealsByDate = pastMeals.stream()
            .collect(Collectors.groupingBy(meal -> meal.getDateTime().toLocalDate()));
        
        // Sort the map by date in descending order
        Map<LocalDate, List<Meal>> sortedPastMealsByDate = new TreeMap<>(Collections.reverseOrder());
        sortedPastMealsByDate.putAll(pastMealsByDate);
        
        model.addAttribute("pastMealsByDate", sortedPastMealsByDate);

        return "loggedmeals";
    }

    @GetMapping("/user/meals/{id}")
    @ResponseBody
    public ResponseEntity<?> getMeal(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userService.findByEmail(email);
            
            Meal meal = mealService.findByIdAndUser(id, user);
            if (meal == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(meal);
        } catch (Exception e) {
            logger.error("Error fetching meal", e);
            return ResponseEntity.internalServerError().body("Error fetching meal: " + e.getMessage());
        }
    }

    @PostMapping("/user/savemeal")
    public String saveMeal(@AuthenticationPrincipal UserDetails userDetails,
                          @ModelAttribute Meal meal,
                          @RequestParam("foodNames[]") List<String> foodNames,
                          @RequestParam("foodCalories[]") List<Integer> foodCalories,
                          RedirectAttributes redi) {
        try {
            String email = userDetails.getUsername();
            User user = userService.findByEmail(email);
            
            meal.setUser(user);
            meal.setDateTime(LocalDateTime.now());
            
            List<MealFoodItem> foodItems = new ArrayList<>();
            for (int i = 0; i < foodNames.size(); i++) {
                MealFoodItem foodItem = new MealFoodItem();
                foodItem.setFoodItem(foodNames.get(i));
                foodItem.setCalories(foodCalories.get(i));
                foodItem.setMeal(meal);
                foodItems.add(foodItem);
            }
            meal.setFoodItems(foodItems);
            
            mealService.saveMeal(meal);
            goalService.updateGoalsBasedOnActivity(user);
            
            redi.addFlashAttribute("successMessage", "Meal saved successfully!");
        } catch (Exception e) {
            logger.error("Error saving meal", e);
            redi.addFlashAttribute("errorMessage", "Error saving meal: " + e.getMessage());
        }
        
        return "redirect:/user/meals";
    }

    @PostMapping("/user/meals/{id}/update")
    public String updateMeal(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @ModelAttribute Meal meal,
                           @RequestParam("foodNames[]") List<String> foodNames,
                           @RequestParam("foodCalories[]") List<Integer> foodCalories,
                           RedirectAttributes redi) {
        try {
            String email = userDetails.getUsername();
            User user = userService.findByEmail(email);
            
            Meal existingMeal = mealService.findByIdAndUser(id, user);
            if (existingMeal == null) {
                redi.addFlashAttribute("errorMessage", "Meal not found");
                return "redirect:/user/meals";
            }
            
            existingMeal.setMealName(meal.getMealName());
            existingMeal.setDateTime(meal.getDateTime());
            
            // Clear existing food items
            existingMeal.getFoodItems().clear();
            
            // Add new food items
            for (int i = 0; i < foodNames.size(); i++) {
                MealFoodItem foodItem = new MealFoodItem();
                foodItem.setFoodItem(foodNames.get(i));
                foodItem.setCalories(foodCalories.get(i));
                foodItem.setMeal(existingMeal);
                existingMeal.getFoodItems().add(foodItem);
            }
            
            mealService.saveMeal(existingMeal);
            goalService.updateGoalsBasedOnActivity(user);
            
            redi.addFlashAttribute("successMessage", "Meal updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating meal", e);
            redi.addFlashAttribute("errorMessage", "Error updating meal: " + e.getMessage());
        }
        
        return "redirect:/user/meals";
    }

    @PostMapping("/user/meals/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteMeal(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userService.findByEmail(email);
            
            Meal meal = mealService.findByIdAndUser(id, user);
            if (meal == null) {
                return ResponseEntity.notFound().build();
            }
            
            mealService.deleteMeal(meal);
            goalService.updateGoalsBasedOnActivity(user);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting meal", e);
            return ResponseEntity.internalServerError().body("Error deleting meal: " + e.getMessage());
        }
    }
}
