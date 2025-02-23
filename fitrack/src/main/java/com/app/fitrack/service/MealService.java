package com.app.fitrack.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.fitrack.model.Meal;
import com.app.fitrack.repository.MealRepository;

@Service
public class MealService {
    @Autowired
    private MealRepository mealRepository;

    public Meal saveMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    public List<Meal> getMealsForCurrentDate() {
        LocalDate today = LocalDate.now();
        List<Meal> meals = mealRepository.findByDateTimeBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
        
        // Log meals and check for null dateTime
        for (Meal meal : meals) {
            if (meal.getDateTime() == null) {
                System.out.println("Meal with ID " + meal.getId() + " has a null dateTime.");
            }
        }
        
        return meals;
    }

    public int getTotalCaloriesForCurrentDate() {
        LocalDate today = LocalDate.now();
        List<Meal> meals = mealRepository.findByDateTimeBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
        return meals.stream().mapToInt(Meal::getCalories).sum();
    }
}