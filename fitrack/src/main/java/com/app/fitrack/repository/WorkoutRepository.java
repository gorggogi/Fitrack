package com.app.fitrack.repository;

import com.app.fitrack.model.User;
import com.app.fitrack.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUser(User user);
    List<Workout> findByUserEmailAndDateTimeAfter(String email, LocalDateTime dateTime);
    List<Workout> findByUserAndDateTimeBetween(User user, LocalDateTime start, LocalDateTime end);
}
