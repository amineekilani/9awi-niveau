package com.kawi_niveau.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration pour le moteur de recommandation
 */
@Configuration
@ConfigurationProperties(prefix = "recommendation")
@Data
public class RecommendationConfig {
    
    /**
     * Chemin vers le script Python du moteur ML
     */
    private String pythonScriptPath = "src/main/python/recommendation_engine.py";
    
    /**
     * Commande Python à utiliser (python3, python, etc.)
     */
    private String pythonCommand = "python3";
    
    /**
     * Timeout pour l'exécution du script Python (en millisecondes)
     */
    private long pythonTimeout = 30000; // 30 secondes
    
    /**
     * Activer/désactiver le moteur ML Python
     */
    private boolean enablePythonEngine = false;
    
    /**
     * Nombre maximum de recommandations par défaut
     */
    private int defaultMaxRecommendations = 10;
    
    /**
     * Score de confiance minimum par défaut
     */
    private double defaultMinConfidence = 0.5;
    
    /**
     * Poids pour les différentes méthodes de recommandation
     */
    private RecommendationWeights weights = new RecommendationWeights();
    
    @Data
    public static class RecommendationWeights {
        private double collaborative = 0.4;
        private double contentBased = 0.4;
        private double levelBased = 0.2;
        private double rulesBased = 0.6; // Poids des règles pédagogiques
    }
    
    /**
     * Configuration des règles pédagogiques
     */
    private PedagogicalRules pedagogicalRules = new PedagogicalRules();
    
    @Data
    public static class PedagogicalRules {
        
        /**
         * Score minimum pour recommander un contenu plus avancé
         */
        private double advancedContentThreshold = 80.0;
        
        /**
         * Score maximum pour recommander un contenu de révision
         */
        private double reviewContentThreshold = 50.0;
        
        /**
         * Nombre minimum de quiz pour analyser les performances
         */
        private int minQuizzesForAnalysis = 2;
        
        /**
         * Nombre de jours pour considérer une activité comme récente
         */
        private int recentActivityDays = 7;
        
        /**
         * Progression minimum pour recommander la suite d'un cours
         */
        private double minProgressForContinuation = 10.0;
        
        /**
         * Nombre maximum de cours incomplets à recommander
         */
        private int maxIncompleteCoursesToRecommend = 3;
    }
}