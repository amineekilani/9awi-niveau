package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.ResultatQuiz;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de mise à jour automatique des recommandations
 * Gère les déclencheurs et la fréquence des mises à jour
 */
@Service
public class RecommendationUpdateService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationUpdateService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RecommendationService recommendationService;
    
    // Cache des dernières mises à jour par utilisateur
    private final Map<Long, LocalDateTime> lastUpdateCache = new ConcurrentHashMap<>();
    
    // Seuil de mise à jour (en minutes)
    private static final int UPDATE_THRESHOLD_MINUTES = 30;
    
    /**
     * Déclenche une mise à jour des recommandations pour un utilisateur
     * après une action significative
     */
    @Async
    public void triggerRecommendationUpdate(Long userId, String trigger) {
        try {
            logger.info("Déclenchement mise à jour recommandations pour utilisateur {} - Trigger: {}", userId, trigger);
            
            // Vérifier si une mise à jour récente a déjà eu lieu
            LocalDateTime lastUpdate = lastUpdateCache.get(userId);
            LocalDateTime now = LocalDateTime.now();
            
            if (lastUpdate != null && lastUpdate.plusMinutes(UPDATE_THRESHOLD_MINUTES).isAfter(now)) {
                logger.debug("Mise à jour récente détectée pour utilisateur {}, skip", userId);
                return;
            }
            
            // Marquer la mise à jour
            lastUpdateCache.put(userId, now);
            
            // Pré-calculer les recommandations (cache warming)
            recommendationService.generateRecommendations(userId);
            
            logger.info("Recommandations mises à jour avec succès pour utilisateur {} - Trigger: {}", userId, trigger);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des recommandations pour utilisateur {} - Trigger: {}", 
                    userId, trigger, e);
        }
    }
    
    /**
     * Mise à jour automatique périodique (toutes les heures)
     * Pour les utilisateurs actifs récemment
     */
    @Scheduled(fixedRate = 3600000) // 1 heure
    public void scheduledRecommendationUpdate() {
        try {
            logger.info("Démarrage mise à jour périodique des recommandations");
            
            // Récupérer les utilisateurs actifs dans les dernières 24h
            // (Simplification : on prend tous les utilisateurs non archivés)
            userRepository.findAll().stream()
                    .filter(user -> !user.isArchived())
                    .limit(50) // Limiter pour éviter la surcharge
                    .forEach(user -> {
                        try {
                            triggerRecommendationUpdate(user.getId(), "SCHEDULED_UPDATE");
                            Thread.sleep(100); // Petite pause entre les utilisateurs
                        } catch (Exception e) {
                            logger.warn("Erreur mise à jour périodique pour utilisateur {}", user.getId(), e);
                        }
                    });
            
            logger.info("Mise à jour périodique terminée");
            
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour périodique", e);
        }
    }
    
    /**
     * Nettoyage du cache (tous les jours à 2h du matin)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupCache() {
        logger.info("Nettoyage du cache de mise à jour des recommandations");
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        
        lastUpdateCache.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        logger.info("Cache nettoyé, {} entrées conservées", lastUpdateCache.size());
    }
    
    /**
     * Force la mise à jour pour un utilisateur (ignore le seuil)
     */
    public void forceUpdate(Long userId, String reason) {
        logger.info("Mise à jour forcée pour utilisateur {} - Raison: {}", userId, reason);
        
        // Supprimer du cache pour forcer la mise à jour
        lastUpdateCache.remove(userId);
        
        // Déclencher la mise à jour
        triggerRecommendationUpdate(userId, "FORCE_UPDATE: " + reason);
    }
    
    /**
     * Vérifie si un utilisateur a besoin d'une mise à jour
     */
    public boolean needsUpdate(Long userId) {
        LocalDateTime lastUpdate = lastUpdateCache.get(userId);
        
        if (lastUpdate == null) {
            return true; // Jamais mis à jour
        }
        
        return lastUpdate.plusMinutes(UPDATE_THRESHOLD_MINUTES).isBefore(LocalDateTime.now());
    }
    
    /**
     * Statistiques du service de mise à jour
     */
    public Map<String, Object> getUpdateStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedUsers", lastUpdateCache.size());
        stats.put("updateThresholdMinutes", UPDATE_THRESHOLD_MINUTES);
        stats.put("lastCleanup", LocalDateTime.now().withHour(2).withMinute(0).withSecond(0));
        
        return stats;
    }
}