package com.app.fitrack.repository;

import com.app.fitrack.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}