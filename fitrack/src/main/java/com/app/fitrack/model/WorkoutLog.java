package com.app.fitrack.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class WorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String workoutName;

    @Column
    private String workoutType;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "burned_calories", nullable = false)
    private Double caloriesBurned;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    public WorkoutLog() {
        this.completedAt = LocalDateTime.now();
        this.caloriesBurned = 0.0;
    }

    public WorkoutLog(User user, String workoutName, int duration, Double caloriesBurned) {
        this.user = user;
        this.workoutName = workoutName;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned != null ? caloriesBurned : 0.0;
        this.workoutType = "GENERAL";
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Double getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Double caloriesBurned) {
        this.caloriesBurned = caloriesBurned != null ? caloriesBurned : 0.0;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }


}
