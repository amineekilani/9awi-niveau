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

                    // Calculer XP pour le prochain niveau
                    Optional<Level> nextLevel = levelRepository.findNextLevel(userXP.getTotalXP());
                    if (nextLevel.isPresent()) {
                        userXP.setXpToNextLevel(nextLevel.get().getXpRequired() - userXP.getTotalXP());
                    } else {
                        userXP.setXpToNextLevel(0); // Niveau maximum atteint
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

    // Méthodes pour les événements d'apprentissage avec protection
    public void onCourseCompleted(User user) {
        try {
            // Attribuer des XP pour terminer un cours
            awardXP(user, 50, "Cours terminé");

            // Vérifier les badges de cours
            long completedCourses = getCompletedCoursesCount(user);
            checkBadgeEligibility(user, BadgeCriteriaType.COURS_COMPLETED, (int) completedCourses);

            // Badge pour le premier cours
            if (completedCourses == 1) {
                checkBadgeEligibility(user, BadgeCriteriaType.FIRST_COURSE, 1);
            }

            // Vérifier les défis automatiquement
            checkAllActiveChallenges(user);

            System.out.println(
                    "Gamification: Cours terminé pour " + user.getEmail() + " (Total: " + completedCourses + " cours)");
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de cours terminé pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onLessonCompleted(User user) {
        try {
            // Attribuer des XP pour terminer une leçon
            awardXP(user, 5, "Leçon terminée");

            // Vérifier si c'est la première leçon
            long completedLessons = getCompletedLessonsCount(user);
            if (completedLessons == 1) {
                checkBadgeEligibility(user, BadgeCriteriaType.FIRST_COURSE, 1); // Réutiliser pour "Premier Pas"
            }

            System.out.println("Gamification: Leçon terminée pour " + user.getEmail() + " (Total: " + completedLessons
                    + " leçons)");
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de leçon terminée pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onCourseEnrollment(User user) {
        try {
            // Attribuer des XP d'encouragement pour l'inscription
            awardXP(user, 5, "Inscription à un cours");

            // Vérifier si c'est la première inscription
            long enrollments = getEnrollmentsCount(user);
            if (enrollments == 1) {
                checkBadgeEligibility(user, BadgeCriteriaType.FIRST_COURSE, 1); // Badge "Débutant Motivé"
            }

            System.out.println("Gamification: Inscription cours pour " + user.getEmail() + " (Total: " + enrollments
                    + " inscriptions)");
        } catch (Exception e) {
            System.err
                    .println("Erreur lors du traitement d'inscription pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onFirstLogin(User user) {
        try {
            // Vérifier si c'est vraiment la première connexion
            long loginCount = userLoginRepository.countByUser(user);
            if (loginCount <= 1) { // Première connexion (le login actuel est déjà enregistré)
                // Attribuer des XP de bienvenue
                awardXP(user, 10, "Première connexion");

                // Badge de bienvenue (réutiliser FIRST_COURSE pour l'instant)
                checkBadgeEligibility(user, BadgeCriteriaType.FIRST_COURSE, 1);

                System.out.println("Gamification: Première connexion pour " + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de première connexion pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onDailyLogin(User user) {
        try {
            // Vérifier les connexions quotidiennes consécutives
            long consecutiveDays = getConsecutiveLoginDays(user);

            // Badge pour 7 jours consécutifs
            if (consecutiveDays >= 7) {
                checkBadgeEligibility(user, BadgeCriteriaType.STREAK_DAYS, (int) consecutiveDays);
            }

            // XP quotidien pour encourager la régularité
            if (consecutiveDays > 1) {
                int bonusXP = Math.min((int) consecutiveDays, 10); // Max 10 XP de bonus
                awardXP(user, bonusXP, "Connexion quotidienne (Jour " + consecutiveDays + ")");
            }

            // Vérifier les défis automatiquement
            checkAllActiveChallenges(user);

            System.out.println("Gamification: Connexion quotidienne pour " + user.getEmail() + " (Streak: "
                    + consecutiveDays + " jours)");
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement de connexion quotidienne pour " + user.getEmail() + ": "
                    + e.getMessage());
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

    public void onChallengeCompleted(User user, Challenge challenge) {
        try {
            // Attribuer les XP du défi
            awardXP(user, challenge.getXpReward(), "Défi terminé: " + challenge.getName());

            // Vérifier les badges de défi
            long completedChallenges = userChallengeRepository.countCompletedChallengesByUser(user);
            checkBadgeEligibility(user, BadgeCriteriaType.CHALLENGE_COMPLETED, (int) completedChallenges);

            System.out.println("Gamification: Défi '" + challenge.getName() + "' terminé pour " + user.getEmail()
                    + " (+" + challenge.getXpReward() + " XP)");
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du traitement de défi terminé pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes utilitaires avec protection contre les erreurs
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

    private long getCompletedLessonsCount(User user) {
        try {
            // Compter toutes les leçons terminées par l'utilisateur
            // Pour l'instant placeholder, mais on peut utiliser l'ID si implémenté
            return enrollmentRepository.findByUserId(user.getId()).stream()
                    .mapToLong(enrollment -> {
                        // Cette logique sera implémentée avec LeconCompletionRepository
                        return 0; // Placeholder pour l'instant
                    })
                    .sum();
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du comptage des leçons terminées pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getEnrollmentsCount(User user) {
        try {
            return enrollmentRepository.findByUserId(user.getId()).size();
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du comptage des inscriptions pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getConsecutiveLoginDays(User user) {
        try {
            // Calculer les jours consécutifs de connexion
            long currentTime = System.currentTimeMillis();
            long oneDayMs = 24 * 60 * 60 * 1000L;

            // Compter les jours distincts de connexion dans les 30 derniers jours
            long thirtyDaysAgo = currentTime - (30 * oneDayMs);
            return userLoginRepository.countDistinctDaysByUserIdAndLoginTimeAfter(user.getId(), thirtyDaysAgo);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du calcul des jours consécutifs pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    // Méthode pour enregistrer une connexion
    public void recordLogin(User user, String ipAddress, String userAgent) {
        try {
            UserLogin login = new UserLogin();
            login.setUser(user);
            login.setIpAddress(ipAddress);
            login.setUserAgent(userAgent);
            userLoginRepository.save(login);

            // Déclencher les événements de gamification
            onFirstLogin(user);
            onDailyLogin(user);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de l'enregistrement de connexion pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Système automatique de vérification des défis
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

                case DAILY_LOGIN:
                    return (int) getConsecutiveLoginDays(user);

                case PERFECT_SCORES:
                    return (int) getPerfectScoresCount(user);

                case EARN_BADGES:
                    return (int) getEarnedBadgesCount(user);

                case COMPLETE_MODULE:
                    return (int) getCompletedModulesCount(user);

                case WEEKLY_ACTIVITY:
                    return (int) getWeeklyActivityScore(user);

                case MONTHLY_GOAL:
                    return (int) getMonthlyGoalProgress(user);

                default:
                    return 0;
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du calcul de progression pour le défi " + challenge.getName() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getPerfectScoresCount(User user) {
        try {
            return resultatQuizRepository.countByUserIdAndScoreGreaterThanEqual(user.getId(), 100.0);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du comptage des scores parfaits pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getEarnedBadgesCount(User user) {
        try {
            return userBadgeRepository.countByUserId(user.getId());
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des badges pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getCompletedModulesCount(User user) {
        try {
            // Pour l'instant, approximation basée sur les cours terminés
            // Une implémentation plus précise nécessiterait de tracker les modules
            // individuellement
            return getCompletedCoursesCount(user) * 3; // Estimation moyenne de 3 modules par cours
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des modules pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getWeeklyActivityScore(User user) {
        try {
            // Calculer l'activité de la semaine (connexions + quiz + leçons)
            long weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
            long weeklyLogins = userLoginRepository.findByUserIdAndLoginTimeAfter(user.getId(), weekStart).size();
            long weeklyQuizzes = getPassedQuizzesCount(user); // Approximation
            return (int) (weeklyLogins + weeklyQuizzes);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du calcul d'activité hebdomadaire pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    private long getMonthlyGoalProgress(User user) {
        try {
            // Calculer la progression mensuelle (XP + cours + badges)
            UserXP userXP = getUserXP(user);
            long monthlyXP = userXP.getTotalXP(); // Approximation
            long monthlyCourses = getCompletedCoursesCount(user);
            long monthlyBadges = getEarnedBadgesCount(user);
            return (int) (monthlyXP / 10 + monthlyCourses * 20 + monthlyBadges * 5); // Score composite
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors du calcul d'objectif mensuel pour " + user.getEmail() + ": " + e.getMessage());
            return 0;
        }
    }

    // Méthodes publiques pour obtenir les statistiques
    public long getTotalBadges() {
        return badgeRepository.count();
    }

    public long getActiveBadges() {
        return badgeRepository.countActiveBadges();
    }

    public long getTotalChallenges() {
        return challengeRepository.count();
    }

    public long getActiveChallenges() {
        return challengeRepository.countActiveChallenges();
    }

    public long getTotalXPAwarded() {
        Long total = userXPRepository.findTotalXPAwarded();
        return total != null ? total : 0L;
    }

    public double getAverageUserXP() {
        Double average = userXPRepository.getAverageXP();
        return average != null ? average : 0.0;
    }

    public long getTotalBadgesEarned() {
        return userBadgeRepository.countTotalBadgesEarned();
    }

    public long getTotalChallengesCompleted() {
        return userChallengeRepository.countTotalChallengesCompleted();
    }
}