package com.app.fitrack.service;

import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.BodyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BodyMeasurementService {
    private static final Logger logger = LoggerFactory.getLogger(BodyMeasurementService.class);

    @Autowired
    private BodyMeasurementRepository bodyMeasurementRepository;

    public void saveMeasurement(BodyMeasurement measurement) {
        logger.info("Saving measurement: Weight={}, DateTime={}, UserID={}", 
                    measurement.getWeight(), measurement.getDateTime(), measurement.getUser().getId());
        bodyMeasurementRepository.save(measurement);
        logger.info("Measurement saved successfully.");
    }

    public List<BodyMeasurement> getMeasurementsForUser(User user) {
        logger.info("Fetching measurements for UserID={}", user.getId());
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserOrderByDateTimeDesc(user);
        logger.info("Found {} measurements for UserID={}", measurements.size(), user.getId());
        return measurements;
    }

    public List<BodyMeasurement> getMeasurementsForPeriod(User user, LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching measurements for UserID={} between {} and {}", user.getId(), start, end);
        List<BodyMeasurement> measurements = bodyMeasurementRepository.findByUserAndDateTimeBetween(user, start, end);
        logger.info("Found {} measurements in period for UserID={}", measurements.size(), user.getId());
        return measurements;
    }

    public BodyMeasurement getLatestMeasurement(User user) {
        logger.info("Fetching latest measurement for UserID={}", user.getId());
        List<BodyMeasurement> measurements = getMeasurementsForUser(user);
        BodyMeasurement latest = measurements.isEmpty() ? null : measurements.get(0);
        if (latest != null) {
            logger.info("Latest measurement: Weight={}, DateTime={}", latest.getWeight(), latest.getDateTime());
        } else {
            logger.info("No measurements found for UserID={}", user.getId());
        }
        return latest;
    }

    public double calculateProgress(User user, String metric) {
        logger.info("Calculating progress for UserID={} and metric='{}'", user.getId(), metric);
        List<BodyMeasurement> measurements = getMeasurementsForUser(user);
        if (measurements.size() < 2) {
            logger.info("Not enough measurements (< 2) to calculate progress.");
            return 0.0;
        }

        BodyMeasurement latest = measurements.get(0);
        BodyMeasurement previous = measurements.get(1);
        logger.info("Latest measurement (for progress): Weight={}, DateTime={}", latest.getWeight(), latest.getDateTime());
        logger.info("Previous measurement (for progress): Weight={}, DateTime={}", previous.getWeight(), previous.getDateTime());

        double progress = 0.0;
        switch (metric.toLowerCase()) {
            case "weight":
                if (previous.getWeight() == null || latest.getWeight() == null) {
                    logger.warn("Cannot calculate weight progress due to null values.");
                } else {
                    progress = latest.getWeight() - previous.getWeight();
                }
                break;
            case "bmi":
                double latestBmi = calculateBMI(latest);
                double previousBmi = calculateBMI(previous);
                if (latestBmi == 0.0 || previousBmi == 0.0) {
                    logger.warn("Cannot calculate BMI progress due to invalid BMI values.");
                } else {
                    progress = latestBmi - previousBmi;
                }
                break;
            default:
                logger.warn("Unsupported metric for progress calculation: {}", metric);
                break;
        }
        logger.info("Calculated progress for metric '{}': {}", metric, progress);
        return progress;
    }

    public double calculateBMI(BodyMeasurement measurement) {
        if (measurement == null || measurement.getUser() == null || measurement.getWeight() == null || measurement.getUser().getHeight() == null) {
            logger.warn("Cannot calculate BMI due to null values in measurement or user data.");
            return 0.0;
        }
        double heightInMeters = measurement.getUser().getHeight() / 100.0;
        if (heightInMeters == 0) {
            logger.warn("Cannot calculate BMI because height is zero.");
            return 0.0;
        }
        double bmi = measurement.getWeight() / (heightInMeters * heightInMeters);
        logger.debug("Calculated BMI: {} for Weight={}, Height={}", bmi, measurement.getWeight(), measurement.getUser().getHeight());
        return bmi;
    }

    public double calculateCurrentBMI(User user) {
        if (user == null || user.getWeight() == null || user.getHeight() == null) {
            logger.warn("Cannot calculate current BMI due to null values in user data.");
            return 0.0;
        }
        double heightInMeters = user.getHeight() / 100.0;
        if (heightInMeters == 0) {
            logger.warn("Cannot calculate current BMI because height is zero.");
            return 0.0;
        }
        double bmi = user.getWeight() / (heightInMeters * heightInMeters);
        logger.debug("Calculated current BMI: {} for Weight={}, Height={}", bmi, user.getWeight(), user.getHeight());
        return bmi;
    }

    public String getBMICategory(double bmi) {
        if (bmi <= 0) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal weight";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }
} 