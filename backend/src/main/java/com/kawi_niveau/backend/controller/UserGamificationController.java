package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.UserGamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserGamificationController {

    @Autowired
    private UserGamificationService userGamificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<UserGamificationStatsResponse> getUserStats(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserGamificationStatsResponse stats = userGamificationService.getUserStats(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des stats utilisateur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/badges")
    public ResponseEntity<List<UserGamificationStatsResponse.UserBadgeResponse>> getUserBadges(
            @RequestParam(defaultValue = "all") String filter,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<UserGamificationStatsResponse.UserBadgeResponse> badges = 
                userGamificationService.getUserBadges(user, filter);
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des badges utilisateur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/challenges")
    public ResponseEntity<List<UserChallengeResponse>> getUserChallenges(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<UserChallengeResponse> challenges = userGamificationService.getUserChallenges(user);
            return ResponseEntity.ok(challenges);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des défis utilisateur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<UserLeaderboardResponse> getUserLeaderboard(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserLeaderboardResponse leaderboard = userGamificationService.getUserLeaderboard(user);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du classement utilisateur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

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
}