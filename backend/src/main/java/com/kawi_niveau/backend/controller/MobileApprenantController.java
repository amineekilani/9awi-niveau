package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.UserGamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile/apprenant")
@CrossOrigin(origins = "*")
public class MobileApprenantController {

    @Autowired
    private UserGamificationService userGamificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint principal pour le dashboard de l'apprenant
     * Retourne toutes les données nécessaires pour la page d'accueil
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Récupérer toutes les statistiques
            UserGamificationStatsResponse stats = userGamificationService.getUserStats(user);
            List<UserGamificationStatsResponse.UserBadgeResponse> badges = userGamificationService.getUserBadges(user, "earned");
            List<UserChallengeResponse> challenges = userGamificationService.getUserChallenges(user);
            UserLeaderboardResponse leaderboard = userGamificationService.getUserLeaderboard(user);

            // Construire la réponse complète
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("stats", stats);
            dashboard.put("badges", badges);
            dashboard.put("challenges", challenges);
            dashboard.put("leaderboard", leaderboard);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du dashboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupérer les statistiques de l'utilisateur (XP, niveau, badges, défis)
     */
    @GetMapping("/stats")
    public ResponseEntity<UserGamificationStatsResponse> getStats(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserGamificationStatsResponse stats = userGamificationService.getUserStats(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer tous les badges de l'utilisateur
     * @param filter Options: "all", "earned", "locked", "new"
     */
    @GetMapping("/badges")
    public ResponseEntity<List<UserGamificationStatsResponse.UserBadgeResponse>> getBadges(
            @RequestParam(defaultValue = "earned") String filter,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<UserGamificationStatsResponse.UserBadgeResponse> badges =
                    userGamificationService.getUserBadges(user, filter);
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des badges: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer tous les défis de l'utilisateur (en cours et terminés)
     */
    @GetMapping("/challenges")
    public ResponseEntity<List<UserChallengeResponse>> getChallenges(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<UserChallengeResponse> challenges = userGamificationService.getUserChallenges(user);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des défis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer le classement avec la position de l'utilisateur et le top 10
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<UserLeaderboardResponse> getLeaderboard(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserLeaderboardResponse leaderboard = userGamificationService.getUserLeaderboard(user);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du classement: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer l'activité récente de l'utilisateur
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<UserGamificationStatsResponse.RecentActivityResponse>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<UserGamificationStatsResponse.RecentActivityResponse> activities =
                    userGamificationService.getRecentActivity(user, limit);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'activité récente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marquer un badge comme vu (pour retirer le flag "nouveau")
     */
    @PostMapping("/badges/{badgeId}/view")
    public ResponseEntity<?> markBadgeAsViewed(@PathVariable Long badgeId, Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            userGamificationService.markBadgeAsViewed(user, badgeId);
            return ResponseEntity.ok().body(Map.of("message", "Badge marqué comme vu"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Marquer un défi comme vu (pour retirer le flag "nouveau")
     */
    @PostMapping("/challenges/{challengeId}/view")
    public ResponseEntity<?> markChallengeAsViewed(@PathVariable Long challengeId, Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            userGamificationService.markChallengeAsViewed(user, challengeId);
            return ResponseEntity.ok().body(Map.of("message", "Défi marqué comme vu"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}
