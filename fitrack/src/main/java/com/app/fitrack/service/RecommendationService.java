package com.app.fitrack.service;

import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.BodyMeasurementRepository;
import com.app.fitrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class RecommendationService {

    @Autowired
    private BodyMeasurementRepository bodyMeasurementRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> generateRecommendations(Long userId) {
        // Get user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("actualWeeklyTrend", 0.0);
            return error;
        }

        // Get user's latest measurement
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserOrderByDateTimeDesc(user);
        BodyMeasurement latestMeasurement = measurements.isEmpty() ? null : measurements.get(0);
        if (latestMeasurement == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("actualWeeklyTrend", 0.0);
            return error;
        }
        
        // Calculate actual weight trend (4-week average)
        double actualWeeklyTrend = calculateActualWeightTrend(user);

        Map<String, Object> result = new HashMap<>();
        result.put("actualWeeklyTrend", actualWeeklyTrend);
        return result;
    }

    private double calculateActualWeightTrend(User user) {
        List<BodyMeasurement> measurements = bodyMeasurementRepository
            .findByUserOrderByDateTimeDesc(user);
            
        if (measurements.size() < 2) return 0;
        
        // Get first and last measurement in last 4 weeks
        BodyMeasurement newest = measurements.get(0);
        BodyMeasurement oldest = measurements.stream()
            .filter(m -> m.getDateTime().isAfter(newest.getDateTime().minusWeeks(4)))
            .reduce((first, last) -> last)
            .orElse(measurements.get(measurements.size()-1));
            
        double weightDiff = newest.getWeight() - oldest.getWeight();
        long daysBetween = java.time.Duration.between(oldest.getDateTime(), newest.getDateTime()).toDays();
        long weeks = daysBetween / 7;
        weeks = weeks == 0 ? 1 : weeks; // prevent division by zero
        
        return weightDiff / weeks;
    }
} 