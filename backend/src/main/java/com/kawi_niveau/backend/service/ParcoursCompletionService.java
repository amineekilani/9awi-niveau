package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service spécialisé pour la validation et completion des parcours
 */
@Service
@Transactional
public class ParcoursCompletionService {

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ParcoursValidationService validationService;

    @Autowired
    private XPSynchronizationService xpSynchronizationService;

    @Autowired
    private ParcoursNotificationService notificationService;

    @Autowired
    private ParcoursNotificationRepository parcoursNotificationRepository;

    /**
     * Force la vérification et completion d'un parcours pour un utilisateur
     */
    public boolean forceCheckParcoursCompletion(User user, ParcoursApprentissage parcours) {
        try {
            System.out.println("🔍 FORCE CHECK - Parcours: " + parcours.getTitre() + " pour " + user.getEmail());

            Optional<ParcoursInscription> inscriptionOpt = inscriptionRepository.findByParcoursAndUser(parcours, user);
            if (inscriptionOpt.isEmpty()) {
                System.out.println("❌ Pas d'inscription au parcours");
                return false;
            }

            ParcoursInscription inscription = inscriptionOpt.get();
            
            // Si déjà terminé, pas besoin de revérifier
            if (inscription.getIsCompleted()) {
                System.out.println("✅ Parcours déjà terminé");
                return true;
            }

            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            System.out.println("📋 Nombre d'étapes: " + etapes.size());

            int etapesCompletes = 0;
            boolean parcoursComplete = true;

            // Vérifier chaque étape avec la logique corrigée
            for (ParcoursEtape etape : etapes) {
                boolean isComplete = isEtapeCompleteWithCorrectLogic(etape, user);
                System.out.println("🔍 Étape " + etape.getOrdreEtape() + " (" + etape.getCours().getTitre() + "): " + (isComplete ? "COMPLÈTE" : "INCOMPLÈTE"));
                
                if (isComplete) {
                    etapesCompletes++;
                } else {
                    parcoursComplete = false;
                }
            }

            // Calculer la progression
            int progressionPourcentage = etapes.isEmpty() ? 0 : (etapesCompletes * 100) / etapes.size();
            System.out.println("📊 Progression calculée: " + etapesCompletes + "/" + etapes.size() + " = " + progressionPourcentage + "%");

            // Mettre à jour l'inscription
            boolean wasCompleted = inscription.getIsCompleted();
            inscription.setProgressionPourcentage(progressionPourcentage);
            inscription.setIsCompleted(parcoursComplete);

            if (parcoursComplete && !wasCompleted) {
                inscription.setDateCompletion(LocalDateTime.now());
                
                // Attribuer les récompenses
                awardCompletionRewards(inscription, user, parcours);
                
                System.out.println("🎉 PARCOURS TERMINÉ: " + parcours.getTitre());
            }

            inscriptionRepository.save(inscription);
            return parcoursComplete;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification forcée: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validation d'étape avec logique corrigée (quiz non obligatoires)
     */
    private boolean isEtapeCompleteWithCorrectLogic(ParcoursEtape etape, User user) {
        try {
            // 1. Vérifier l'inscription au cours
            Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
            if (enrollment.isEmpty()) {
                System.out.println("❌ Pas d'inscription au cours " + etape.getCours().getTitre());
                return false;
            }

            Enrollment enroll = enrollment.get();
            System.out.println("📈 Progression cours: " + enroll.getProgress() + "%");

            // 2. Vérifier le pourcentage de completion requis
            Integer completionRequis = etape.getPourcentageCompletionRequis();
            float progressionRequise = (completionRequis != null && completionRequis > 0) ? completionRequis : 100.0f;
            
            if (enroll.getProgress() < progressionRequise) {
                System.out.println("❌ Progression insuffisante: " + enroll.getProgress() + "% < " + progressionRequise + "%");
                return false;
            }
            System.out.println("✅ Progression suffisante: " + enroll.getProgress() + "% >= " + progressionRequise + "%");

            // 3. Vérifier les quiz SEULEMENT s'ils sont obligatoires
            Boolean quizObligatoires = etape.getQuizObligatoires();
            if (quizObligatoires != null && quizObligatoires) {
                System.out.println("🎯 Quiz obligatoires - Vérification requise");
                
                Integer scoreMinimum = etape.getScoreMinimum();
                double seuilQuiz = (scoreMinimum != null && scoreMinimum > 0) ? scoreMinimum : 60.0;
                
                if (!validationService.hasPassedRequiredQuizzesWithThreshold(user, etape.getCours(), seuilQuiz)) {
                    System.out.println("❌ Quiz obligatoires non réussis");
                    return false;
                }
                System.out.println("✅ Quiz obligatoires réussis");
            } else {
                System.out.println("ℹ️ Quiz non obligatoires - Validation basée uniquement sur la progression");
            }

            System.out.println("✅ Étape " + etape.getOrdreEtape() + " validée");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur validation étape " + etape.getOrdreEtape() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Attribue les récompenses de completion (version simplifiée)
     */
    private void awardCompletionRewards(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            System.out.println("🎯 Attribution récompenses - Parcours: " + parcours.getTitre());

            // 1. Calculer les XP
            Integer xpToAward = parcours.getPointsBonus();
            if (xpToAward == null || xpToAward <= 0) {
                xpToAward = 100; // XP par défaut
            }
            inscription.setPointsGagnes(xpToAward);

            // 2. Synchroniser les XP avec gestion d'erreur robuste
            try {
                xpSynchronizationService.synchronizeXPAfterParcoursCompletion(user, parcours, xpToAward);
                System.out.println("✅ XP synchronisés: +" + xpToAward);
            } catch (Exception e) {
                System.err.println("⚠️ Erreur synchronisation XP (non bloquante): " + e.getMessage());
                // Continuer même si la synchronisation échoue
            }

            // 3. Créer la notification avec gestion d'erreur
            try {
                ParcoursNotification notification = new ParcoursNotification(
                    user, parcours, ParcoursNotification.NotificationType.PARCOURS_COMPLETED,
                    "Parcours Terminé: " + parcours.getTitre(),
                    String.format("Félicitations ! Vous avez terminé le parcours \"%s\" et gagné %d points XP !", 
                                parcours.getTitre(), xpToAward),
                    xpToAward
                );
                
                parcoursNotificationRepository.save(notification);
                System.out.println("📢 Notification créée");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur création notification (non bloquante): " + e.getMessage());
            }

            System.out.println("✅ Récompenses attribuées avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur attribution récompenses: " + e.getMessage());
            // Ne pas faire échouer la completion du parcours
        }
    }

    /**
     * Vérifie et force la completion de tous les parcours d'un utilisateur
     */
    public void checkAllUserParcours(User user) {
        try {
            List<ParcoursInscription> inscriptions = inscriptionRepository.findByUser(user);
            System.out.println("🔍 Vérification de " + inscriptions.size() + " parcours pour " + user.getEmail());

            for (ParcoursInscription inscription : inscriptions) {
                if (!inscription.getIsCompleted()) {
                    forceCheckParcoursCompletion(user, inscription.getParcours());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification des parcours: " + e.getMessage());
        }
    }
}