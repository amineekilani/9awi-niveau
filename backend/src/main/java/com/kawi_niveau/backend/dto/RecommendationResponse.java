package com.kawi_niveau.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * Réponse JSON pour les recommandations pédagogiques
 * Format conforme aux spécifications de l'agent IA
 */
@Data
@Builder
public class RecommendationResponse {
    private Long userId;
    private String generatedAt;
    private List<Recommendation> recommendations;
}