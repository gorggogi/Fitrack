package com.app.fitrack.service;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MealService {
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
                
                // Condition to check if a Nutritionix lookup is needed
                boolean needsNutritionixLookup = (item.getCalories() == 0) && // Calories is int, check for 0
                                                 (item.getProtein() == null || item.getProtein() == 0.0) &&
                                                 (item.getCarbs() == null || item.getCarbs() == 0.0) &&
                                                 (item.getFat() == null || item.getFat() == 0.0);

                // Call NutritionixService only if data seems missing and key fields are present
                if (needsNutritionixLookup && 
                    item.getFoodItem() != null && !item.getFoodItem().isEmpty() &&
                    item.getQuantity() > 0 && // quantity is double, check if greater than 0
                    item.getUnit() != null && !item.getUnit().isEmpty()) {
                    try {
                        // NutritionixService.getCalories updates the item's calories, protein, carbs, fat
                        nutritionixService.getCalories(item); 
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
}

