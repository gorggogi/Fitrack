package com.app.fitrack.repository;

import com.app.fitrack.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foodItems WHERE m.dateTime BETWEEN :start AND :end")
    List<Meal> findByDateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
