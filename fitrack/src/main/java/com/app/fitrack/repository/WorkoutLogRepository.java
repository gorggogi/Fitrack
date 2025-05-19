package com.app.fitrack.repository;

import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    @Query("SELECT COUNT(w) > 0 FROM WorkoutLog w WHERE w.user = :user AND w.workoutName = :workoutName AND DATE(w.completedAt) = :date")
boolean existsByUserAndWorkoutNameAndDate(@Param("user") User user, 
                                          @Param("workoutName") String workoutName, 
                                          @Param("date") LocalDate date);

    @Query("SELECT COUNT(w) > 0 FROM WorkoutLog w WHERE w.user = :user AND w.workoutName = :workoutName AND w.completedAt >= :startOfDay AND w.completedAt < :endOfDay")
    boolean existsByUserAndWorkoutNameBetweenDates(
        @Param("user") User user,
        @Param("workoutName") String workoutName,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    List<WorkoutLog> findByUserAndCompletedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.user = :user AND w.completedAt BETWEEN :start AND :end")
    long countByUserAndCompletedAtBetween(@Param("user") User user, 
                                         @Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);

    // Find all logs for a user, ordered by completion time descending
    List<WorkoutLog> findByUserOrderByCompletedAtDesc(User user);
}
