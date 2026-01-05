package com.kawi_niveau.backend.listener;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.event.CourseCompletedEvent;
import com.kawi_niveau.backend.event.QuizCompletedEvent;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.service.GamificationService;
import com.kawi_niveau.backend.service.ParcoursNotificationService;
import com.kawi_niveau.backend.service.XPSynchronizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ParcoursProgressionListener {

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ParcoursNotificationService notificationService;

    @Autowired
    private ParcoursNotificationRepository parcoursNotificationRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private XPSynchronizationService xpSynchronizationService;

    /**
     * Écoute les événements de completion de cours pour mettre à jour les parcours
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onCourseCompleted(CourseCompletedEvent event) {
        try {
            System.out.println("🎯 ÉVÉNEMENT REÇU: Cours terminé - " + event.getCours().getTitre() + " par " + event.getUser().getEmail());
            System.out.println("📊 Progression finale: " + event.getFinalProgress() + "%");
            
            // Vérifier que le cours est vraiment terminé (100%)
            if (event.getFinalProgress() >= 100.0f) {
                updateParcoursProgressionForUser(event.getUser(), event.getCours());
                System.out.println("✅ Mise à jour parcours terminée avec succès");
            } else {
                System.out.println("⚠️ Cours pas complètement terminé (" + event.getFinalProgress() + "%), pas de mise à jour parcours");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de progression parcours: " + e.getMessage());
            e.printStackTrace();
            // Ne pas propager l'exception pour éviter le rollback de la transaction principale
        }
    }

    /**
     * Écoute les événements de completion de quiz pour mettre à jour les parcours
     */
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {})
    public void onQuizCompleted(QuizCompletedEvent event) {
        System.out.println("🚨 ÉVÉNEMENT REÇU: Quiz terminé");
        System.out.println("🚨 User: " + event.getUser().getEmail());
        System.out.println("🚨 Quiz: " + event.getQuiz().getTitre());
        System.out.println("🚨 Score: " + event.getScore() + "%");
        System.out.println("🚨 Cours associé: " + event.getQuiz().getModule().getCours().getTitre());
        
        try {
            System.out.println("🎯 Début traitement événement quiz...");
            updateParcoursProgressionForUser(event.getUser(), event.getQuiz().getModule().getCours());
            System.out.println("✅ LISTENER QUIZ TERMINÉ AVEC SUCCÈS");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de progression parcours (quiz): " + e.getMessage());
            e.printStackTrace();
            // Ne pas propager l'exception pour éviter le rollback de la transaction principale
        }
    }

    /**
     * Met à jour la progression de tous les parcours d'un utilisateur pour un cours donné
     */
    private void updateParcoursProgressionForUser(User user, Cours cours) {
        System.out.println("🔍 Recherche des parcours contenant le cours: " + cours.getTitre() + " pour " + user.getEmail());
        
        // Trouver toutes les inscriptions aux parcours qui contiennent ce cours
        List<ParcoursEtape> etapesAvecCeCours = etapeRepository.findByCours(cours);
        System.out.println("📋 Nombre d'étapes trouvées avec ce cours: " + etapesAvecCeCours.size());
        
        for (ParcoursEtape etape : etapesAvecCeCours) {
            ParcoursApprentissage parcours = etape.getParcours();
            System.out.println("🔍 Parcours trouvé: " + parcours.getTitre() + " (Étape " + etape.getOrdreEtape() + ")");
            
            // Vérifier si l'utilisateur est inscrit à ce parcours
            Optional<ParcoursInscription> inscriptionOpt = inscriptionRepository.findByParcoursAndUser(parcours, user);
            if (inscriptionOpt.isPresent()) {
                System.out.println("✅ Utilisateur inscrit au parcours: " + parcours.getTitre());
                updateSingleParcoursProgression(inscriptionOpt.get(), parcours, user);
            } else {
                System.out.println("❌ Utilisateur non inscrit au parcours: " + parcours.getTitre());
            }
        }
        
        if (etapesAvecCeCours.isEmpty()) {
            System.out.println("⚠️ Aucun parcours ne contient le cours: " + cours.getTitre());
        }
    }

    /**
     * Met à jour la progression d'un parcours spécifique pour un utilisateur
     */
    private void updateSingleParcoursProgression(ParcoursInscription inscription, ParcoursApprentissage parcours, User user) {
        try {
            System.out.println("🔄 Mise à jour progression parcours: " + parcours.getTitre() + " pour " + user.getEmail());
            
            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            System.out.println("📋 Nombre d'étapes dans le parcours: " + etapes.size());
            
            int etapesCompletes = 0;
            int etapeCourante = 1; // Commencer à 1 par défaut
            boolean parcoursComplete = true;
            
            // Vérifier chaque étape
            for (ParcoursEtape etape : etapes) {
                System.out.println("🔍 Vérification étape " + etape.getOrdreEtape() + ": " + etape.getCours().getTitre());
                boolean isComplete = isEtapeComplete(etape, user);
                
                if (isComplete) {
                    etapesCompletes++;
                    System.out.println("✅ Étape " + etape.getOrdreEtape() + " complète");
                } else {
                    parcoursComplete = false;
                    // Si c'est la première étape non complète et qu'on n'a pas encore défini l'étape courante
                    if (etapeCourante == 1 && etapesCompletes == 0) {
                        // Première étape non complète = étape courante
                        etapeCourante = etape.getOrdreEtape();
                    } else if (etapesCompletes > 0 && etapeCourante == 1) {
                        // Il y a des étapes complètes, cette étape non complète devient l'étape courante
                        etapeCourante = etape.getOrdreEtape();
                    }
                    System.out.println("❌ Étape " + etape.getOrdreEtape() + " incomplète - Étape courante: " + etapeCourante);
                }
            }
            
            // Si toutes les étapes sont complètes, l'étape courante est la dernière
            if (parcoursComplete && !etapes.isEmpty()) {
                etapeCourante = etapes.get(etapes.size() - 1).getOrdreEtape();
                System.out.println("🎉 Toutes les étapes complètes - Étape courante: " + etapeCourante);
            }
            
            // Sécurité : s'assurer que l'étape courante est valide
            if (etapeCourante < 1 && !etapes.isEmpty()) {
                etapeCourante = etapes.get(0).getOrdreEtape(); // Première étape par défaut
                System.out.println("🔧 Correction étape courante: " + etapeCourante);
            }
            
            System.out.println("📊 Résultat final: " + etapesCompletes + "/" + etapes.size() + " étapes complètes, étape courante: " + etapeCourante);
            
            // Calculer le pourcentage de progression
            int progressionPourcentage = etapes.isEmpty() ? 0 : (etapesCompletes * 100) / etapes.size();
            System.out.println("📊 Calcul progression: " + etapesCompletes + "/" + etapes.size() + " = " + progressionPourcentage + "%");
            
            // Mettre à jour l'inscription
            boolean wasCompleted = inscription.getIsCompleted();
            inscription.setProgressionPourcentage(progressionPourcentage);
            inscription.setEtapeCourante(etapeCourante);
            inscription.setIsCompleted(parcoursComplete);
            
            // Si le parcours vient d'être terminé
            if (parcoursComplete && !wasCompleted) {
                inscription.setDateCompletion(LocalDateTime.now());
                
                // Vérifier qu'il n'y a pas déjà une notification de completion pour éviter les doublons
                boolean notificationExists = parcoursNotificationRepository
                    .existsByUserAndParcoursAndType(user, parcours, 
                        ParcoursNotification.NotificationType.PARCOURS_COMPLETED);
                
                if (!notificationExists) {
                    // Attribuer les récompenses
                    awardParcoursCompletionRewards(inscription, user, parcours);
                    System.out.println("🎉 Parcours terminé: " + parcours.getTitre() + " par " + user.getEmail());
                } else {
                    System.out.println("⚠️ Notification de completion déjà existante pour " + parcours.getTitre() + " - " + user.getEmail());
                }
            }
            
            inscriptionRepository.save(inscription);
            
            System.out.println("📊 Progression mise à jour: " + parcours.getTitre() + " - " + progressionPourcentage + "% (" + etapesCompletes + "/" + etapes.size() + " étapes)");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du parcours " + parcours.getTitre() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide si une étape est complète pour un utilisateur (version robuste)
     */
    private boolean isEtapeComplete(ParcoursEtape etape, User user) {
        try {
            Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
            if (enrollment.isEmpty()) {
                System.out.println("❌ Pas d'inscription au cours " + etape.getCours().getTitre() + " pour " + user.getEmail());
                return false;
            }

            Enrollment enroll = enrollment.get();
            System.out.println("🔍 Validation étape " + etape.getOrdreEtape() + " - Cours: " + etape.getCours().getTitre() + " - Progression: " + enroll.getProgress() + "%");
            
            // Afficher toutes les conditions de l'étape pour debug
            System.out.println("📋 Conditions étape " + etape.getOrdreEtape() + ":");
            System.out.println("   - Score minimum: " + (etape.getScoreMinimum() != null ? etape.getScoreMinimum() : "NULL"));
            System.out.println("   - Completion requise: " + (etape.getPourcentageCompletionRequis() != null ? etape.getPourcentageCompletionRequis() : "NULL"));
            System.out.println("   - Quiz obligatoires: " + (etape.getQuizObligatoires() != null ? etape.getQuizObligatoires() : "NULL"));
            System.out.println("   - Étape obligatoire: " + (etape.getIsObligatoire() != null ? etape.getIsObligatoire() : "NULL"));
            
            // Vérifier le pourcentage de completion requis (gérer les valeurs NULL)
            Integer completionRequis = etape.getPourcentageCompletionRequis();
            float progressionRequise = (completionRequis != null && completionRequis > 0) ? completionRequis : 100.0f;
            
            if (enroll.getProgress() < progressionRequise) {
                System.out.println("❌ Progression insuffisante: " + enroll.getProgress() + "% < " + progressionRequise + "%");
                return false;
            }
            System.out.println("✅ Progression suffisante: " + enroll.getProgress() + "% >= " + progressionRequise + "%");

            // Vérifier les quiz SEULEMENT s'ils sont obligatoires
            Boolean quizObligatoires = etape.getQuizObligatoires();
            if (quizObligatoires != null && quizObligatoires) {
                System.out.println("🎯 Quiz obligatoires activés - Vérification requise");
                
                List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                if (quizzes.isEmpty()) {
                    System.out.println("⚠️ Quiz obligatoires requis mais aucun quiz dans le cours " + etape.getCours().getTitre());
                    // Si pas de quiz dans le cours, on considère la condition comme remplie
                } else {
                    Integer scoreMinimum = etape.getScoreMinimum();
                    double seuilQuiz = (scoreMinimum != null && scoreMinimum > 0) ? scoreMinimum : 60.0;
                    
                    if (!hasPassedRequiredQuizzes(user, etape.getCours(), (int)seuilQuiz)) {
                        System.out.println("❌ Quiz obligatoires non réussis avec seuil " + seuilQuiz + "%");
                        return false;
                    }
                    System.out.println("✅ Quiz obligatoires réussis avec seuil " + seuilQuiz + "%");
                }
            } else {
                System.out.println("ℹ️ Quiz non obligatoires - Validation basée uniquement sur la progression du cours");
                Integer scoreMinimum = etape.getScoreMinimum();
                if (scoreMinimum != null && scoreMinimum > 0) {
                    System.out.println("⚠️ Score minimum (" + scoreMinimum + "%) défini mais quiz non obligatoires - IGNORÉ");
                }
            }

            System.out.println("✅ Étape " + etape.getOrdreEtape() + " validée pour " + user.getEmail());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la validation de l'étape " + etape.getOrdreEtape() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère le meilleur score obtenu dans les quiz d'un cours
     */
    private double getMeilleurScoreQuizCours(User user, Cours cours) {
        try {
            List<Quiz> quizzes = quizRepository.findByCours(cours);
            double meilleurScore = 0.0;

            for (Quiz quiz : quizzes) {
                Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                        .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                if (meilleurResultat.isPresent()) {
                    meilleurScore = Math.max(meilleurScore, meilleurResultat.get().getScore());
                }
            }

            return meilleurScore;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du score: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Vérifie si l'utilisateur a réussi tous les quiz obligatoires d'un cours
     */
    private boolean hasPassedRequiredQuizzes(User user, Cours cours) {
        return hasPassedRequiredQuizzes(user, cours, 60); // Score par défaut 60%
    }

    /**
     * Vérifie si l'utilisateur a réussi tous les quiz obligatoires d'un cours avec un score minimum
     */
    private boolean hasPassedRequiredQuizzes(User user, Cours cours, Integer scoreMinimum) {
        try {
            List<Quiz> quizzes = quizRepository.findByCours(cours);
            
            // Utiliser le score minimum fourni, ou 60% par défaut
            double scoreRequis = (scoreMinimum != null && scoreMinimum > 0) ? scoreMinimum : 60.0;
            
            for (Quiz quiz : quizzes) {
                Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                        .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                
                if (meilleurResultat.isEmpty() || meilleurResultat.get().getScore() < scoreRequis) {
                    System.out.println("❌ Quiz '" + quiz.getTitre() + "' non réussi - Score requis: " + scoreRequis + "%");
                    return false;
                }
                System.out.println("✅ Quiz '" + quiz.getTitre() + "' réussi - Score: " + meilleurResultat.get().getScore() + "%");
            }

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification des quiz: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attribue les récompenses de completion d'un parcours (XP + Badges + Certificats)
     */
    private void awardParcoursCompletionRewards(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            System.out.println("🎯 Attribution des récompenses de parcours pour: " + user.getEmail());
            System.out.println("📋 Parcours: " + parcours.getTitre());
            
            // 1. Calculer les XP à attribuer
            Integer xpToAward = parcours.getPointsBonus();
            if (xpToAward == null || xpToAward <= 0) {
                xpToAward = 100; // XP par défaut
                System.out.println("💰 Utilisation XP par défaut: " + xpToAward);
            } else {
                System.out.println("💰 XP du parcours: " + xpToAward);
            }

            // 2. Enregistrer les points dans l'inscription
            inscription.setPointsGagnes(xpToAward);
            System.out.println("📝 Points enregistrés dans l'inscription: " + xpToAward);

            // 3. Synchroniser les XP avec le système global
            try {
                xpSynchronizationService.synchronizeXPAfterParcoursCompletion(user, parcours, xpToAward);
                System.out.println("✅ Synchronisation XP terminée");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur synchronisation XP (non bloquante): " + e.getMessage());
            }

            // 4. Générer le certificat si activé
            boolean certificateGenerated = false;
            String certificateUrl = null;
            
            if (parcours.getCertificatEnabled() != null && parcours.getCertificatEnabled()) {
                try {
                    certificateUrl = generateSimpleCertificate(inscription, user, parcours);
                    certificateGenerated = (certificateUrl != null);
                    System.out.println("📜 Certificat généré: " + certificateUrl);
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur génération certificat (non bloquante): " + e.getMessage());
                }
            }

            // 6. Créer la notification de completion
            try {
                createNotificationSafely(user, parcours, xpToAward, certificateGenerated, certificateUrl);
                System.out.println("📢 Notification créée");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur création notification (non bloquante): " + e.getMessage());
            }

            System.out.println("✅ Toutes les récompenses de parcours ont été traitées");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'attribution des récompenses: " + e.getMessage());
            e.printStackTrace();
            // Ne pas propager l'exception pour éviter le rollback
        }
    }

    /**
     * Génère un certificat de completion (version simplifiée)
     */
    private String generateSimpleCertificate(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            // Générer une URL simple pour le certificat
            String certificatUrl = "/certificates/parcours-" + parcours.getId() + "-user-" + user.getId() + ".pdf";
            
            inscription.setCertificatGenere(true);
            inscription.setCertificatUrl(certificatUrl);
            
            System.out.println("📜 Certificat généré: " + certificatUrl + " pour " + user.getEmail());
            
            return certificatUrl;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du certificat: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Créer une notification de manière sécurisée (sans faire échouer la transaction principale)
     */
    private void createNotificationSafely(User user, ParcoursApprentissage parcours, Integer xpEarned, 
                                         boolean certificateGenerated, String certificateUrl) {
        try {
            System.out.println("📢 Création notification pour: " + user.getEmail() + " - Parcours: " + parcours.getTitre());
            
            // Créer la notification avec des titres sans emojis
            String title = "Parcours Terminé: " + parcours.getTitre();
            String message = String.format(
                "Félicitations ! Vous avez terminé le parcours \"%s\" et gagné %d points XP !",
                parcours.getTitre(),
                xpEarned != null ? xpEarned : 0
            );

            if (certificateGenerated) {
                message += " Votre certificat est prêt à être téléchargé !";
            }

            ParcoursNotification notification = new ParcoursNotification(
                user, parcours, ParcoursNotification.NotificationType.PARCOURS_COMPLETED,
                title, message, xpEarned
            );

            notification.setCertificateReady(certificateGenerated);
            notification.setCertificateUrl(certificateUrl);

            // Sauvegarder directement sans passer par le service pour éviter les problèmes de transaction
            ParcoursNotification savedNotification = parcoursNotificationRepository.save(notification);

            System.out.println("✅ Notification créée avec succès - ID: " + savedNotification.getId());
            System.out.println("📧 Titre: " + title);
            System.out.println("💰 XP: " + xpEarned);

        } catch (Exception e) {
            System.err.println("❌ Erreur création notification sécurisée: " + e.getMessage());
            e.printStackTrace();
            // Ne pas propager l'exception
        }
    }
}