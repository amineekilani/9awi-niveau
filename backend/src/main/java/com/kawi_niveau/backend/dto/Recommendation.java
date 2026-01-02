package com.kawi_niveau.backend.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Modèle de données pour une recommandation pédagogique individuelle
 */
@Data
@Builder
public class Recommendation {
    private String type; // "LECON", "QUIZ", "CHALLENGE", "COURS"
    private Long id;
    private String title;
    private String reason; // Explication pédagogique
    private Integer priority; // 1 = haute priorité
    private Double confidenceScore; // 0.0 à 1.0
}