package com.app.fitrack.repository;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foodItems WHERE m.user = :user AND m.dateTime BETWEEN :start AND :end")
    List<Meal> findByUserAndDateTimeBetween(@Param("user") User user, 
                                            @Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end);
}
