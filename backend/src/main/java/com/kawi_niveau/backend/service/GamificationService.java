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

    // Gestion des XP
    public void awardXP(User user, Integer xpAmount, String reason) {
        UserXP userXP = getUserXP(user);
        userXP.setTotalXP(userXP.getTotalXP() + xpAmount);
        
        // Vérifier si l'utilisateur monte de niveau
        checkLevelUp(userXP);
        
        userXPRepository.save(userXP);
        
        // Vérifier les badges liés aux XP
        checkXPBadges(user, userXP.getTotalXP());
    }

    public UserXP getUserXP(User user) {
        return userXPRepository.findByUser(user)
                .orElseGet(() -> createInitialUserXP(user));
    }

    private UserXP createInitialUserXP(User user) {
        UserXP userXP = new UserXP();
        userXP.setUser(user);
        userXP.setTotalXP(0);
        userXP.setCurrentLevel(1);
        userXP.setXpToNextLevel(100);
        return userXPRepository.save(userXP);
    }

    private void checkLevelUp(UserXP userXP) {
        List<Level> availableLevels = levelRepository.findLevelsForXP(userXP.getTotalXP());
        if (!availableLevels.isEmpty()) {
            Level currentLevel = availableLevels.get(0);
            if (currentLevel.getLevel() > userXP.getCurrentLevel()) {
                userXP.setCurrentLevel(currentLevel.getLevel());
                
                // Calculer XP pour le prochain niveau
                Optional<Level> nextLevel = levelRepository.findNextLevel(userXP.getTotalXP());
                if (nextLevel.isPresent()) {
                    userXP.setXpToNextLevel(nextLevel.get().getXpRequired() - userXP.getTotalXP());
                } else {
                    userXP.setXpToNextLevel(0); // Niveau maximum atteint
                }
                
                // Vérifier les badges de niveau
                checkLevelBadges(userXP.getUser(), currentLevel.getLevel());
            }
        }
    }

    // Gestion des badges
    public void awardBadge(User user, Badge badge) {
        if (!userBadgeRepository.existsByUserAndBadge(user, badge)) {
            UserBadge userBadge = new UserBadge();
            userBadge.setUser(user);
            userBadge.setBadge(badge);
            userBadgeRepository.save(userBadge);
        }
    }

    public void checkBadgeEligibility(User user, BadgeCriteriaType criteriaType, Integer currentValue) {
        List<Badge> eligibleBadges = badgeRepository.findByCriteriaTypeAndIsActiveTrue(criteriaType);
        
        for (Badge badge : eligibleBadges) {
            if (currentValue >= badge.getCriteriaValue()) {
                awardBadge(user, badge);
            }
        }
    }

    private void checkXPBadges(User user, Integer totalXP) {
        checkBadgeEligibility(user, BadgeCriteriaType.XP_EARNED, totalXP);
    }

    private void checkLevelBadges(User user, Integer level) {
        checkBadgeEligibility(user, BadgeCriteriaType.LEVEL_REACHED, level);
    }

    // Méthodes pour les événements d'apprentissage
    public void onCourseCompleted(User user) {
        // Attribuer des XP pour terminer un cours
        awardXP(user, 50, "Cours terminé");
        
        // Vérifier les badges de cours
        long completedCourses = getCompletedCoursesCount(user);
        checkBadgeEligibility(user, BadgeCriteriaType.COURS_COMPLETED, (int) completedCourses);
    }

    public void onQuizPassed(User user, double score) {
        // Attribuer des XP basés sur le score
        int xpAmount = (int) (score * 20); // 20 XP pour un score parfait
        awardXP(user, xpAmount, "Quiz réussi");
        
        // Badge pour score parfait
        if (score >= 100.0) {
            checkBadgeEligibility(user, BadgeCriteriaType.PERFECT_SCORE, 1);
        }
        
        // Vérifier les badges de quiz
        long passedQuizzes = getPassedQuizzesCount(user);
        checkBadgeEligibility(user, BadgeCriteriaType.QUIZ_PASSED, (int) passedQuizzes);
    }

    public void onChallengeCompleted(User user, Challenge challenge) {
        // Attribuer les XP du défi
        awardXP(user, challenge.getXpReward(), "Défi terminé: " + challenge.getName());
        
        // Vérifier les badges de défi
        long completedChallenges = userChallengeRepository.countCompletedChallengesByUser(user);
        checkBadgeEligibility(user, BadgeCriteriaType.CHALLENGE_COMPLETED, (int) completedChallenges);
    }

    // Méthodes utilitaires
    private long getCompletedCoursesCount(User user) {
        return enrollmentRepository.countByUserAndProgressGreaterThanEqual(user, 100.0f);
    }

    private long getPassedQuizzesCount(User user) {
        return resultatQuizRepository.countByUserAndScoreGreaterThanEqual(user, 50.0);
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