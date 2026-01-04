package com.kawi_niveau.backend.listener;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.event.CourseCompletedEvent;
import com.kawi_niveau.backend.event.QuizCompletedEvent;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
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
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    /**
     * Écoute les événements de completion de cours pour mettre à jour les parcours
     */
    @EventListener
    public void onCourseCompleted(CourseCompletedEvent event) {
        try {
            System.out.println("🎯 Événement: Cours terminé - " + event.getCours().getTitre() + " par " + event.getUser().getEmail());
            updateParcoursProgressionForUser(event.getUser(), event.getCours());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de progression parcours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Écoute les événements de completion de quiz pour mettre à jour les parcours
     */
    @EventListener
    public void onQuizCompleted(QuizCompletedEvent event) {
        try {
            System.out.println("🎯 Événement: Quiz terminé - Score " + event.getScore() + "% par " + event.getUser().getEmail());
            updateParcoursProgressionForUser(event.getUser(), event.getQuiz().getModule().getCours());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de progression parcours (quiz): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour la progression de tous les parcours d'un utilisateur pour un cours donné
     */
    private void updateParcoursProgressionForUser(User user, Cours cours) {
        // Trouver toutes les inscriptions aux parcours qui contiennent ce cours
        List<ParcoursEtape> etapesAvecCeCours = etapeRepository.findByCours(cours);
        
        for (ParcoursEtape etape : etapesAvecCeCours) {
            ParcoursApprentissage parcours = etape.getParcours();
            
            // Vérifier si l'utilisateur est inscrit à ce parcours
            inscriptionRepository.findByParcoursAndUser(parcours, user)
                .ifPresent(inscription -> updateSingleParcoursProgression(inscription, parcours, user));
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
            int etapeCourante = 1;
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
                    if (etapeCourante == etape.getOrdreEtape()) {
                        etapeCourante = etape.getOrdreEtape();
                    }
                    System.out.println("❌ Étape " + etape.getOrdreEtape() + " incomplète");
                }
            }
            
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
                
                // Attribuer les récompenses
                awardParcoursCompletionRewards(inscription, user, parcours);
                
                System.out.println("🎉 Parcours terminé: " + parcours.getTitre() + " par " + user.getEmail());
            }
            
            inscriptionRepository.save(inscription);
            
            System.out.println("📊 Progression mise à jour: " + parcours.getTitre() + " - " + progressionPourcentage + "% (" + etapesCompletes + "/" + etapes.size() + " étapes)");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du parcours " + parcours.getTitre() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide si une étape est complète pour un utilisateur (version améliorée)
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
            
            // Vérifier le pourcentage de completion requis
            if (etape.getPourcentageCompletionRequis() > 0) {
                float progressionRequise = etape.getPourcentageCompletionRequis().floatValue();
                if (enroll.getProgress() < progressionRequise) {
                    System.out.println("❌ Progression insuffisante: " + enroll.getProgress() + "% < " + progressionRequise + "%");
                    return false;
                }
                System.out.println("✅ Progression suffisante: " + enroll.getProgress() + "% >= " + progressionRequise + "%");
            }

            // Vérifier le score minimum requis
            if (etape.getScoreMinimum() > 0) {
                double scoreObtenu = getMeilleurScoreQuizCours(user, etape.getCours());
                if (scoreObtenu < etape.getScoreMinimum()) {
                    System.out.println("❌ Score insuffisant: " + scoreObtenu + "% < " + etape.getScoreMinimum() + "%");
                    return false;
                }
                System.out.println("✅ Score suffisant: " + scoreObtenu + "% >= " + etape.getScoreMinimum() + "%");
            }

            // Vérifier les quiz obligatoires (seulement si le cours a des quiz)
            if (etape.getQuizObligatoires()) {
                List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                if (quizzes.isEmpty()) {
                    System.out.println("⚠️ Quiz obligatoires requis mais aucun quiz dans le cours " + etape.getCours().getTitre());
                    // Si pas de quiz dans le cours, on considère la condition comme remplie
                    // car on ne peut pas exiger de réussir des quiz qui n'existent pas
                } else {
                    if (!hasPassedRequiredQuizzes(user, etape.getCours())) {
                        System.out.println("❌ Quiz obligatoires non réussis");
                        return false;
                    }
                    System.out.println("✅ Quiz obligatoires réussis");
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
        try {
            List<Quiz> quizzes = quizRepository.findByCours(cours);
            
            for (Quiz quiz : quizzes) {
                Optional<ResultatQuiz> meilleurResultat = resultatQuizRepository
                        .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                
                // Considérer qu'un quiz est réussi avec un score >= 60%
                if (meilleurResultat.isEmpty() || meilleurResultat.get().getScore() < 60.0) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification des quiz: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attribue les récompenses de completion d'un parcours
     */
    private void awardParcoursCompletionRewards(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            // 1. Attribuer les points bonus
            if (parcours.getPointsBonus() != null && parcours.getPointsBonus() > 0) {
                gamificationService.awardXP(user, parcours.getPointsBonus(), 
                    "Parcours terminé: " + parcours.getTitre());
                inscription.setPointsGagnes(parcours.getPointsBonus());
                
                System.out.println("💰 Points bonus attribués: +" + parcours.getPointsBonus() + " XP pour " + user.getEmail());
            }

            // 2. Attribuer le badge de completion personnalisé
            if (parcours.getBadgeCompletion() != null && !parcours.getBadgeCompletion().trim().isEmpty()) {
                awardCustomCompletionBadge(user, parcours);
            }

            // 3. Générer le certificat si activé (version simplifiée)
            if (parcours.getCertificatEnabled() != null && parcours.getCertificatEnabled()) {
                generateSimpleCertificate(inscription, user, parcours);
            }

            // 4. Déclencher l'événement de gamification standard
            gamificationService.onCourseCompleted(user);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'attribution des récompenses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Attribue un badge personnalisé de completion de parcours
     */
    private void awardCustomCompletionBadge(User user, ParcoursApprentissage parcours) {
        try {
            String badgeName = parcours.getBadgeCompletion();
            
            // Chercher un badge existant ou en créer un nouveau
            Badge badge = badgeRepository.findAll().stream()
                .filter(b -> badgeName.equals(b.getName()))
                .findFirst()
                .orElseGet(() -> createCustomBadge(badgeName, parcours));

            // Vérifier si l'utilisateur n'a pas déjà ce badge
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                gamificationService.awardBadge(user, badge);
                System.out.println("🏆 Badge personnalisé attribué: " + badgeName + " à " + user.getEmail());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'attribution du badge personnalisé: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée un badge personnalisé pour un parcours
     */
    private Badge createCustomBadge(String badgeName, ParcoursApprentissage parcours) {
        try {
            Badge badge = new Badge();
            badge.setName(badgeName);
            badge.setDescription("Badge obtenu en terminant le parcours: " + parcours.getTitre());
            badge.setIconUrl("🎓"); // Icône par défaut pour les parcours
            badge.setCriteriaType(BadgeCriteriaType.COURS_COMPLETED); // Type générique
            badge.setCriteriaValue(1); // Valeur par défaut
            badge.setIsActive(true);
            badge.setCreatedAt(System.currentTimeMillis());

            return badgeRepository.save(badge);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du badge personnalisé: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le badge personnalisé", e);
        }
    }

    /**
     * Génère un certificat de completion (version simplifiée)
     */
    private void generateSimpleCertificate(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            // Générer une URL simple pour le certificat
            String certificatUrl = "/certificates/parcours-" + parcours.getId() + "-user-" + user.getId() + ".pdf";
            
            inscription.setCertificatGenere(true);
            inscription.setCertificatUrl(certificatUrl);
            
            System.out.println("📜 Certificat généré: " + certificatUrl + " pour " + user.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du certificat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}