package com.app.fitrack.repository;

import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    List<WorkoutLog> findByUser(User user);
}
