package com.kawi_niveau.backend.event;

import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché quand les recommandations d'un utilisateur doivent être mises à jour
 */
public class RecommendationUpdateEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String trigger;
    private final String details;
    
    public RecommendationUpdateEvent(Object source, Long userId, String trigger, String details) {
        super(source);
        this.userId = userId;
        this.trigger = trigger;
        this.details = details;
    }
    
    public RecommendationUpdateEvent(Object source, Long userId, String trigger) {
        this(source, userId, trigger, null);
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getTrigger() {
        return trigger;
    }
    
    public String getDetails() {
        return details;
    }
    
    @Override
    public String toString() {
        return String.format("RecommendationUpdateEvent{userId=%d, trigger='%s', details='%s'}", 
                userId, trigger, details);
    }
}