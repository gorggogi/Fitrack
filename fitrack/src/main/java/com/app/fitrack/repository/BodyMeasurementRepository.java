package com.app.fitrack.repository;

import com.app.fitrack.model.BodyMeasurement;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findByUserOrderByDateTimeDesc(User user);
    List<BodyMeasurement> findByUserAndDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
} 