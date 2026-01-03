package com.kawi_niveau.backend.event;

import com.kawi_niveau.backend.service.RecommendationUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener pour les événements de mise à jour des recommandations
 */
@Component
public class RecommendationUpdateEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationUpdateEventListener.class);
    
    @Autowired
    private RecommendationUpdateService recommendationUpdateService;
    
    /**
     * Gère les événements de mise à jour des recommandations
     */
    @EventListener
    @Async
    public void handleRecommendationUpdateEvent(RecommendationUpdateEvent event) {
        logger.info("Événement reçu: {}", event);
        
        try {
            recommendationUpdateService.triggerRecommendationUpdate(
                event.getUserId(), 
                event.getTrigger() + (event.getDetails() != null ? " - " + event.getDetails() : "")
            );
        } catch (Exception e) {
            logger.error("Erreur lors du traitement de l'événement: {}", event, e);
        }
    }
}