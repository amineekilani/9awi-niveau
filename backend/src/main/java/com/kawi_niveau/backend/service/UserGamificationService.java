package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.Badge;
import com.kawi_niveau.backend.entity.BadgeCriteriaType;
import com.kawi_niveau.backend.entity.Challenge;
import com.kawi_niveau.backend.entity.Level;
import com.kawi_niveau.backend.entity.ParcoursNotification;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserBadge;
import com.kawi_niveau.backend.entity.UserChallenge;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map; // Added import for Map
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserGamificationService {

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ParcoursNotificationRepository parcoursNotificationRepository;

    /**
     * Méthode pour invalider le cache (non utilisée actuellement mais prête si besoin)
     */
    // @CacheEvict(value = "userStats", key = "#user.email")
    public void invalidateUserStatsCache(User user) {
        // Cette méthode pourrait vider le cache si on réactive le cache plus tard
    }

    // Option 1 : Pas de cache pour garantir des données toujours fraîches (important pour la gamification)
    // @Cacheable(value = "userStats", key = "#user.email", unless = "#result == null")
    public UserGamificationStatsResponse getUserStats(User user) {
        try {
            // System.out.println("========================================");
            // System.out.println("getUserStats appelé pour: " + user.getEmail());

            // S'assurer que l'utilisateur a des données de gamification
            UserXP userXP = gamificationService.getUserXP(user);
            // System.out.println("UserXP récupéré: " + userXP.getTotalXP() + " XP, Niveau " + userXP.getCurrentLevel());

            // Vérification défensive des valeurs nulles
            int currentLevelVal = userXP.getCurrentLevel() != null ? userXP.getCurrentLevel() : 1;
            int totalXpVal = userXP.getTotalXP() != null ? userXP.getTotalXP() : 0;
            // System.out.println("Valeurs après vérification: " + totalXpVal + " XP, Niveau " + currentLevelVal);

            // Compter les badges
            int badgesCount = (int) userBadgeRepository.countByUserId(user.getId());
            // System.out.println("Badges count: " + badgesCount);

            // Compter les défis terminés
            int completedChallenges = (int) userChallengeRepository.countCompletedChallengesByUserId(user.getId());
            // System.out.println("Completed challenges: " + completedChallenges);

            // Créer une réponse simple
            UserGamificationStatsResponse response = new UserGamificationStatsResponse(
                    totalXpVal,
                    currentLevelVal,
                    "Niveau " + currentLevelVal,
                    "Description du niveau",
                    100,
                    100,
                    0.0,
                    badgesCount,
                    completedChallenges,
                    1,
                    new ArrayList<>(),
                    new ArrayList<>());

            // System.out.println("Réponse créée avec succès!");
            // System.out.println("========================================");
            return response;
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("ERREUR CRITIQUE dans getUserStats pour " + user.getEmail());
            System.err.println("Type d'exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("========================================");
            return createDefaultStats();
        }
    }

    @Transactional
    public void markChallengeAsViewed(User user, Long challengeId) {
        UserChallenge userChallenge = userChallengeRepository.findByUserId(user.getId()).stream()
                .filter(uc -> uc.getChallenge().getId().equals(challengeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Défi non trouvé pour cet utilisateur"));

        userChallenge.setIsNew(false);
        userChallengeRepository.save(userChallenge);
    }

    @Transactional
    public void markBadgeAsViewed(User user, Long badgeId) {
        UserBadge userBadge = userBadgeRepository.findByUserId(user.getId()).stream()
                .filter(ub -> ub.getBadge().getId().equals(badgeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Badge non trouvé pour cet utilisateur"));

        userBadge.setIsNew(false);
        userBadgeRepository.save(userBadge);
    }

    public List<UserGamificationStatsResponse.UserBadgeResponse> getUserBadges(User user, String filter) {
        try {
            // 1. Récupérer tous les badges actifs du système
            List<Badge> allActiveBadges = badgeRepository.findByIsActiveTrue();

            // 2. Récupérer les badges déjà obtenus par l'utilisateur
            List<UserBadge> userEarnedBadges = userBadgeRepository.findByUserId(user.getId());

            // Créer une map pour une recherche rapide par ID de badge
            Map<Long, UserBadge> earnedBadgesMap = userEarnedBadges.stream()
                    .collect(Collectors.toMap(ub -> ub.getBadge().getId(), ub -> ub));

            // 3. Fusionner les résultats
            return allActiveBadges.stream()
                    .map(badge -> {
                        UserBadge earnedBadge = earnedBadgesMap.get(badge.getId());

                        // Si le badge est obtenu, on utilise ses infos (date, new, etc.)
                        if (earnedBadge != null) {
                            return convertToUserBadgeResponse(earnedBadge);
                        }

                        // Sinon, on crée une réponse "badge verrouillé"
                        return new UserGamificationStatsResponse.UserBadgeResponse(
                                badge.getId(),
                                badge.getName(),
                                badge.getDescription(),
                                badge.getCriteriaType().toString(), // Added criteriaType
                                badge.getIconUrl(),
                                0L, // earnedAt = 0 signifie non obtenu
                                false // isNew = false
                        );
                    })
                    // Appliquer le filtre si nécessaire (pour l'instant 'all' renvoie tout)
                    .filter(response -> {
                        if ("earned".equals(filter))
                            return response.getEarnedAt() > 0;
                        if ("new".equals(filter))
                            return response.getIsNew();
                        if ("locked".equals(filter))
                            return response.getEarnedAt() == 0;
                        return true;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de la récupération des badges pour " + user.getEmail() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<UserChallengeResponse> getUserChallenges(User user) {
        try {
            // 1. Récupérer tous les défis actifs du système
            List<Challenge> allActiveChallenges = challengeRepository.findByIsActiveTrue();
            System.out.println("DEBUG: Challenges actifs trouvés: " + allActiveChallenges.size());

            // 2. Récupérer les défis déjà rejoints/complétés par l'utilisateur
            List<UserChallenge> userChallenges = userChallengeRepository.findByUserId(user.getId());
            System.out.println("DEBUG: Challenges utilisateur trouvés: " + userChallenges.size());

            // Map pour recherche rapide
            Map<Long, UserChallenge> userChallengesMap = userChallenges.stream()
                    .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), uc -> uc));

            // 3. Fusionner
            return allActiveChallenges.stream()
                    .map(challenge -> {
                        UserChallenge userChallenge = userChallengesMap.get(challenge.getId());

                        if (userChallenge != null) {
                            return convertToUserChallengeResponse(userChallenge);
                        }

                        // Défi disponible mais non commencé
                        return new UserChallengeResponse(
                                challenge.getId(),
                                challenge.getName(),
                                challenge.getDescription(),
                                challenge.getChallengeType().toString(),
                                challenge.getTargetValue(),
                                0, // currentProgress
                                0.0, // progressPercent (Double)
                                challenge.getXpReward(),
                                false, // isCompleted
                                0L, // completedAt (Long)
                                0L, // joinedAt (Long)
                                challenge.getEndDate() != null ? challenge.getEndDate() : 0L,
                                formatTimeRemaining(challenge.getEndDate()),
                                false, // isNew
                                true // isActive
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de la récupération des défis pour " + user.getEmail() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public UserLeaderboardResponse getUserLeaderboard(User user) {
        try {
            // Position de l'utilisateur
            int userPosition = getApproximateLeaderboardPosition(user);
            UserXP userXP = gamificationService.getUserXP(user);
            Optional<Level> userLevel = levelRepository.findByLevel(userXP.getCurrentLevel());
            int userBadgesCount = (int) userBadgeRepository.countByUserId(user.getId());

            UserLeaderboardResponse.UserPositionResponse userPos = new UserLeaderboardResponse.UserPositionResponse(
                    userPosition,
                    user.getFirstName() + " " + user.getLastName(),
                    userXP.getTotalXP(),
                    userXP.getCurrentLevel(),
                    userLevel.map(Level::getName).orElse("Niveau " + userXP.getCurrentLevel()),
                    userBadgesCount);

            // Top 10 (approximation)
            List<UserLeaderboardResponse.LeaderboardEntryResponse> topLeaderboard = getTopLeaderboard(user, 10);

            return new UserLeaderboardResponse(userPos, topLeaderboard);
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de la récupération du classement pour " + user.getEmail() + ": " + e.getMessage());
            return new UserLeaderboardResponse(null, new ArrayList<>());
        }
    }

    public List<UserGamificationStatsResponse.RecentActivityResponse> getRecentActivity(User user, int limit) {
        try {
            List<UserGamificationStatsResponse.RecentActivityResponse> activities = new ArrayList<>();

            // Récupérer les notifications de parcours récentes
            List<ParcoursNotification> recentParcoursNotifications = parcoursNotificationRepository
                    .findByUserOrderByCreatedAtDesc(user).stream()
                    .limit(limit / 3)
                    .collect(Collectors.toList());

            for (ParcoursNotification notification : recentParcoursNotifications) {
                String icon = "🎉";
                if (notification.getType() == ParcoursNotification.NotificationType.CERTIFICATE_READY) {
                    icon = "📜";
                }
                
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                        "parcours",
                        notification.getMessage(),
                        notification.getXpEarned() != null ? notification.getXpEarned() : 0,
                        formatTimeAgo(notification.getCreatedAt()),
                        icon));
            }

            // Récupérer les badges récents
            List<UserBadge> recentBadges = userBadgeRepository.findByUserId(user.getId()).stream()
                    .sorted((a, b) -> Long.compare(b.getEarnedAt(), a.getEarnedAt()))
                    .limit(limit / 3)
                    .collect(Collectors.toList());

            for (UserBadge badge : recentBadges) {
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                        "badge",
                        "Récompense \"" + badge.getBadge().getName() + "\" obtenue",
                        0,
                        formatTimeAgo(badge.getEarnedAt()),
                        "🏆"));
            }

            // Récupérer les défis récents complétés
            List<UserChallenge> recentChallenges = userChallengeRepository.findByUserId(user.getId()).stream()
                    .filter(UserChallenge::isCompleted)
                    .sorted((a, b) -> Long.compare(b.getCompletedAt(), a.getCompletedAt()))
                    .limit(limit / 3)
                    .collect(Collectors.toList());

            for (UserChallenge challenge : recentChallenges) {
                activities.add(new UserGamificationStatsResponse.RecentActivityResponse(
                        "challenge",
                        "Défi \"" + challenge.getChallenge().getName() + "\" réussi",
                        challenge.getChallenge().getXpReward(),
                        formatTimeAgo(challenge.getCompletedAt()),
                        "🎯"));
            }

            // Trier toutes les activités par date (décroissant)
            // Note: Comme nous n'avons pas une table unique d'activité, on trie la liste
            // combinée
            // Pour l'instant, c'est déjà pas mal.

            return activities.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'activité récente pour " + user.getEmail() + ": "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }
    // Méthodes utilitaires privées

    private String formatTimeRemaining(Long endDate) {
        if (endDate == null || endDate == 0)
            return null;
        long now = System.currentTimeMillis();
        long diff = endDate - now;
        if (diff <= 0)
            return "Terminé";
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days > 0)
            return days + " jours";
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours > 0)
            return hours + " heures";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        return minutes + " minutes";
    }

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
                new ArrayList<>(), new ArrayList<>());
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
            return userBadgeRepository.findByUserId(user.getId()).stream()
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
        // Utiliser le champ isNew de l'entité (mis à jour quand l'utilisateur le voit)
        boolean isNew = userBadge.getIsNew() != null && userBadge.getIsNew();

        return new UserGamificationStatsResponse.UserBadgeResponse(
                userBadge.getBadge().getId(),
                userBadge.getBadge().getName(),
                userBadge.getBadge().getDescription(),
                userBadge.getBadge().getCriteriaType().toString(), // Added criteriaType
                userBadge.getBadge().getIconUrl(),
                userBadge.getEarnedAt(),
                isNew);
    }

    private UserChallengeResponse convertToUserChallengeResponse(UserChallenge userChallenge) {
        Challenge challenge = userChallenge.getChallenge();
        double progressPercent = challenge.getTargetValue() > 0
                ? (double) userChallenge.getCurrentProgress() / challenge.getTargetValue() * 100
                : 0;

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
                userChallenge.getIsNew() != null && userChallenge.getIsNew(),
                challenge.getIsActive());
    }

    private List<UserLeaderboardResponse.LeaderboardEntryResponse> getTopLeaderboard(User currentUser, int limit) {
        try {
            List<UserXP> topUsers = userXPRepository.findAllOrderByTotalXPDesc().stream()
                    .limit(limit)
                    .collect(Collectors.toList());

            List<UserLeaderboardResponse.LeaderboardEntryResponse> entries = new ArrayList<>();
            for (int i = 0; i < topUsers.size(); i++) {
                UserXP userXP = topUsers.get(i);
                User u = userXP.getUser();
                Optional<Level> level = levelRepository.findByLevel(userXP.getCurrentLevel());
                int badgesCount = (int) userBadgeRepository.countByUserId(u.getId());

                entries.add(new UserLeaderboardResponse.LeaderboardEntryResponse(
                        i + 1,
                        u.getFirstName() + " " + u.getLastName(),
                        userXP.getTotalXP(),
                        userXP.getCurrentLevel(),
                        level.map(Level::getName).orElse("Niveau " + userXP.getCurrentLevel()),
                        badgesCount,
                        u.getId().equals(currentUser.getId())));
            }

            return entries;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String formatTimeAgo(Long timestamp) {
        if (timestamp == null)
            return "récemment";

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

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null)
            return "récemment";

        // Convertir LocalDateTime en timestamp
        long timestamp = dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return formatTimeAgo(timestamp);
    }

    private void initializeUserChallengesIfNeeded(User user) {
        try {
            // Vérifier si l'utilisateur a des défis
            List<UserChallenge> userChallenges = userChallengeRepository.findByUserId(user.getId());
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
                System.out.println(
                        "Défis initialisés pour " + user.getEmail() + ": " + activeChallenges.size() + " défis");
            }
        } catch (Exception e) {
            System.err.println(
                    "Erreur lors de l'initialisation des défis pour " + user.getEmail() + ": " + e.getMessage());
        }
    }
}