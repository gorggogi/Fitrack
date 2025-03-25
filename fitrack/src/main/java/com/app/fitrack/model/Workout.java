package com.app.fitrack.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String workoutName; 

    @Column(nullable = false)
    private Integer duration; 

    @Column(nullable = false)
    private double burnedCalories;

    @ElementCollection(fetch = FetchType.EAGER)  
    @CollectionTable(name = "workout_repeat_days", joinColumns = @JoinColumn(name = "workout_id"))
    @Column(name = "repeat_day")
    private List<String> repeatDays;  

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateTime;

    public Workout() {
    this.dateTime = LocalDateTime.now();
    this.repeatDays = new ArrayList<>(List.of("Daily")); 
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

    public double getBurnedCalories() {
        return burnedCalories;
    }

    public void setBurnedCalories(double burnedCalories) {
        this.burnedCalories = burnedCalories;
    }

    public List<String> getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(List<String> repeatDays) {
        this.repeatDays = repeatDays;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    
}
