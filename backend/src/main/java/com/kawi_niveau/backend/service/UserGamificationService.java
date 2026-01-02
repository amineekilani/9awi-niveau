package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserGamificationService {

    @Autowired
    private UserXPRepository userXPRepository;
    
    @Autowired
    private LevelRepository levelRepository;
    
    @Autowired
    private UserBadgeRepository userBadgeRepository;
    
    @Autowired
    private UserChallengeRepository userChallengeRepository;
    
    @Autowired
    private ChallengeRepository challengeRepository;
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    @Autowired
    private GamificationService gamificationService;

    public UserGamificationStatsResponse getUserStats(User user) {
        try {
            // S'assurer que l'utilisateur a des données de gamification
            UserXP userXP = gamificationService.getUserXP(user);
            
            // Initialiser les défis si nécessaire
            initializeUserChallengesIfNeeded(user);
            
            // Récupérer le niveau actuel
            Optional<Level> currentLevelOpt = levelRepository.findByLevel(userXP.getCurrentLevel());
            Level currentLevel = currentLevelOpt.orElse(createDefaultLevel(userXP.getCurrentLevel()));
            
            // Calculer le niveau suivant
            Optional<Level> nextLevelOpt = levelRepository.findNextLevel(userXP.getTotalXP());
            Integer nextLevelPoints = nextLevelOpt.map(Level::getXpRequired).orElse(userXP.getTotalXP() + 100);
            Integer pointsToNextLevel = Math.max(0, nextLevelPoints - userXP.getTotalXP());
            
            // Calculer le pourcentage de progression
            Double progressPercent = 0.0;
            if (nextLevelOpt.isPresent()) {
                int currentLevelXP = currentLevel.getXpRequired();
                int totalNeeded = nextLevelPoints - currentLevelXP;
                int currentProgress = userXP.getTotalXP() - currentLevelXP;
                progressPercent = totalNeeded > 0 ? (double) currentProgress / totalNeeded * 100 : 100.0;
            }
            
            // Compter les badges
            int badgesCount = userBadgeRepository.findByUser(user).size();
            
            // Compter les défis terminés
            int completedChallenges = (int) userChallengeRepository.countCompletedChallengesByUser(user);
            
            // Position dans le classement (approximation)
            int leaderboardPosition = getApproximateLeaderboardPosition(user);
            
            // Activité récente
            List<UserGamificationStatsResponse.RecentActivityResponse> recentActivities = 
                getRecentActivity(user, 5);
            
            // Badges récents
            List<UserGamificationStatsResponse.UserBadgeResponse> recentBadges = 
                getRecentBadges(user, 3);

            UserGamificationStatsResponse response = new UserGamificationStatsResponse(
                userXP.getTotalXP(),
                userXP.getCurrentLevel(),
                currentLevel.getName(),
                currentLevel.getDescription(),
                pointsToNextLevel,
                nextLevelPoints,
                Math.min(100.0, Math.max(0.0, progressPercent)),
                badgesCount,
                completedChallenges,
                leaderboardPosition,
                recentActivities,
                recentBadges
            );
            
            System.out.println("Stats générées pour " + user.getEmail() + ": " + 
                userXP.getTotalXP() + " XP, Niveau " + userXP.getCurrentLevel() + 
                ", " + badgesCount + " badges, " + completedChallenges + " défis");
            
            return response;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des stats pour " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return createDefaultStats();
        }
    }

    public List<UserGamificationStatsResponse.UserBadgeResponse> getUserBadges(User user, String filter) {
        try {
            List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
            
            return userBadges.stream()
                .filter(ub -> filterBadge(ub, filter))
                .map(this::convertToUserBadgeResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des badges pour " + user.getEmail() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserChallengeResponse> getUserChallenges(User user) {
        try {
            List<UserChallenge> userChallenges = userChallengeRepository.findByUser(user);
            
            return userChallenges.stream()
                .map(this::convertToUserChallengeResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des défis pour " + user.getEmail() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public UserLeaderboardResponse getUserLeaderboard(User user) {
        try {
            // Position de l'utilisateur
            int userPosition = getApproximateLeaderboardPosition(user);
            UserXP userXP = gamificationService.getUserXP(user);
            Optional<Level> userLevel = levelRepository.findByLevel(userXP.getCurrentLevel());
            int userBadgesCount = userBadgeRepository.findByUser(user).size();
            
            UserLeaderboardResponse.UserPositionResponse userPos = 
                new UserLeaderboardResponse.UserPositionResponse(
                    userPosition,
                    user.getFirstName() + " " + user.getLastName(),
                    userXP.getTotalXP(),
                    userXP.getCurrentLevel(),
                    userLevel.map(Level::getName).orElse("Niveau " + userXP.getCurrentLevel()),
                    userBadgesCount
                );
            
            // Top 10 (approximation)
            List<UserLeaderboardResponse.LeaderboardEntryResponse> topLeaderboard = 
                getTopLeaderboard(user, 10);
            
            return new UserLeaderboardResponse(userPos, topLeaderboard);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du classement pour " + user.getEmail() + ": " + e.getMessage());
            return new UserLeaderboardResponse(null, new ArrayList<>());
        }
    }

    public List<UserGamificationStatsResponse.RecentActivityResponse> getRecentActivity(User user, int limit) {
        try {
            List<UserGamificationStatsResponse.RecentActivityResponse> activities = new ArrayList<>();
            
            // Récupérer les badges récents
            List<UserBadge> recentBadges = userBadgeRepository.findByUser(user).stream()
                .sorted((a, b) -> Long.compare(b.getEarnedAt(), a.getEarnedAt()))
                .limit(limit / 2)
                .collect(Collectors.toList());
            
            for (UserBadge badge : recentBadges) {
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                    "badge",
                    "Récompense \"" + badge.getBadge().getName() + "\" obtenue",
                    0,
                    formatTimeAgo(badge.getEarnedAt()),
                    "🏆"
                ));
            }
            
            // Ajouter des activités simulées pour l'exemple
            if (activities.size() < limit) {
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                    "quiz",
                    "Quiz réussi avec succès",
                    15,
                    "il y a 2h",
                    "✓"
                ));
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                    "course",
                    "Cours terminé",
                    50,
                    "il y a 1j",
                    "📚"
                ));
            }
            
            return activities.stream().limit(limit).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'activité récente pour " + user.getEmail() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    } 
   // Méthodes utilitaires privées
    
    private Level createDefaultLevel(int levelNumber) {
        Level level = new Level();
        level.setLevel(levelNumber);
        level.setName("Niveau " + levelNumber);
        level.setDescription("Niveau " + levelNumber);
        level.setXpRequired(levelNumber * 100);
        return level;
    }
    
    private UserGamificationStatsResponse createDefaultStats() {
        return new UserGamificationStatsResponse(
            0, 1, "Débutant", "Bienvenue !", 100, 100, 0.0, 0, 0, 999,
            new ArrayList<>(), new ArrayList<>()
        );
    }
    
    private int getApproximateLeaderboardPosition(User user) {
        try {
            UserXP userXP = gamificationService.getUserXP(user);
            long betterUsers = userXPRepository.findAll().stream()
                .filter(ux -> ux.getTotalXP() > userXP.getTotalXP())
                .count();
            return (int) betterUsers + 1;
        } catch (Exception e) {
            return 999;
        }
    }
    
    private List<UserGamificationStatsResponse.UserBadgeResponse> getRecentBadges(User user, int limit) {
        try {
            return userBadgeRepository.findByUser(user).stream()
                .sorted((a, b) -> Long.compare(b.getEarnedAt(), a.getEarnedAt()))
                .limit(limit)
                .map(this::convertToUserBadgeResponse)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private boolean filterBadge(UserBadge userBadge, String filter) {
        switch (filter.toLowerCase()) {
            case "recent":
                long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                return userBadge.getEarnedAt() > weekAgo;
            case "quiz":
                return userBadge.getBadge().getCriteriaType() == BadgeCriteriaType.QUIZ_PASSED ||
                       userBadge.getBadge().getCriteriaType() == BadgeCriteriaType.PERFECT_SCORE ||
                       userBadge.getBadge().getCriteriaType() == BadgeCriteriaType.FIRST_QUIZ;
            case "cours":
                return userBadge.getBadge().getCriteriaType() == BadgeCriteriaType.COURS_COMPLETED ||
                       userBadge.getBadge().getCriteriaType() == BadgeCriteriaType.FIRST_COURSE;
            default:
                return true;
        }
    }
    
    private UserGamificationStatsResponse.UserBadgeResponse convertToUserBadgeResponse(UserBadge userBadge) {
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        boolean isNew = userBadge.getEarnedAt() > weekAgo;
        
        return new UserGamificationStatsResponse.UserBadgeResponse(
            userBadge.getBadge().getId(),
            userBadge.getBadge().getName(),
            userBadge.getBadge().getDescription(),
            userBadge.getBadge().getIconUrl(),
            userBadge.getEarnedAt(),
            isNew
        );
    }
    
    private UserChallengeResponse convertToUserChallengeResponse(UserChallenge userChallenge) {
        Challenge challenge = userChallenge.getChallenge();
        double progressPercent = challenge.getTargetValue() > 0 ? 
            (double) userChallenge.getCurrentProgress() / challenge.getTargetValue() * 100 : 0;
        
        String timeRemaining = null;
        if (challenge.getEndDate() != null && challenge.getEndDate() > System.currentTimeMillis()) {
            long remaining = challenge.getEndDate() - System.currentTimeMillis();
            long days = remaining / (24 * 60 * 60 * 1000L);
            if (days > 0) {
                timeRemaining = days + " jour" + (days > 1 ? "s" : "");
            } else {
                long hours = remaining / (60 * 60 * 1000L);
                timeRemaining = hours + " heure" + (hours > 1 ? "s" : "");
            }
        }
        
        return new UserChallengeResponse(
            challenge.getId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getChallengeType().name(),
            challenge.getTargetValue(),
            userChallenge.getCurrentProgress(),
            Math.min(100.0, progressPercent),
            challenge.getXpReward(),
            userChallenge.isCompleted(),
            userChallenge.getCompletedAt(),
            userChallenge.getJoinedAt(),
            challenge.getEndDate(),
            timeRemaining,
            challenge.getIsActive()
        );
    }
    
    private List<UserLeaderboardResponse.LeaderboardEntryResponse> getTopLeaderboard(User currentUser, int limit) {
        try {
            List<UserXP> topUsers = userXPRepository.findAllOrderByTotalXPDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            List<UserLeaderboardResponse.LeaderboardEntryResponse> entries = new ArrayList<>();
            for (int i = 0; i < topUsers.size(); i++) {
                UserXP userXP = topUsers.get(i);
                User user = userXP.getUser();
                Optional<Level> level = levelRepository.findByLevel(userXP.getCurrentLevel());
                int badgesCount = userBadgeRepository.findByUser(user).size();
                
                entries.add(new UserLeaderboardResponse.LeaderboardEntryResponse(
                    i + 1,
                    user.getFirstName() + " " + user.getLastName(),
                    userXP.getTotalXP(),
                    userXP.getCurrentLevel(),
                    level.map(Level::getName).orElse("Niveau " + userXP.getCurrentLevel()),
                    badgesCount,
                    user.getId().equals(currentUser.getId())
                ));
            }
            
            return entries;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private String formatTimeAgo(Long timestamp) {
        if (timestamp == null) return "récemment";
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        long minutes = diff / (60 * 1000L);
        long hours = diff / (60 * 60 * 1000L);
        long days = diff / (24 * 60 * 60 * 1000L);
        
        if (days > 0) {
            return "il y a " + days + " jour" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return "il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return "il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return "à l'instant";
        }
    }
    
    private void initializeUserChallengesIfNeeded(User user) {
        try {
            // Vérifier si l'utilisateur a des défis
            List<UserChallenge> userChallenges = userChallengeRepository.findByUser(user);
            if (userChallenges.isEmpty()) {
                // Inscrire l'utilisateur aux défis actifs
                List<Challenge> activeChallenges = challengeRepository.findByIsActiveTrue();
                for (Challenge challenge : activeChallenges) {
                    UserChallenge userChallenge = new UserChallenge();
                    userChallenge.setUser(user);
                    userChallenge.setChallenge(challenge);
                    userChallenge.setCurrentProgress(0);
                    userChallenge.setCompleted(false);
                    userChallenge.setJoinedAt(System.currentTimeMillis());
                    userChallengeRepository.save(userChallenge);
                }
                System.out.println("Défis initialisés pour " + user.getEmail() + ": " + activeChallenges.size() + " défis");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des défis pour " + user.getEmail() + ": " + e.getMessage());
        }
    }
}