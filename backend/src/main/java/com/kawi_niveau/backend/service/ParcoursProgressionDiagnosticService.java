package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcoursProgressionDiagnosticService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ParcoursNotificationRepository notificationRepository;

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private ParcoursProgressionService progressionService;

    @Autowired
    private ParcoursNotificationService notificationService;

    @Autowired
    private GamificationService gamificationService;

    /**
     * Diagnostic complet pour un utilisateur
     */
    public String diagnosticUtilisateur(String userEmail) {
        StringBuilder diagnostic = new StringBuilder();
        diagnostic.append("=== DIAGNOSTIC PROGRESSION PARCOURS ===\n");
        diagnostic.append("Utilisateur: ").append(userEmail).append("\n\n");

        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                diagnostic.append("❌ ERREUR: Utilisateur non trouvé\n");
                return diagnostic.toString();
            }

            diagnostic.append("✅ Utilisateur trouvé: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n\n");

            // 1. Vérifier les inscriptions aux parcours
            diagnostic.append("--- INSCRIPTIONS PARCOURS ---\n");
            List<ParcoursInscription> inscriptions = inscriptionRepository.findByUser(user);
            diagnostic.append("Nombre d'inscriptions: ").append(inscriptions.size()).append("\n");

            for (ParcoursInscription inscription : inscriptions) {
                diagnostic.append("📋 Parcours: ").append(inscription.getParcours().getTitre()).append("\n");
                diagnostic.append("   - Progression: ").append(inscription.getProgressionPourcentage()).append("%\n");
                diagnostic.append("   - Étape courante: ").append(inscription.getEtapeCourante()).append("\n");
                diagnostic.append("   - Terminé: ").append(inscription.getIsCompleted() ? "OUI" : "NON").append("\n");
                diagnostic.append("   - Points gagnés: ").append(inscription.getPointsGagnes()).append("\n");
                diagnostic.append("   - Date completion: ").append(inscription.getDateCompletion()).append("\n\n");
            }

            // 2. Vérifier les inscriptions aux cours
            diagnostic.append("--- INSCRIPTIONS COURS ---\n");
            List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
            diagnostic.append("Nombre d'inscriptions cours: ").append(enrollments.size()).append("\n");

            for (Enrollment enrollment : enrollments) {
                diagnostic.append("📚 Cours: ").append(enrollment.getCours().getTitre()).append("\n");
                diagnostic.append("   - Progression: ").append(enrollment.getProgress()).append("%\n");
                diagnostic.append("   - Terminé: ").append(enrollment.getProgress() >= 100.0f ? "OUI" : "NON").append("\n\n");
            }

            // 3. Vérifier les XP
            diagnostic.append("--- POINTS XP ---\n");
            Optional<UserXP> userXP = userXPRepository.findByUser(user);
            if (userXP.isPresent()) {
                diagnostic.append("✅ UserXP trouvé\n");
                diagnostic.append("   - Total XP: ").append(userXP.get().getTotalXP()).append("\n");
                diagnostic.append("   - Niveau: ").append(userXP.get().getCurrentLevel()).append("\n");
            } else {
                diagnostic.append("❌ Aucun UserXP trouvé\n");
            }

            // 4. Vérifier les notifications
            diagnostic.append("\n--- NOTIFICATIONS PARCOURS ---\n");
            List<ParcoursNotification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
            diagnostic.append("Nombre de notifications: ").append(notifications.size()).append("\n");

            for (ParcoursNotification notification : notifications) {
                diagnostic.append("📢 ").append(notification.getTitle()).append("\n");
                diagnostic.append("   - Type: ").append(notification.getType()).append("\n");
                diagnostic.append("   - Lu: ").append(notification.getIsRead() ? "OUI" : "NON").append("\n");
                diagnostic.append("   - XP: ").append(notification.getXpEarned()).append("\n");
                diagnostic.append("   - Date: ").append(notification.getCreatedAt()).append("\n\n");
            }

            diagnostic.append("=== FIN DIAGNOSTIC ===\n");

        } catch (Exception e) {
            diagnostic.append("❌ ERREUR LORS DU DIAGNOSTIC: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return diagnostic.toString();
    }

    /**
     * Force la recalculation de la progression d'un utilisateur
     */
    public String forceRecalculProgression(String userEmail) {
        StringBuilder result = new StringBuilder();
        result.append("=== RECALCUL FORCE PROGRESSION ===\n");
        result.append("Utilisateur: ").append(userEmail).append("\n\n");

        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                result.append("❌ ERREUR: Utilisateur non trouvé\n");
                return result.toString();
            }

            result.append("🔄 Début du recalcul...\n");
            progressionService.recalculerProgressionUtilisateur(user);
            result.append("✅ Recalcul terminé avec succès\n");

        } catch (Exception e) {
            result.append("❌ ERREUR LORS DU RECALCUL: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return result.toString();
    }

    /**
     * Teste la création d'une notification de parcours
     */
    public String testNotificationParcours(String userEmail, Long parcoursId) {
        StringBuilder result = new StringBuilder();
        result.append("=== TEST NOTIFICATION PARCOURS ===\n");

        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                result.append("❌ ERREUR: Utilisateur non trouvé\n");
                return result.toString();
            }

            Optional<ParcoursApprentissage> parcoursOpt = parcoursRepository.findById(parcoursId);
            if (parcoursOpt.isEmpty()) {
                result.append("❌ ERREUR: Parcours non trouvé\n");
                return result.toString();
            }

            Optional<ParcoursInscription> inscription = inscriptionRepository.findByParcoursAndUser(parcoursOpt.get(), user);
            if (inscription.isEmpty()) {
                result.append("❌ ERREUR: Inscription au parcours non trouvée\n");
                return result.toString();
            }

            ParcoursApprentissage parcours = inscription.get().getParcours();
            result.append("📋 Parcours: ").append(parcours.getTitre()).append("\n");

            // Tester la création de notification
            notificationService.createParcoursCompletionNotification(
                user, parcours, 100, false, null
            );

            result.append("✅ Notification de test créée avec succès\n");

        } catch (Exception e) {
            result.append("❌ ERREUR LORS DU TEST: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return result.toString();
    }

    /**
     * Teste l'attribution d'XP
     */
    public String testAttributionXP(String userEmail, int xpAmount) {
        StringBuilder result = new StringBuilder();
        result.append("=== TEST ATTRIBUTION XP ===\n");

        try {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user == null) {
                result.append("❌ ERREUR: Utilisateur non trouvé\n");
                return result.toString();
            }

            result.append("🎯 Attribution de ").append(xpAmount).append(" XP à ").append(user.getEmail()).append("\n");

            // Vérifier XP avant
            Optional<UserXP> userXPBefore = userXPRepository.findByUser(user);
            int xpBefore = userXPBefore.map(UserXP::getTotalXP).orElse(0);
            result.append("XP avant: ").append(xpBefore).append("\n");

            // Attribuer XP
            gamificationService.awardXP(user, xpAmount, "Test attribution XP");

            // Vérifier XP après
            Optional<UserXP> userXPAfter = userXPRepository.findByUser(user);
            int xpAfter = userXPAfter.map(UserXP::getTotalXP).orElse(0);
            result.append("XP après: ").append(xpAfter).append("\n");
            result.append("Différence: +").append(xpAfter - xpBefore).append(" XP\n");

            if (xpAfter - xpBefore == xpAmount) {
                result.append("✅ Attribution XP réussie\n");
            } else {
                result.append("❌ Problème d'attribution XP\n");
            }

        } catch (Exception e) {
            result.append("❌ ERREUR LORS DU TEST XP: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return result.toString();
    }
}