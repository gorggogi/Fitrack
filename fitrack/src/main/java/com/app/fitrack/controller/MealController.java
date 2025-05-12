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
import org.springframework.http.HttpStatus;
import com.app.fitrack.dto.FoodItemDTO;
import com.app.fitrack.dto.MealDTO;

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

    @PostMapping("/user/meals/save")
    @ResponseBody
    public ResponseEntity<?> saveMealAjax(@AuthenticationPrincipal UserDetails userDetails,
                                       @ModelAttribute Meal meal) {
        User user = null; 
        try {
            String email = userDetails.getUsername();
            user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found. Please log in again.");
            }
            
            // Populate nutritional information for each food item
            if (meal.getFoodItems() != null) {
                for (MealFoodItem foodItem : meal.getFoodItems()) {
                    nutritionixService.getCalories(foodItem); // This will populate protein, carbs, and fat
                }
            }
            
            Meal savedMeal = mealService.saveMeal(meal); 
            
            goalService.updateGoalsBasedOnActivity(user);
            
            List<Meal> todayMeals = mealService.getMealsForCurrentDate();

            List<MealDTO> todayMealDTOs = todayMeals.stream()
                .map(m -> new MealDTO(
                    m.getId(),
                    m.getDateTime(),
                    m.getMealName(),
                    m.getFoodItems().stream()
                        .map(fi -> new FoodItemDTO(
                            fi.getFoodItem(),
                            fi.getProtein(),
                            fi.getCarbs(),
                            fi.getFat(),
                            fi.getCalories() 
                        ))
                        .collect(Collectors.toList()),
                    m.getTotalCalories() 
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(todayMealDTOs);
        } catch (Exception e) {
             logger.error("Error saving meal via AJAX for user {}", (user != null ? user.getEmail() : "UNKNOWN"), e);
             return ResponseEntity.internalServerError().body("Error saving meal: " + e.getMessage());
        }
    }

    @GetMapping("/user/meals")
    public String getLoggedMeals(Model model, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("user", user);

        // Fetch all meals for the user, sorted by date/time descending
        List<Meal> allMeals = mealService.findAllMealsByUserSorted(user); // Assuming this method exists or will be created in MealService

        // Group meals by date
        Map<LocalDate, List<Meal>> groupedMeals = allMeals.stream()
            .collect(Collectors.groupingBy(
                meal -> meal.getDateTime().toLocalDate(),
                LinkedHashMap::new, // Use LinkedHashMap to preserve insertion order (date order)
                Collectors.toList()
            ));
        
        model.addAttribute("groupedMeals", groupedMeals);

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

            // Build a map for the frontend
            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("id", meal.getId());
            mealMap.put("mealName", meal.getMealName());
            mealMap.put("dateTime", meal.getDateTime());
            List<Map<String, Object>> foodItems = new ArrayList<>();
            for (MealFoodItem item : meal.getFoodItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("foodItem", item.getFoodItem());
                itemMap.put("calories", item.getCalories());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit", item.getUnit());
                foodItems.add(itemMap);
            }
            mealMap.put("foodItems", foodItems);

            return ResponseEntity.ok(mealMap);
        } catch (Exception e) {
            logger.error("Error fetching meal", e);
            return ResponseEntity.internalServerError().body("Error fetching meal: " + e.getMessage());
        }
    }

    @PostMapping("/user/meals/{id}/update")
    public String updateMeal(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           @ModelAttribute Meal submittedMeal,
                           RedirectAttributes redi) {
        User user = null;
        try {
            String email = userDetails.getUsername();
            user = userService.findByEmail(email);
            
            Meal existingMeal = mealService.findByIdAndUser(id, user);
            if (existingMeal == null) {
                redi.addFlashAttribute("errorMessage", "Meal not found or you do not have permission to edit it.");
                return "redirect:/user/meals";
            }
            
            existingMeal.setMealName(submittedMeal.getMealName());
            if (submittedMeal.getDateTime() != null) {
                 existingMeal.setDateTime(submittedMeal.getDateTime());
            }
            
            existingMeal.getFoodItems().clear(); 
            if (submittedMeal.getFoodItems() != null) {
                 submittedMeal.getFoodItems().forEach(item -> {
                    item.setMeal(existingMeal);
                    existingMeal.getFoodItems().add(item);
                 });
            }
            
            mealService.saveMeal(existingMeal);
            
            if(user != null) {
               goalService.updateGoalsBasedOnActivity(user);
            }
            
            redi.addFlashAttribute("successMessage", "Meal updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating meal with ID: {}", id, e);
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
