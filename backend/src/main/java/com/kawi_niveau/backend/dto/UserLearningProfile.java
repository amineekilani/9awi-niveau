package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.ResultatQuiz;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Profil d'apprentissage de l'utilisateur
 * Utilisé pour analyser les performances et générer des recommandations
 */
@Data
@Builder
public class UserLearningProfile {
    private Long userId;
    private Integer currentLevel;
    private Integer totalXP;
    
    // Données d'activité
    private List<Enrollment> enrollments;
    private List<ResultatQuiz> quizResults;
    
    // Analyses de performance
    private Map<String, Double> categoryPerformance; // Catégorie -> Score moyen
    private List<String> weakAreas; // Domaines à améliorer
    private List<String> strongAreas; // Domaines de force
    
    // Métriques globales
    private Double averageQuizScore;
    private Double completionRate; // Taux de complétion des cours
    
    // Patterns d'apprentissage
    private Integer consecutiveDaysActive;
    private Long lastActivityTimestamp;
    private String preferredLearningTime; // Matin, après-midi, soir
    
    // Préférences de contenu
    private List<String> preferredCategories;
    private String preferredContentType; // Texte, vidéo, pratique
}