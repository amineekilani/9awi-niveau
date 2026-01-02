package com.kawi_niveau.backend.config;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GamificationDataInitializer implements CommandLineRunner {

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Initialiser les niveaux s'ils n'existent pas
            if (levelRepository.count() == 0) {
                initializeLevels();
                System.out.println("✅ Niveaux de gamification initialisés");
            }

            // Initialiser les badges s'ils n'existent pas
            if (badgeRepository.count() == 0) {
                initializeBadges();
                System.out.println("✅ Badges de gamification initialisés");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation de la gamification: " + e.getMessage());
            // Ne pas faire échouer le démarrage de l'application
        }
    }

    private void initializeLevels() {
        Level[] levels = {
            createLevel(1, 0, "Débutant", "Bienvenue dans votre parcours d'apprentissage !", "Accès aux cours de base"),
            createLevel(2, 100, "Apprenti", "Vous commencez à maîtriser les bases", "Badge de progression"),
            createLevel(3, 250, "Étudiant", "Vous progressez bien dans vos études", "Accès aux quiz avancés"),
            createLevel(4, 500, "Avancé", "Vous avez acquis de solides compétences", "Certificat de niveau"),
            createLevel(5, 1000, "Expert", "Vous maîtrisez votre domaine", "Accès aux cours premium"),
            createLevel(6, 2000, "Maître", "Vous êtes un véritable expert", "Badge de maître"),
            createLevel(7, 3500, "Sage", "Votre sagesse inspire les autres", "Statut de mentor"),
            createLevel(8, 5500, "Légende", "Vous êtes une légende vivante", "Reconnaissance spéciale"),
            createLevel(9, 8000, "Champion", "Vous êtes au sommet de votre art", "Titre de champion"),
            createLevel(10, 12000, "Grand Maître", "Le niveau ultime d'excellence", "Statut de grand maître")
        };

        for (Level level : levels) {
            levelRepository.save(level);
        }
    }

    private Level createLevel(int level, int xpRequired, String name, String description, String rewardDescription) {
        Level l = new Level();
        l.setLevel(level);
        l.setXpRequired(xpRequired);
        l.setName(name);
        l.setDescription(description);
        l.setRewardDescription(rewardDescription);
        return l;
    }

    private void initializeBadges() {
        Badge[] badges = {
            createBadge("Premier Pas", "Félicitations pour votre première connexion !", "/icons/first-login.svg", BadgeCriteriaType.FIRST_COURSE, 1),
            createBadge("Étudiant Assidu", "Terminez votre premier cours", "/icons/first-course.svg", BadgeCriteriaType.COURS_COMPLETED, 1),
            createBadge("Quiz Master", "Réussissez votre premier quiz", "/icons/first-quiz.svg", BadgeCriteriaType.QUIZ_PASSED, 1),
            createBadge("Perfectionniste", "Obtenez un score parfait à un quiz", "/icons/perfect-score.svg", BadgeCriteriaType.PERFECT_SCORE, 1),
            createBadge("Marathonien", "Connectez-vous 7 jours consécutifs", "/icons/streak.svg", BadgeCriteriaType.STREAK_DAYS, 7),
            createBadge("Collectionneur", "Terminez 5 cours", "/icons/collector.svg", BadgeCriteriaType.COURS_COMPLETED, 5),
            createBadge("Expert Quiz", "Réussissez 10 quiz", "/icons/quiz-expert.svg", BadgeCriteriaType.QUIZ_PASSED, 10),
            createBadge("Montée en Niveau", "Atteignez le niveau 5", "/icons/level-up.svg", BadgeCriteriaType.LEVEL_REACHED, 5),
            createBadge("Chasseur de Points", "Gagnez 1000 points XP", "/icons/xp-hunter.svg", BadgeCriteriaType.XP_EARNED, 1000),
            createBadge("Défi Relevé", "Terminez votre premier défi", "/icons/challenge.svg", BadgeCriteriaType.CHALLENGE_COMPLETED, 1)
        };

        for (Badge badge : badges) {
            badgeRepository.save(badge);
        }
    }

    private Badge createBadge(String name, String description, String iconUrl, BadgeCriteriaType criteriaType, int criteriaValue) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);
        badge.setIconUrl(iconUrl);
        badge.setCriteriaType(criteriaType);
        badge.setCriteriaValue(criteriaValue);
        badge.setIsActive(true);
        return badge;
    }
}