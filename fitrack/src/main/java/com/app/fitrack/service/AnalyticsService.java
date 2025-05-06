package com.app.fitrack.service;

import com.app.fitrack.dto.AnalyticsPageDTO;
import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.User;
import com.app.fitrack.model.WorkoutLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserService userService;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private MealService mealService;

    public AnalyticsPageDTO getAnalyticsPageData(User user) {
        AnalyticsPageDTO dto = new AnalyticsPageDTO();

        dto.setFullName(user.getFirstName() + " " + user.getLastName());

        List<BodyMeasurement> measurements = bodyMeasurementService.getMeasurementsForUser(user);
        BodyMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);
        dto.setCurrentUserWeight(latestMeasurement != null ? latestMeasurement.getWeight() : null);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate7Days = endDate.minusDays(7);

        dto.setAvgDailyCalories(mealService.getAverageDailyCalories(user, 7));
        dto.setAvgDailyExerciseCalories(workoutService.getAverageDailyExerciseCalories(user, 7));
        
        double tdee = userService.calculateTDEE(user, userService.calculateDynamicActivityFactor(user, 7));
        dto.setTdee(tdee);

        dto.setTotalProtein7Days(mealService.getTotalProteinForPeriod(user, 7));
        dto.setTotalCarbs7Days(mealService.getTotalCarbsForPeriod(user, 7));
        dto.setTotalFats7Days(mealService.getTotalFatsForPeriod(user, 7));

        populateWorkoutChartData(dto, user, startDate7Days, endDate);
        populateMeasurementChartData(dto, user, measurements);
        populateTimelineData(dto, measurements);

        dto.setCurrentBmiValue(bodyMeasurementService.calculateCurrentBMI(user));
        dto.setCurrentBmiCategory(bodyMeasurementService.getBMICategory(dto.getCurrentBmiValue()));

        populateWeightProgressAndTrend(dto, measurements, latestMeasurement);
        populateNutrientNeeds(dto, latestMeasurement, tdee);
        
        return dto;
    }

    private void populateWorkoutChartData(AnalyticsPageDTO dto, User user, LocalDateTime startDate, LocalDateTime endDate) {
        int[] workoutCounts = new int[7];
        int[] caloriesBurned = new int[7];
        LocalDate today = LocalDate.now();
        List<WorkoutLog> recentLogs = workoutService.getWorkoutLogsForUser(user, startDate, endDate);

        for (WorkoutLog log : recentLogs) {
            LocalDate logDate = log.getCompletedAt().toLocalDate();
            long daysAgo = ChronoUnit.DAYS.between(logDate, today);
            if (daysAgo >= 0 && daysAgo < 7) {
                int index = 6 - (int) daysAgo;
                workoutCounts[index]++;
                if (log.getCaloriesBurned() != null) {
                    caloriesBurned[index] += log.getCaloriesBurned().intValue();
                }
            }
        }
        dto.setDailyWorkoutCounts7Days(workoutCounts);
        dto.setDailyCaloriesBurned7Days(caloriesBurned);
    }

    private void populateMeasurementChartData(AnalyticsPageDTO dto, User user, List<BodyMeasurement> measurements) {
        List<Map<String, Object>> chartData = measurements.stream()
            .sorted(Comparator.comparing(BodyMeasurement::getDateTime))
            .map(m -> {
                Map<String, Object> point = new HashMap<>();
                point.put("dateTime", m.getDateTime().toString());
                point.put("weight", m.getWeight());
                point.put("userHeight", user.getHeight());
                if (user.getHeight() != null && user.getHeight() > 0 && m.getWeight() != null) {
                    double heightInMeters = user.getHeight() / 100.0;
                    double bmi = m.getWeight() / (heightInMeters * heightInMeters);
                    point.put("bmi", bmi);
                }
                return point;
            })
            .collect(Collectors.toList());
        dto.setChartMeasurementData(chartData);
    }

    private void populateTimelineData(AnalyticsPageDTO dto, List<BodyMeasurement> measurements) {
        DateTimeFormatter timelineDateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        List<Map<String, Object>> timelineData = measurements.stream()
            // Already sorted desc by service: bodyMeasurementService.getMeasurementsForUser(user)
            // .sorted((m1, m2) -> m2.getDateTime().compareTo(m1.getDateTime())) 
            .map(m -> {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", m.getDateTime().format(timelineDateFormatter));
                entry.put("weight", m.getWeight());
                entry.put("notes", m.getNotes() != null ? m.getNotes() : "");
                return entry;
            })
            .collect(Collectors.toList());
        dto.setTimelineData(timelineData);
    }

    private void populateWeightProgressAndTrend(AnalyticsPageDTO dto, List<BodyMeasurement> measurements, BodyMeasurement latestMeasurement) {
        if (measurements.size() >= 2 && latestMeasurement != null) {
            BodyMeasurement previousMeasurement = measurements.get(1); // measurements are sorted desc
            if (previousMeasurement.getWeight() != null && latestMeasurement.getWeight() != null) {
                 dto.setWeightProgress(latestMeasurement.getWeight() - previousMeasurement.getWeight());
            }

            // Simplified 4-week trend - using RecommendationService might be more robust if complex
            // This version uses the Nth measurement if available.
            if (measurements.size() >= 4) { // Needs at least 4 measurements for a 3-interval trend
                // Assuming measurements are roughly weekly, measurement at index 3 is ~3-4 weeks ago.
                // For a more precise 4-week window, filter by date.
                BodyMeasurement fourWeeksAgoMeasurement = null;
                LocalDateTime fourWeeksAgoDate = latestMeasurement.getDateTime().minusWeeks(4);
                // Find the measurement closest to 4 weeks ago or the oldest within that period
                for (int i = measurements.size() -1; i >=0; i--) {
                    if (!measurements.get(i).getDateTime().isAfter(fourWeeksAgoDate)) {
                        fourWeeksAgoMeasurement = measurements.get(i);
                        break;
                    }
                }
                if (fourWeeksAgoMeasurement == null && measurements.size() > 1) {
                     // Fallback: use the oldest if no measurement is ~4 weeks old but more than one exists
                     fourWeeksAgoMeasurement = measurements.get(measurements.size() -1);
                }


                if (fourWeeksAgoMeasurement != null && latestMeasurement.getWeight() != null && fourWeeksAgoMeasurement.getWeight() != null && !fourWeeksAgoMeasurement.equals(latestMeasurement)) {
                    long daysBetween = ChronoUnit.DAYS.between(
                        fourWeeksAgoMeasurement.getDateTime().toLocalDate(),
                        latestMeasurement.getDateTime().toLocalDate()
                    );

                    if (daysBetween > 0) {
                        double totalWeightChange = latestMeasurement.getWeight() - fourWeeksAgoMeasurement.getWeight();
                        double weeklyTrend = (totalWeightChange / daysBetween) * 7;
                        dto.setWeightTrend(weeklyTrend);

                        if (Math.abs(weeklyTrend) < 0.1) {
                            dto.setWeightTrendDescription("Stable");
                            dto.setWeightRecommendation("Your weight is stable. Consider setting specific goals for better progress tracking.");
                        } else if (weeklyTrend > 0) {
                            dto.setWeightTrendDescription(weeklyTrend > 0.5 ? "Rapid gain" : "Gradual gain");
                            dto.setWeightRecommendation(weeklyTrend > 0.5 ? "Rapid weight gain detected. Adjust calorie intake if unintended." : "Steady weight gain observed. Good for muscle building.");
                        } else { // weeklyTrend < 0
                            dto.setWeightTrendDescription(weeklyTrend < -0.5 ? "Rapid loss" : "Gradual loss");
                            dto.setWeightRecommendation(weeklyTrend < -0.5 ? "Rapid weight loss detected. Increase calories if unintended." : "Steady weight loss observed. Good for fat loss.");
                        }
                    }
                } else {
                    dto.setWeightTrendDescription("Insufficient distinct data for 4-week trend");
                    dto.setWeightRecommendation("Keep tracking your measurements for trend analysis.");
                }
            } else {
                dto.setWeightTrendDescription("Insufficient data for 4-week trend");
                dto.setWeightRecommendation("Need more measurements over time for trend analysis.");
            }
        } else {
            dto.setWeightProgress(null);
            dto.setWeightTrend(null);
            dto.setWeightTrendDescription("Getting started");
            dto.setWeightRecommendation("Add more measurements to track your progress.");
        }
    }

    private void populateNutrientNeeds(AnalyticsPageDTO dto, BodyMeasurement latestMeasurement, double tdee) {
        if (latestMeasurement != null && latestMeasurement.getWeight() != null) {
            dto.setProteinNeeds(latestMeasurement.getWeight() * 1.6); // 1.6 g/kg
            dto.setWaterNeeds(latestMeasurement.getWeight() * 0.033); // 33 mL/kg
        } else {
            dto.setProteinNeeds(0);
            dto.setWaterNeeds(0);
        }
        // Carb and Fat needs depend on TDEE which could be non-zero even if weight is null (if user has default values)
        dto.setCarbNeeds(tdee > 0 ? (tdee * 0.4 / 4) : 0); // 40% of calories from carbs
        dto.setFatNeeds(tdee > 0 ? (tdee * 0.3 / 9) : 0);   // 30% of calories from fat
    }
} 