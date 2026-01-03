package com.kawi_niveau.backend.aspect;

import com.kawi_niveau.backend.event.RecommendationUpdateEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect pour déclencher automatiquement les mises à jour de recommandations
 * après certaines actions utilisateur
 */
@Aspect
@Component
public class RecommendationUpdateAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationUpdateAspect.class);
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Déclenche une mise à jour après inscription à un cours
     */
    @AfterReturning(
        pointcut = "execution(* com.kawi_niveau.backend.service.EnrollmentService.enrollInCourse(..))",
        returning = "result"
    )
    public void afterCourseEnrollment(JoinPoint joinPoint, Object result) {
        try {
            // Extraire l'ID utilisateur du résultat ou des paramètres
            if (result != null) {
                logger.info("Déclenchement mise à jour après inscription à un cours");
                // Note: Il faudrait extraire l'userId du résultat
                // Pour l'instant, on log juste l'événement
                // eventPublisher.publishEvent(new RecommendationUpdateEvent(this, userId, "COURSE_ENROLLMENT"));
            }
        } catch (Exception e) {
            logger.warn("Erreur lors du déclenchement de mise à jour après inscription", e);
        }
    }
    
    /**
     * Déclenche une mise à jour après complétion d'une leçon
     */
    @AfterReturning(
        pointcut = "execution(* com.kawi_niveau.backend.service.EnrollmentService.markLeconAsCompleted(..))",
        returning = "result"
    )
    public void afterLeconCompletion(JoinPoint joinPoint, Object result) {
        try {
            logger.info("Déclenchement mise à jour après complétion de leçon");
            // eventPublisher.publishEvent(new RecommendationUpdateEvent(this, userId, "LECON_COMPLETED"));
        } catch (Exception e) {
            logger.warn("Erreur lors du déclenchement de mise à jour après complétion leçon", e);
        }
    }
    
    /**
     * Déclenche une mise à jour après passage d'un quiz
     */
    @AfterReturning(
        pointcut = "execution(* com.kawi_niveau.backend.service.QuizResultatService.saveQuizResult(..))",
        returning = "result"
    )
    public void afterQuizCompletion(JoinPoint joinPoint, Object result) {
        try {
            logger.info("Déclenchement mise à jour après passage de quiz");
            // eventPublisher.publishEvent(new RecommendationUpdateEvent(this, userId, "QUIZ_COMPLETED"));
        } catch (Exception e) {
            logger.warn("Erreur lors du déclenchement de mise à jour après quiz", e);
        }
    }
}