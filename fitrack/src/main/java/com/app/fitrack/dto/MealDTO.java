package com.app.fitrack.dto;

import java.time.LocalDateTime;
import java.util.List;

// Moved from AnalyticsController
public class MealDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String mealName;
    private List<FoodItemDTO> foodItems;
    private int totalCalories; // Added total calories

    // Constructor including total calories
    public MealDTO(Long id, LocalDateTime dateTime, String mealName, List<FoodItemDTO> foodItems, int totalCalories) {
        this.id = id;
        this.dateTime = dateTime;
        this.mealName = mealName;
        this.foodItems = foodItems;
        this.totalCalories = totalCalories;
    }

    // Getters
    public Long getId() { return id; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getMealName() { return mealName; }
    public List<FoodItemDTO> getFoodItems() { return foodItems; }
    public int getTotalCalories() { return totalCalories; } // Getter for total calories
} 