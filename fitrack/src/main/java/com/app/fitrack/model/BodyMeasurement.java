package com.app.fitrack.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime dateTime;
    private Double weight;
    private Double bodyFatPercentage;
    private Double waist;
    private Double hips;
    private Double chest;
    private Double arms;
    private Double thighs;
    private String notes;

    // Getters and Setters
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(Double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public Double getWaist() {
        return waist;
    }

    public void setWaist(Double waist) {
        this.waist = waist;
    }

    public Double getHips() {
        return hips;
    }

    public void setHips(Double hips) {
        this.hips = hips;
    }

    public Double getChest() {
        return chest;
    }

    public void setChest(Double chest) {
        this.chest = chest;
    }

    public Double getArms() {
        return arms;
    }

    public void setArms(Double arms) {
        this.arms = arms;
    }

    public Double getThighs() {
        return thighs;
    }

    public void setThighs(Double thighs) {
        this.thighs = thighs;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 