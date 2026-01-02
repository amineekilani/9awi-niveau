package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.BadgeService;
import com.kawi_niveau.backend.service.ChallengeService;
import com.kawi_niveau.backend.service.LeaderboardService;
import com.kawi_niveau.backend.service.LevelService;
import com.kawi_niveau.backend.repository.BadgeRepository;
import com.kawi_niveau.backend.repository.ChallengeRepository;
import com.kawi_niveau.backend.repository.UserBadgeRepository;
import com.kawi_niveau.backend.repository.UserChallengeRepository;
import com.kawi_niveau.backend.repository.UserXPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/gamification")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminGamificationController {

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    @Autowired
    private UserXPRepository userXPRepository;

    // Endpoint de test simple
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Endpoint de gamification admin accessible"));
    }

    // Vérifier si l'utilisateur connecté est admin
    private void checkAdminRole(Authentication authentication) {
        User currentUser = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès non autorisé - Rôle administrateur requis");
        }
    }

    // ===== STATISTIQUES GÉNÉRALES =====
    @GetMapping("/stats")
    public ResponseEntity<?> getGamificationStats(Authentication authentication) {
        try {
            System.out.println("=== DEBUG: getGamificationStats called ===");
            
            checkAdminRole(authentication);
            System.out.println("Admin role check passed");

            // Utiliser les vraies données de la base
            long totalBadges = badgeRepository.count();
            long activeBadges = badgeRepository.findByIsActiveTrue().size();
            long totalChallenges = challengeRepository.count();
            long activeChallenges = challengeService.getActiveChallenges().size();
            long totalBadgesEarned = userBadgeRepository.count();
            long totalChallengesCompleted = userChallengeRepository.findAll().stream()
                    .mapToLong(uc -> uc.isCompleted() ? 1 : 0)
                    .sum();
            
            // Calculer le total des XP attribués
            long totalXPAwarded = userXPRepository.findAll().stream()
                    .mapToLong(ux -> ux.getTotalXP())
                    .sum();
            
            Double averageUserXP = userXPRepository.getAverageXP();
            if (averageUserXP == null) averageUserXP = 0.0;

            GamificationStatsResponse stats = new GamificationStatsResponse(
                    totalBadges,
                    activeBadges,
                    totalChallenges,
                    (long) activeChallenges,
                    totalXPAwarded,
                    averageUserXP,
                    totalBadgesEarned,
                    totalChallengesCompleted
            );

            System.out.println("Real stats created: " + stats);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("Error in getGamificationStats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du chargement des statistiques: " + e.getMessage()));
        }
    }

    // ===== GESTION DES BADGES =====
    @GetMapping("/badges")
    public ResponseEntity<?> getAllBadges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        try {
            checkAdminRole(authentication);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : 
                    Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<BadgeResponse> badges = badgeService.getAllBadges(pageable);
            
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            System.err.println("Error in getAllBadges: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du chargement des badges: " + e.getMessage()));
        }
    }

    @GetMapping("/badges/active")
    public ResponseEntity<List<BadgeResponse>> getActiveBadges(Authentication authentication) {
        checkAdminRole(authentication);
        List<BadgeResponse> badges = badgeService.getActiveBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/badges/{id}")
    public ResponseEntity<BadgeResponse> getBadgeById(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        BadgeResponse badge = badgeService.getBadgeById(id);
        return ResponseEntity.ok(badge);
    }

    @PostMapping("/badges")
    public ResponseEntity<?> createBadge(@RequestBody BadgeRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            BadgeResponse badge = badgeService.createBadge(request);
            return ResponseEntity.ok(badge);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la création du badge: " + e.getMessage()));
        }
    }

    @PutMapping("/badges/{id}")
    public ResponseEntity<?> updateBadge(@PathVariable Long id, @RequestBody BadgeRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            BadgeResponse badge = badgeService.updateBadge(id, request);
            return ResponseEntity.ok(badge);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la mise à jour du badge: " + e.getMessage()));
        }
    }

    @DeleteMapping("/badges/{id}")
    public ResponseEntity<?> deleteBadge(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            badgeService.deleteBadge(id);
            return ResponseEntity.ok(new MessageResponse("Badge supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @PutMapping("/badges/{id}/toggle-status")
    public ResponseEntity<?> toggleBadgeStatus(@PathVariable Long id, Authentication authentication) {
        try {
            System.out.println("=== DEBUG: toggleBadgeStatus called for badge ID: " + id + " ===");
            checkAdminRole(authentication);
            
            badgeService.toggleBadgeStatus(id);
            System.out.println("Badge status toggled successfully for ID: " + id);
            
            return ResponseEntity.ok(new MessageResponse("Statut du badge modifié avec succès"));
        } catch (Exception e) {
            System.err.println("Error toggling badge status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du changement de statut: " + e.getMessage()));
        }
    }

    // ===== GESTION DES DÉFIS =====
    @GetMapping("/challenges")
    public ResponseEntity<Page<ChallengeResponse>> getAllChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChallengeResponse> challenges = challengeService.getAllChallenges(pageable);
        
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/challenges/active")
    public ResponseEntity<List<ChallengeResponse>> getActiveChallenges(Authentication authentication) {
        checkAdminRole(authentication);
        List<ChallengeResponse> challenges = challengeService.getActiveChallenges();
        return ResponseEntity.ok(challenges);
    }

    @GetMapping("/challenges/{id}")
    public ResponseEntity<ChallengeResponse> getChallengeById(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        ChallengeResponse challenge = challengeService.getChallengeById(id);
        return ResponseEntity.ok(challenge);
    }

    @PostMapping("/challenges")
    public ResponseEntity<?> createChallenge(@RequestBody ChallengeRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            ChallengeResponse challenge = challengeService.createChallenge(request);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la création du défi: " + e.getMessage()));
        }
    }

    @PutMapping("/challenges/{id}")
    public ResponseEntity<?> updateChallenge(@PathVariable Long id, @RequestBody ChallengeRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            ChallengeResponse challenge = challengeService.updateChallenge(id, request);
            return ResponseEntity.ok(challenge);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la mise à jour du défi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/challenges/{id}")
    public ResponseEntity<?> deleteChallenge(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            challengeService.deleteChallenge(id);
            return ResponseEntity.ok(new MessageResponse("Défi supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    @PutMapping("/challenges/{id}/toggle-status")
    public ResponseEntity<?> toggleChallengeStatus(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            challengeService.toggleChallengeStatus(id);
            return ResponseEntity.ok(new MessageResponse("Statut du défi modifié avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du changement de statut: " + e.getMessage()));
        }
    }

    // ===== CLASSEMENTS =====
    @GetMapping("/leaderboard")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        Pageable pageable = PageRequest.of(page, size);
        LeaderboardResponse leaderboard = leaderboardService.getLeaderboard(pageable);
        
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/top/{limit}")
    public ResponseEntity<LeaderboardResponse> getTopLeaderboard(@PathVariable int limit, Authentication authentication) {
        checkAdminRole(authentication);
        LeaderboardResponse leaderboard = leaderboardService.getLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/leaderboard/export")
    public ResponseEntity<String> exportLeaderboard(Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            LeaderboardResponse leaderboard = leaderboardService.getLeaderboard(0); // Get all users
            StringBuilder csv = new StringBuilder();
            
            // Headers
            csv.append("Rang,Prénom,Nom,Email,XP Total,Niveau,Nom du Niveau,Badges\n");
            
            // Data
            for (LeaderboardResponse.LeaderboardEntry entry : leaderboard.getEntries()) {
                csv.append(entry.getRank()).append(",")
                   .append("\"").append(entry.getFirstName()).append("\",")
                   .append("\"").append(entry.getLastName()).append("\",")
                   .append("\"").append(entry.getEmail()).append("\",")
                   .append(entry.getTotalXP()).append(",")
                   .append(entry.getCurrentLevel()).append(",")
                   .append("\"").append(entry.getLevelName()).append("\",")
                   .append(entry.getBadgesCount()).append("\n");
            }
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=\"classement-gamification.csv\"")
                    .body(csv.toString());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors de l'export: " + e.getMessage());
        }
    }

    // ===== GESTION DES NIVEAUX =====
    @GetMapping("/levels")
    public ResponseEntity<Page<LevelResponse>> getAllLevels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "level") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        checkAdminRole(authentication);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LevelResponse> levels = levelService.getAllLevels(pageable);
        
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/levels/all")
    public ResponseEntity<List<LevelResponse>> getAllLevelsOrdered(Authentication authentication) {
        checkAdminRole(authentication);
        List<LevelResponse> levels = levelService.getAllLevelsOrdered();
        return ResponseEntity.ok(levels);
    }

    @GetMapping("/levels/{id}")
    public ResponseEntity<LevelResponse> getLevelById(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        LevelResponse level = levelService.getLevelById(id);
        return ResponseEntity.ok(level);
    }

    @PostMapping("/levels")
    public ResponseEntity<?> createLevel(@RequestBody LevelRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            LevelResponse level = levelService.createLevel(request);
            return ResponseEntity.ok(level);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la création du niveau: " + e.getMessage()));
        }
    }

    @PutMapping("/levels/{id}")
    public ResponseEntity<?> updateLevel(@PathVariable Long id, @RequestBody LevelRequest request, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            LevelResponse level = levelService.updateLevel(id, request);
            return ResponseEntity.ok(level);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la mise à jour du niveau: " + e.getMessage()));
        }
    }

    @DeleteMapping("/levels/{id}")
    public ResponseEntity<?> deleteLevel(@PathVariable Long id, Authentication authentication) {
        checkAdminRole(authentication);
        
        try {
            levelService.deleteLevel(id);
            return ResponseEntity.ok(new MessageResponse("Niveau supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la suppression: " + e.getMessage()));
        }
    }
}