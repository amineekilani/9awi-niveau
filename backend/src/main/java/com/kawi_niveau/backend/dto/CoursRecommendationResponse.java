package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import lombok.Data;

import java.util.List;

@Data
public class CoursRecommendationResponse {
    private Long id;
    private String titre;
    private String description;
    private String thumbnailUrl;
    private String categorie;
    private NiveauDifficulte niveauDifficulte;
    private String keywords;
    private String formateurNom;
    
    // Données de recommandation
    private Double scoreRecommendation;
    private List<String> raisonsRecommandation;
    private String niveauCorrespondance;
    private Boolean isEnrolled;
    private Integer progressionUtilisateur;
    
    // Scores détaillés
    private Double scoreCategorie;
    private Double scoreDifficulte;
    private Double scorePopularite;
    private Double scoreKeywords;
}
