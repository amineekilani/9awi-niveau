package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationRequest {
    private List<String> preferredCategories;
    private NiveauDifficulte preferredDifficulty;
    private String learningStyle;
    private Integer timeAvailabilityHours;
    private List<String> learningGoals;
    private List<String> interests;
    private String careerFocus;
    private Integer preferredDurationMin;
    private Integer preferredDurationMax;
    private String challengePreference;
    private Boolean certificationImportant;
    private Integer maxRecommendations = 5;
}