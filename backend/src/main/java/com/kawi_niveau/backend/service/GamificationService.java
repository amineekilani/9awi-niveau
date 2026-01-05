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
public class GamificationService {

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    @Autowired
    private UserLoginRepository userLoginRepository;

    // Gestion des XP avec protection contre les erreurs
    public void awardXP(User user, Integer xpAmount, String reason) {
        try {
            UserXP userXP = getUserXP(user);
            userXP.setTotalXP(userXP.getTotalXP() + xpAmount);

            // Vérifier si l'utilisateur monte de niveau
            checkLevelUp(userXP);

            userXPRepository.save(userXP);

            // Vérifier les badges liés aux XP
            checkXPBadges(user, userXP.getTotalXP());

            System.out.println("Gamification: +" + xpAmount + " XP pour " + user.getEmail() + " (" + reason + ")");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'attribution d'XP pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            // Ne pas faire échouer l'opération principale
        }
    }

    public UserXP getUserXP(User user) {
        try {
            if (user == null || user.getId() == null) {
                System.err.println("Erreur: Tentative de récupération XP pour un utilisateur null ou sans ID");
                throw new IllegalArgumentException("User ou User ID ne peut pas être null");
            }
            List<UserXP> userXPs = userXPRepository.findByUserId(user.getId());
            if (userXPs.isEmpty()) {
                return createInitialUserXP(user);
            }
            // Retourner le premier élément (le plus élevé en XP grâce au tri DESC)
            return userXPs.get(0);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération UserXP pour " + user.getEmail() + ": " + e.getMessage());
            // Créer un nouveau profil XP en cas d'erreur
            return createInitialUserXP(user);
        }
    }

    private UserXP createInitialUserXP(User user) {
        try {
            UserXP userXP = new UserXP();
            userXP.setUser(user);
            userXP.setTotalXP(0);
            userXP.setCurrentLevel(1);
            userXP.setXpToNextLevel(100);
            userXP.setLastUpdated(System.currentTimeMillis());
            return userXPRepository.save(userXP);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de la création UserXP initial pour " + user.getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Impossible de créer le profil XP", e);
        }
    }

    private void checkLevelUp(UserXP userXP) {
        try {
            List<Level> availableLevels = levelRepository.findLevelsForXP(userXP.getTotalXP());
            if (!availableLevels.isEmpty()) {
                Level currentLevel = availableLevels.get(0);
                if (currentLevel.getLevel() > userXP.getCurrentLevel()) {
                    int oldLevel = userXP.getCurrentLevel();
                    userXP.setCurrentLevel(currentLevel.getLevel());

                    // Calculer XP pour le prochain niveau avec gestion d'erreur
                    try {
                        Optional<Level> nextLevel = levelRepository.findNextLevel(userXP.getTotalXP());
                        if (nextLevel.isPresent()) {
                            userXP.setXpToNextLevel(nextLevel.get().getXpRequired() - userXP.getTotalXP());
                        } else {
                            userXP.setXpToNextLevel(0); // Niveau maximum atteint
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Erreur lors de la recherche du niveau suivant: " + e.getMessage());
                        // Calculer manuellement le prochain niveau
                        List<Level> allLevels = levelRepository.findAllOrderByLevel();
                        Level nextLevel = allLevels.stream()
                            .filter(l -> l.getXpRequired() > userXP.getTotalXP())
                            .findFirst()
                            .orElse(null);
                        
                        if (nextLevel != null) {
                            userXP.setXpToNextLevel(nextLevel.getXpRequired() - userXP.getTotalXP());
                        } else {
                            userXP.setXpToNextLevel(0); // Niveau maximum atteint
                        }
                    }

                    userXP.setLastUpdated(System.currentTimeMillis());

                    System.out.println("Gamification: " + userXP.getUser().getEmail() + " monte du niveau " + oldLevel
                            + " au niveau " + currentLevel.getLevel());

                    // Vérifier les badges de niveau
                    checkLevelBadges(userXP.getUser(), currentLevel.getLevel());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de montée de niveau: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Gestion des badges avec protection contre les erreurs
    public void awardBadge(User user, Badge badge) {
        try {
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUser(user);
                userBadge.setBadge(badge);
                userBadge.setEarnedAt(System.currentTimeMillis());
                userBadgeRepository.save(userBadge);

                System.out.println("Gamification: Badge '" + badge.getName() + "' attribué à " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'attribution du badge '" + badge.getName() + "' à " + user.getEmail()
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkBadgeEligibility(User user, BadgeCriteriaType criteriaType, Integer currentValue) {
        try {
            List<Badge> eligibleBadges = badgeRepository.findByCriteriaTypeAndIsActiveTrue(criteriaType);

            for (Badge badge : eligibleBadges) {
                if (currentValue >= badge.getCriteriaValue()) {
                    awardBadge(user, badge);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des badges " + criteriaType + " pour " + user.getEmail()
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkXPBadges(User user, Integer totalXP) {
        checkBadgeEligibility(user, BadgeCriteriaType.XP_EARNED, totalXP);
    }

    private void checkLevelBadges(User user, Integer level) {
        checkBadgeEligibility(user, BadgeCriteriaType.LEVEL_REACHED, level);
    }

    // Méthodes pour les événements d'apprentissage simplifiées (XP seulement)
    public void onCourseCompleted(User user) {
        try {
            // Attribuer des XP pour terminer un cours
            awardXP(user, 50, "Cours terminé");

            System.out.println("Gamification: Cours terminé pour " + user.getEmail());
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de cours terminé pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onQuizPassed(User user, double score) {
        try {
            // Attribuer des XP basés sur le score (minimum 10 XP, maximum 20 XP)
            int xpAmount = Math.max(10, (int) (score / 5)); // Score 50% = 10 XP, Score 100% = 20 XP
            awardXP(user, xpAmount, "Quiz réussi (Score: " + score + "%)");

            // Badge pour score parfait
            if (score >= 100.0) {
                checkBadgeEligibility(user, BadgeCriteriaType.PERFECT_SCORE, 1);
            }

            // Vérifier les badges de quiz
            long passedQuizzes = getPassedQuizzesCount(user);
            checkBadgeEligibility(user, BadgeCriteriaType.QUIZ_PASSED, (int) passedQuizzes);

            // Badge pour le premier quiz
            if (passedQuizzes == 1) {
                checkBadgeEligibility(user, BadgeCriteriaType.FIRST_QUIZ, 1);
            }

            // Vérifier les défis automatiquement
            checkAllActiveChallenges(user);

            System.out.println("Gamification: Quiz réussi pour " + user.getEmail() + " - Score: " + score + "% (+"
                    + xpAmount + " XP, Total quiz: " + passedQuizzes + ")");
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de quiz réussi pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onParcoursCompleted(User user, String parcoursTitle, Integer bonusXP) {
        try {
            if (bonusXP != null && bonusXP > 0) {
                awardXP(user, bonusXP, "Parcours terminé: " + parcoursTitle);
                System.out.println("Gamification: Parcours '" + parcoursTitle + "' terminé pour " + user.getEmail()
                        + " (+" + bonusXP + " XP)");
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de parcours terminé pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes utilitaires simplifiées
    private long getCompletedCoursesCount(User user) {
        try {
            return enrollmentRepository.countByUserIdAndProgressGreaterThanEqual(user.getId(), 100.0f);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du comptage des cours terminés pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getPassedQuizzesCount(User user) {
        try {
            return resultatQuizRepository.countByUserIdAndScoreGreaterThanEqual(user.getId(), 50.0);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du comptage des quiz réussis pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    // Méthodes publiques pour obtenir les statistiques simplifiées
    public long getTotalXPAwarded() {
        Long total = userXPRepository.findTotalXPAwarded();
        return total != null ? total : 0L;
    }

    public double getAverageUserXP() {
        Double average = userXPRepository.getAverageXP();
        return average != null ? average : 0.0;
    }

    public long getTotalUsersWithXP() {
        return userXPRepository.count();
    }

    // Méthodes simplifiées pour compatibilité (sans fonctionnalité)
    public void onCourseEnrollment(User user) {
        try {
            // Attribuer des XP d'encouragement pour l'inscription
            awardXP(user, 5, "Inscription à un cours");
            System.out.println("Gamification: Inscription cours pour " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement d'inscription pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onLessonCompleted(User user) {
        try {
            // Attribuer des XP pour terminer une leçon
            awardXP(user, 5, "Leçon terminée");
            System.out.println("Gamification: Leçon terminée pour " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de leçon terminée pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recordLogin(User user, String ipAddress, String userAgent) {
        try {
            // Enregistrer la connexion dans user_logins si la table existe
            try {
                UserLogin login = new UserLogin();
                login.setUser(user);
                login.setIpAddress(ipAddress);
                login.setUserAgent(userAgent);
                userLoginRepository.save(login);
            } catch (Exception e) {
                System.err.println("Erreur enregistrement login (non bloquant): " + e.getMessage());
            }

            // Vérifier si c'est la première connexion pour attribuer le badge
            try {
                long loginCount = userLoginRepository.countByUser(user);
                if (loginCount <= 1) { // Première connexion
                    // Attribuer des XP de bienvenue
                    awardXP(user, 10, "Première connexion");

                    // Badge de première connexion
                    checkBadgeEligibility(user, BadgeCriteriaType.FIRST_COURSE, 1); // Réutiliser pour "Premier Pas"
                    
                    System.out.println("Gamification: Première connexion pour " + user.getEmail() + " - Badge Premier Pas attribué");
                } else {
                    // XP quotidien pour les connexions suivantes
                    awardXP(user, 1, "Connexion quotidienne");
                    System.out.println("Gamification: Connexion enregistrée pour " + user.getEmail());
                }
            } catch (Exception e) {
                System.err.println("Erreur gamification première connexion (non bloquant): " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'enregistrement de connexion pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void checkAllActiveChallenges(User user) {
        try {
            List<Challenge> activeChallenges = challengeRepository
                    .findByIsActiveTrueAndEndDateAfter(System.currentTimeMillis());

            for (Challenge challenge : activeChallenges) {
                checkChallengeProgress(user, challenge);
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de la vérification des défis pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkChallengeProgress(User user, Challenge challenge) {
        try {
            // Vérifier si l'utilisateur participe déjà à ce défi
            Optional<UserChallenge> existingChallenge = userChallengeRepository.findByUserIdAndChallengeId(user.getId(),
                    challenge.getId());
            UserChallenge userChallenge;

            if (existingChallenge.isPresent()) {
                userChallenge = existingChallenge.get();
                if (userChallenge.isCompleted()) {
                    return; // Défi déjà terminé
                }
            } else {
                // Inscrire automatiquement l'utilisateur au défi
                userChallenge = new UserChallenge();
                userChallenge.setUser(user);
                userChallenge.setChallenge(challenge);
                userChallenge.setCurrentProgress(0);
                userChallenge.setJoinedAt(System.currentTimeMillis());
            }

            // Calculer la progression selon le type de défi
            int newProgress = calculateChallengeProgress(user, challenge);
            userChallenge.setCurrentProgress(newProgress);

            // Vérifier si le défi est terminé
            if (newProgress >= challenge.getTargetValue()) {
                userChallenge.setCompleted(true);
                userChallenge.setCompletedAt(System.currentTimeMillis());
                userChallenge.setIsNew(true); // Flag for notification

                // Déclencher l'événement de défi terminé
                onChallengeCompleted(user, challenge);
            }

            userChallengeRepository.save(userChallenge);

        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du défi " + challenge.getName() + " pour "
                    + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int calculateChallengeProgress(User user, Challenge challenge) {
        try {
            switch (challenge.getChallengeType()) {
                case COMPLETE_COURSES:
                    return (int) getCompletedCoursesCount(user);

                case PASS_QUIZZES:
                    return (int) getPassedQuizzesCount(user);

                case EARN_XP:
                    UserXP userXP = getUserXP(user);
                    return userXP.getTotalXP();

                default:
                    return 0;
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du calcul de progression pour le défi " + challenge.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    public void onChallengeCompleted(User user, Challenge challenge) {
        try {
            // Attribuer les XP du défi
            awardXP(user, challenge.getXpReward(), "Défi terminé: " + challenge.getName());

            System.out.println("Gamification: Défi '" + challenge.getName() + "' terminé pour " + user.getEmail()
                    + " (+" + challenge.getXpReward() + " XP)");
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de défi terminé pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}