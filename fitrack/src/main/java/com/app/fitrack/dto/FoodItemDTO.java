package com.app.fitrack.dto;

// Moved from AnalyticsController
public class FoodItemDTO {
    private String foodItem;
    private Double protein;
    private Double carbs;
    private Double fat;
    private int calories; // Added calories

    // Constructor including calories
    public FoodItemDTO(String foodItem, Double protein, Double carbs, Double fat, int calories) {
        this.foodItem = foodItem;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.calories = calories;
    }

    // Getters
    public String getFoodItem() { return foodItem; }
    public Double getProtein() { return protein; }
    public Double getCarbs() { return carbs; }
    public Double getFat() { return fat; }
    public int getCalories() { return calories; } // Getter for calories
} 