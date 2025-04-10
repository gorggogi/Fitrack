package com.app.fitrack.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExerciseResponse {
    @JsonProperty("exercises")
    private List<Exercise> exercises;

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }
} 