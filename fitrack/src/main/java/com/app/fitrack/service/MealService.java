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

    public Meal saveMeal(Meal meal) {
        User currentUser = userService.getAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user found.");
        }
        meal.setUser(currentUser);
        if (meal.getDateTime() == null) {
            meal.setDateTime(LocalDateTime.now());
        }

        // Ensure bidirectional relationship is set for food items
        if (meal.getFoodItems() != null) {
            meal.getFoodItems().forEach(item -> item.setMeal(meal));
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

    public double getAverageDailyProtein(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        return recentMeals.stream().mapToDouble(Meal::getTotalProtein).sum();
    }

    public double getAverageDailyCarbs(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        return recentMeals.stream().mapToDouble(Meal::getTotalCarbs).sum();
    }

    public double getAverageDailyFats(User user, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        List<Meal> recentMeals = mealRepository.findByUserAndDateTimeBetween(user, startDate, endDate);
        
        if (recentMeals.isEmpty()) {
            return 0.0;
        }
        
        return recentMeals.stream().mapToDouble(Meal::getTotalFat).sum();
    }
}

