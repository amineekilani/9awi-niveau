package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.TypeParcours;
import lombok.Data;

import java.util.List;

@Data
public class ParcoursRecommendationResponse {
    private Long id;
    private String titre;
    private String description;
    private String thumbnailUrl;
    private String categorie;
    private NiveauDifficulte niveauDifficulte;
    private Integer dureeEstimeeHeures;
    private String prerequis;
    private TypeParcours typeParcours;
    private Integer pointsBonus;
    private Boolean certificatEnabled;
    private String formateurNom;
    private Integer nombreEtapes;
    private Integer nombreInscriptions;
    private Double progressionMoyenne;
    
    // Données de recommandation
    private Double scoreRecommendation; // Score de 0 à 100
    private List<String> raisonsRecommandation; // Pourquoi ce parcours est recommandé
    private String niveauCorrespondance; // PARFAIT, BON, ACCEPTABLE
    private Boolean isInscrit;
    private Integer progressionUtilisateur;
    
    // Métadonnées d'analyse
    private Double scoreCategorie;
    private Double scoreDifficulte;
    private Double scoreDuree;
    private Double scorePopularite;
    private Double scorePerformance;
    private Double scorePrerequisMatch;
}