package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'intégration pour mettre à jour automatiquement la progression des parcours
 * quand l'utilisateur progresse dans les cours ou réussit des quiz
 */
@Service
@Transactional
public class ParcoursIntegrationService {

    @Autowired
    private ParcoursProgressionService progressionService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoursRepository coursRepository;

    /**
     * Met à jour la progression des parcours quand un utilisateur progresse dans un cours
     */
    public void onCoursProgressUpdated(Long userId, Long coursId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            Cours cours = coursRepository.findById(coursId).orElse(null);
            
            if (user != null && cours != null) {
                progressionService.updateProgressionParcours(user, cours);
            }
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer l'opération principale
            System.err.println("Erreur lors de la mise à jour de la progression des parcours: " + e.getMessage());
        }
    }

    /**
     * Met à jour la progression des parcours quand un utilisateur réussit un quiz
     */
    public void onQuizCompleted(Long userId, Long quizId, double score) {
        try {
            System.out.println("🔄 Intégration parcours: Quiz terminé par user " + userId + ", quiz " + quizId + ", score " + score + "%");
            
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                System.err.println("❌ Utilisateur non trouvé: " + userId);
                return;
            }

            // Trouver le cours associé au quiz via les modules
            // Cette logique dépend de votre structure de données
            // Pour l'instant, on recalcule toute la progression de l'utilisateur
            progressionService.recalculerProgressionUtilisateur(user);
            
            System.out.println("✅ Progression parcours recalculée pour " + user.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de la progression des parcours après quiz: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour la progression quand une leçon est marquée comme complète
     */
    public void onLeconCompleted(Long userId, Long coursId) {
        onCoursProgressUpdated(userId, coursId);
    }

    /**
     * Recalcule la progression de tous les parcours d'un utilisateur
     */
    public void recalculerProgressionComplete(Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                progressionService.recalculerProgressionUtilisateur(user);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du recalcul complet de la progression: " + e.getMessage());
        }
    }
}