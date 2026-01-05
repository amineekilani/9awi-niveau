package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.event.CourseCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service temporaire pour tester la progression automatique
 * Remplace EnrollmentService qui a des problèmes de compilation
 */
@Service
public class EnrollmentTestService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LeconCompletionRepository leconCompletionRepository;

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ParcoursIntegrationService parcoursIntegrationService;

    @Autowired
    private GamificationService gamificationService;

    /**
     * Méthode de test pour marquer une leçon comme terminée et déclencher la progression
     */
    @Transactional
    public void testMarkLeconAsCompleted(Long userId, Long coursId, Long leconId) {
        try {
            System.out.println("🧪 TEST - Marquage leçon terminée: User=" + userId + ", Cours=" + coursId + ", Leçon=" + leconId);
            
            // Trouver l'enrollment
            Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(userId); // Simplifié pour test
            if (enrollmentOpt.isEmpty()) {
                System.out.println("❌ Enrollment non trouvé");
                return;
            }
            
            Enrollment enrollment = enrollmentOpt.get();
            System.out.println("✅ Enrollment trouvé: " + enrollment.getCours().getTitre());
            
            // Forcer la mise à jour de progression
            testUpdateProgress(enrollment);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode de test pour mettre à jour la progression
     */
    private void testUpdateProgress(Enrollment enrollment) {
        System.out.println("🔄 TEST - DÉBUT updateProgress pour cours: " + enrollment.getCours().getTitre());
        
        try {
            // Compter le nombre total de leçons dans le cours
            List<com.kawi_niveau.backend.entity.Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(enrollment.getCours());
            long totalLecons = modules.stream()
                    .mapToLong(module -> leconRepository.findByModuleOrderByOrdreAsc(module).size())
                    .sum();

            System.out.println("📊 Nombre total de leçons: " + totalLecons);

            if (totalLecons == 0) {
                enrollment.setProgress(0.0f);
                System.out.println("⚠️ Aucune leçon dans le cours - Progression: 0%");
            } else {
                long completedLecons = leconCompletionRepository.countByEnrollment(enrollment);
                float progress = (float) completedLecons / totalLecons * 100;
                enrollment.setProgress(Math.round(progress * 100) / 100.0f);
                
                System.out.println("📊 Leçons complétées: " + completedLecons + "/" + totalLecons);
                System.out.println("📊 Progression calculée: " + progress + "%");
                System.out.println("📊 Progression arrondie: " + enrollment.getProgress() + "%");
                
                // Vérifier si le cours est terminé (100% de progression)
                if (progress >= 100.0f) {
                    System.out.println("🎉 COURS TERMINÉ À 100% - Déclenchement des événements");
                    
                    // Déclencher l'événement de cours terminé pour la gamification
                    try {
                        gamificationService.onCourseCompleted(enrollment.getUser());
                        System.out.println("✅ Gamification cours terminé déclenchée");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur gamification: " + e.getMessage());
                    }
                    
                    // Publier l'événement pour les parcours
                    try {
                        System.out.println("📢 Publication événement CourseCompletedEvent");
                        eventPublisher.publishEvent(new CourseCompletedEvent(this, enrollment.getUser(), enrollment.getCours(), progress));
                        System.out.println("✅ Événement CourseCompletedEvent publié avec succès");
                    } catch (Exception e) {
                        System.err.println("❌ Erreur publication événement: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Mettre à jour la progression des parcours directement
                    try {
                        System.out.println("🔄 Mise à jour progression parcours directe");
                        parcoursIntegrationService.onCoursProgressUpdated(enrollment.getUser().getId(), enrollment.getCours().getId());
                        System.out.println("✅ Progression parcours mise à jour après completion cours: " + enrollment.getCours().getTitre());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur mise à jour progression parcours: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("ℹ️ Cours pas encore terminé: " + progress + "% < 100%");
                }
            }

            enrollmentRepository.save(enrollment);
            System.out.println("💾 Enrollment sauvegardé avec progression: " + enrollment.getProgress() + "%");
            System.out.println("🔄 FIN updateProgress");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans testUpdateProgress: " + e.getMessage());
            e.printStackTrace();
        }
    }
}