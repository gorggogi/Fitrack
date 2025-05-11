package com.app.fitrack.dto;

import java.util.List;
import java.util.Map;

// Using Lombok annotations for boilerplate code, but will write out if not available.
// For now, assuming getters/setters/constructors will be implicitly handled or written out.

public class AnalyticsPageDTO {

    private String fullName;
    private Double currentUserWeight;
    private Double weightProgress; // Difference from previous measurement
    private String weightTrendDescription; // e.g., "Stable", "Gradual gain"
    private String weightRecommendation;
    private Double weightTrend; // kg per week

    private double avgDailyCalories;
    private double avgDailyExerciseCalories;
    private double tdee;

    private double totalProtein7Days;
    private double totalCarbs7Days;
    private double totalFats7Days;

    private double currentBmiValue;
    private String currentBmiCategory;

    private double proteinNeeds; // g/day
    private double carbNeeds;    // g/day
    private double fatNeeds;     // g/day
    private double waterNeeds;   // L/day

    // Chart data
    private int[] dailyWorkoutCounts7Days; // Count of workouts per day for last 7 days
    private int[] dailyCaloriesBurned7Days; // Calories burned from workouts per day
    private List<Map<String, Object>> chartMeasurementData; // Historical weight/BMI for charts
    private List<Map<String, Object>> timelineData; // Formatted measurements for a timeline view
    private List<Map<String, Object>> workoutTypeData; // Workout counts by type for last 7 days

    // Constructor (example - can be more detailed or use a builder)
    public AnalyticsPageDTO() {}

    // Getters and Setters for all fields (essential)

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Double getCurrentUserWeight() {
        return currentUserWeight;
    }

    public void setCurrentUserWeight(Double currentUserWeight) {
        this.currentUserWeight = currentUserWeight;
    }

    public Double getWeightProgress() {
        return weightProgress;
    }

    public void setWeightProgress(Double weightProgress) {
        this.weightProgress = weightProgress;
    }

    public String getWeightTrendDescription() {
        return weightTrendDescription;
    }

    public void setWeightTrendDescription(String weightTrendDescription) {
        this.weightTrendDescription = weightTrendDescription;
    }

    public String getWeightRecommendation() {
        return weightRecommendation;
    }

    public void setWeightRecommendation(String weightRecommendation) {
        this.weightRecommendation = weightRecommendation;
    }

    public Double getWeightTrend() {
        return weightTrend;
    }

    public void setWeightTrend(Double weightTrend) {
        this.weightTrend = weightTrend;
    }

    public double getAvgDailyCalories() {
        return avgDailyCalories;
    }

    public void setAvgDailyCalories(double avgDailyCalories) {
        this.avgDailyCalories = avgDailyCalories;
    }

    public double getAvgDailyExerciseCalories() {
        return avgDailyExerciseCalories;
    }

    public void setAvgDailyExerciseCalories(double avgDailyExerciseCalories) {
        this.avgDailyExerciseCalories = avgDailyExerciseCalories;
    }

    public double getTdee() {
        return tdee;
    }

    public void setTdee(double tdee) {
        this.tdee = tdee;
    }

    public double getTotalProtein7Days() {
        return totalProtein7Days;
    }

    public void setTotalProtein7Days(double totalProtein7Days) {
        this.totalProtein7Days = totalProtein7Days;
    }

    public double getTotalCarbs7Days() {
        return totalCarbs7Days;
    }

    public void setTotalCarbs7Days(double totalCarbs7Days) {
        this.totalCarbs7Days = totalCarbs7Days;
    }

    public double getTotalFats7Days() {
        return totalFats7Days;
    }

    public void setTotalFats7Days(double totalFats7Days) {
        this.totalFats7Days = totalFats7Days;
    }

    public double getCurrentBmiValue() {
        return currentBmiValue;
    }

    public void setCurrentBmiValue(double currentBmiValue) {
        this.currentBmiValue = currentBmiValue;
    }

    public String getCurrentBmiCategory() {
        return currentBmiCategory;
    }

    public void setCurrentBmiCategory(String currentBmiCategory) {
        this.currentBmiCategory = currentBmiCategory;
    }

    public double getProteinNeeds() {
        return proteinNeeds;
    }

    public void setProteinNeeds(double proteinNeeds) {
        this.proteinNeeds = proteinNeeds;
    }

    public double getCarbNeeds() {
        return carbNeeds;
    }

    public void setCarbNeeds(double carbNeeds) {
        this.carbNeeds = carbNeeds;
    }

    public double getFatNeeds() {
        return fatNeeds;
    }

    public void setFatNeeds(double fatNeeds) {
        this.fatNeeds = fatNeeds;
    }

    public double getWaterNeeds() {
        return waterNeeds;
    }

    public void setWaterNeeds(double waterNeeds) {
        this.waterNeeds = waterNeeds;
    }

    public int[] getDailyWorkoutCounts7Days() {
        return dailyWorkoutCounts7Days;
    }

    public void setDailyWorkoutCounts7Days(int[] dailyWorkoutCounts7Days) {
        this.dailyWorkoutCounts7Days = dailyWorkoutCounts7Days;
    }

    public int[] getDailyCaloriesBurned7Days() {
        return dailyCaloriesBurned7Days;
    }

    public void setDailyCaloriesBurned7Days(int[] dailyCaloriesBurned7Days) {
        this.dailyCaloriesBurned7Days = dailyCaloriesBurned7Days;
    }

    public List<Map<String, Object>> getChartMeasurementData() {
        return chartMeasurementData;
    }

    public void setChartMeasurementData(List<Map<String, Object>> chartMeasurementData) {
        this.chartMeasurementData = chartMeasurementData;
    }

    public List<Map<String, Object>> getTimelineData() {
        return timelineData;
    }

    public void setTimelineData(List<Map<String, Object>> timelineData) {
        this.timelineData = timelineData;
    }

    public List<Map<String, Object>> getWorkoutTypeData() {
        return workoutTypeData;
    }

    public void setWorkoutTypeData(List<Map<String, Object>> workoutTypeData) {
        this.workoutTypeData = workoutTypeData;
    }
} 