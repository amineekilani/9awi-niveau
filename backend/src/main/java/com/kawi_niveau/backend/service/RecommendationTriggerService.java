package com.kawi_niveau.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service utilitaire pour déclencher les mises à jour de recommandations
 * depuis d'autres services de l'application
 */
@Service
public class RecommendationTriggerService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationTriggerService.class);
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * Déclenche une mise à jour après inscription à un cours
     */
    public void onCourseEnrollment(Long userId, Long coursId) {
        logger.info("Déclenchement mise à jour après inscription - Utilisateur: {}, Cours: {}", userId, coursId);
        recommendationService.triggerUpdateAfterUserAction(userId, "COURSE_ENROLLMENT", "Cours ID: " + coursId);
    }
    
    /**
     * Déclenche une mise à jour après complétion d'une leçon
     */
    public void onLeconCompletion(Long userId, Long leconId, Float newProgress) {
        logger.info("Déclenchement mise à jour après leçon - Utilisateur: {}, Leçon: {}, Progression: {}%", 
                userId, leconId, newProgress);
        recommendationService.triggerUpdateAfterUserAction(userId, "LECON_COMPLETED", 
                String.format("Leçon ID: %d, Progression: %.1f%%", leconId, newProgress));
    }
    
    /**
     * Déclenche une mise à jour après passage d'un quiz
     */
    public void onQuizCompletion(Long userId, Long quizId, Double score) {
        logger.info("Déclenchement mise à jour après quiz - Utilisateur: {}, Quiz: {}, Score: {}%", 
                userId, quizId, score);
        
        String performance = score >= 80 ? "EXCELLENT" : score >= 60 ? "BON" : "À_AMÉLIORER";
        recommendationService.triggerUpdateAfterUserAction(userId, "QUIZ_COMPLETED", 
                String.format("Quiz ID: %d, Score: %.1f%% (%s)", quizId, score, performance));
    }
    
    /**
     * Déclenche une mise à jour après gain de XP
     */
    public void onXPGain(Long userId, Integer xpGained, Integer newLevel) {
        logger.info("Déclenchement mise à jour après gain XP - Utilisateur: {}, XP: {}, Niveau: {}", 
                userId, xpGained, newLevel);
        recommendationService.triggerUpdateAfterUserAction(userId, "XP_GAINED", 
                String.format("XP: +%d, Nouveau niveau: %d", xpGained, newLevel));
    }
    
    /**
     * Déclenche une mise à jour après obtention d'un badge
     */
    public void onBadgeEarned(Long userId, Long badgeId, String badgeName) {
        logger.info("Déclenchement mise à jour après badge - Utilisateur: {}, Badge: {} ({})", 
                userId, badgeId, badgeName);
        recommendationService.triggerUpdateAfterUserAction(userId, "BADGE_EARNED", 
                String.format("Badge: %s (ID: %d)", badgeName, badgeId));
    }
    
    /**
     * Déclenche une mise à jour après complétion d'un défi
     */
    public void onChallengeCompleted(Long userId, Long challengeId, String challengeName) {
        logger.info("Déclenchement mise à jour après défi - Utilisateur: {}, Défi: {} ({})", 
                userId, challengeId, challengeName);
        recommendationService.triggerUpdateAfterUserAction(userId, "CHALLENGE_COMPLETED", 
                String.format("Défi: %s (ID: %d)", challengeName, challengeId));
    }
    
    /**
     * Déclenche une mise à jour après connexion utilisateur
     */
    public void onUserLogin(Long userId) {
        logger.debug("Déclenchement mise à jour après connexion - Utilisateur: {}", userId);
        recommendationService.triggerUpdateAfterUserAction(userId, "USER_LOGIN", "Nouvelle session");
    }
    
    /**
     * Force une mise à jour immédiate (pour les administrateurs)
     */
    public void forceUpdate(Long userId, String reason) {
        logger.info("Mise à jour forcée - Utilisateur: {}, Raison: {}", userId, reason);
        recommendationService.invalidateRecommendationsCache(userId, "FORCE_UPDATE: " + reason);
    }
}