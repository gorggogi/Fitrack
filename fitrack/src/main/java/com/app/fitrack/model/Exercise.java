package com.app.fitrack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Exercise {
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("duration_min")
    private double duration_min;
    
    @JsonProperty("nf_calories")
    private double nf_calories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDuration_min() {
        return duration_min;
    }

    public void setDuration_min(double duration_min) {
        this.duration_min = duration_min;
    }

    public double getNf_calories() {
        return nf_calories;
    }

    public void setNf_calories(double nf_calories) {
        this.nf_calories = nf_calories;
    }
} 