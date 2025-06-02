package com.app.fitrack.service;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MealService {
    private static final Logger logger = LoggerFactory.getLogger(MealService.class);
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private NutritionixService nutritionixService;

    public Meal saveMeal(Meal meal) {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found.");
        }
        meal.setUser(currentUser);
        if (meal.getDateTime() == null) {
            meal.setDateTime(LocalDateTime.now());
        }

        // Ensure bidirectional relationship and populate nutritional info for food items
        if (meal.getFoodItems() != null) {
            meal.getFoodItems().forEach(item -> {
                item.setMeal(meal); // Set bidirectional relationship
                
                // Handle "other" unit
                if ("other".equalsIgnoreCase(item.getUnit()) && item.getOtherUnit() != null && !item.getOtherUnit().trim().isEmpty()) {
                    item.setUnit(item.getOtherUnit().trim());
                    // item.setOtherUnit(null); // Optionally clear otherUnit after processing
                }
                
                logger.debug("Processing FoodItem: Name='{}', Qty={}, Unit='{}', CaloriesBeforeLookup={}, ProteinBeforeLookup={}, CarbsBeforeLookup={}, FatBeforeLookup={}",
                    item.getFoodItem(), item.getQuantity(), item.getUnit(),
                    item.getCalories(), item.getProtein(), item.getCarbs(), item.getFat());

                // Condition to check if a Nutritionix lookup is needed for macros
                boolean needsMacroLookup = (item.getProtein() == null || item.getProtein() == 0.0) ||
                                           (item.getCarbs() == null || item.getCarbs() == 0.0) ||
                                                 (item.getFat() == null || item.getFat() == 0.0);

                logger.debug("needsMacroLookup for '{}': {} (Protein: {}, Carbs: {}, Fat: {})", 
                    item.getFoodItem(), needsMacroLookup, item.getProtein(), item.getCarbs(), item.getFat());

                // Call NutritionixService only if macros are missing and key fields for lookup are present
                if (needsMacroLookup && 
                    item.getFoodItem() != null && !item.getFoodItem().isEmpty() &&
                    item.getQuantity() > 0 && 
                    item.getUnit() != null && !item.getUnit().isEmpty()) {
                    try {
                        // This call will fetch all nutrients including calories, protein, carbs, fat
                        nutritionixService.getCalories(item); 
                        logger.debug("Nutritionix lookup performed for '{}'. Calories after: {}, Protein after: {}, Carbs after: {}, Fat after: {}", 
                            item.getFoodItem(), item.getCalories(), item.getProtein(), item.getCarbs(), item.getFat());
                    } catch (Exception e) {
                        // Log error or handle cases where Nutritionix might fail
                        // For now, we'll let it proceed with user-entered calories if service fails
                        System.err.println("Error fetching nutritional data for item: " + item.getFoodItem() + " - " + e.getMessage());
                    }
                }
            });
        }

        return mealRepository.save(meal);
    }

    public List<Meal> getMealsForCurrentDate() {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        List<Meal> meals = mealRepository.findByUserAndDateTimeBetween(currentUser, startOfDay, endOfDay);
        meals.sort(Comparator.comparing(Meal::getDateTime));
        return meals;
    }
    
    public int getTotalCaloriesForCurrentDate() {
        List<Meal> meals = getMealsForCurrentDate();
        return meals.stream().mapToInt(Meal::getTotalCalories).sum();
    }

    public double getAverageDailyCalories(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        recentMeals.sort(Comparator.comparing(Meal::getDateTime));
        
        double totalCalories = recentMeals.stream().mapToDouble(Meal::getTotalCalories).sum();
        // Calculate the actual number of days spanned by the meals, minimum 1
        long daysSpanned = java.time.temporal.ChronoUnit.DAYS.between(recentMeals.get(0).getDateTime().toLocalDate(), endDate.toLocalDate()) + 1;
        
        return totalCalories / daysSpanned;
    }

    public double getTotalProteinForPeriod(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        recentMeals.sort(Comparator.comparing(Meal::getDateTime));
        
        return recentMeals.stream().mapToDouble(Meal::getTotalProtein).sum();
    }

    public double getTotalCarbsForPeriod(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        recentMeals.sort(Comparator.comparing(Meal::getDateTime));
        
        return recentMeals.stream().mapToDouble(Meal::getTotalCarbs).sum();
    }

    public double getTotalFatsForPeriod(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        recentMeals.sort(Comparator.comparing(Meal::getDateTime));
        
        return recentMeals.stream().mapToDouble(Meal::getTotalFat).sum();
    }

    public List<Meal> findMealsByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return mealRepository.findByUserAndDateTimeBetweenOrderByDateTimeDesc(user, startDate, endDate);
    }

    public List<Meal> findByUserAndDateTimeBetween(User user, LocalDateTime start, LocalDateTime end) {
        return mealRepository.findByUserAndDateTimeBetween(user, start, end);
    }

    public List<Meal> findByUserAndDateTimeBefore(User user, LocalDateTime dateTime) {
        return mealRepository.findByUserAndDateTimeBefore(user, dateTime);
    }

    public Meal findByIdAndUser(Long id, User user) {
        return mealRepository.findByIdAndUser(id, user);
    }

    public void deleteMeal(Meal meal) {
        mealRepository.delete(meal);
    }

    public List<Meal> findAllMealsByUserSorted(User user) {
        return mealRepository.findByUserOrderByDateTimeDesc(user);
    }

    public boolean hasAnyMealLogs(User user) {
        return mealRepository.countByUser(user) > 0;
    }
}

