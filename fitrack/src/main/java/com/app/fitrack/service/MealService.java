package com.app.fitrack.service;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.MealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MealService {
    @Autowired
    private MealRepository mealRepository;
    
    @Autowired
    private UserService userService;

    public Meal saveMeal(Meal meal) {
        User currentUser = userService.getCurrentUser();
        meal.setUser(currentUser);
        return mealRepository.save(meal);
    }

    public List<Meal> getMealsForCurrentDate() {
        LocalDate today = LocalDate.now();
        User currentUser = userService.getCurrentUser();
        List<Meal> meals = mealRepository.findByUserAndDateTimeBetween(
            currentUser, today.atStartOfDay(), today.atTime(23, 59, 59));

        for (Meal meal : meals) {
            if (meal.getDateTime() == null) {
                System.out.println("Meal with ID " + meal.getId() + " has a null dateTime.");
            }
        }
        
        return meals;
    }

    public int getTotalCaloriesForCurrentDate() {
        LocalDate today = LocalDate.now();
        User currentUser = userService.getCurrentUser();
        List<Meal> meals = mealRepository.findByUserAndDateTimeBetween(
            currentUser, today.atStartOfDay(), today.atTime(23, 59, 59));

        return meals.stream().mapToInt(Meal::getTotalCalories).sum();
    }
}
