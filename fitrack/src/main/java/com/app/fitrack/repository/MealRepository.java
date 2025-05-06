package com.app.fitrack.repository;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foodItems WHERE m.user = :user AND m.dateTime BETWEEN :start AND :end")
    List<Meal> findByUserAndDateTimeBetween(@Param("user") User user, 
                                            @Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end);

    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foodItems WHERE m.user = :user AND m.dateTime < :dateTime ORDER BY m.dateTime DESC")
    List<Meal> findByUserAndDateTimeBefore(@Param("user") User user, @Param("dateTime") LocalDateTime dateTime);

    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foodItems WHERE m.id = :id AND m.user = :user")
    Meal findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    List<Meal> findByUserAndDateTimeBetweenOrderByDateTimeDesc(User user, LocalDateTime startDate, LocalDateTime endDate);
}
