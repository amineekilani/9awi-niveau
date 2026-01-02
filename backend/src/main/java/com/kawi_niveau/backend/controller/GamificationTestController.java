package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/gamification")
@CrossOrigin(origins = "http://localhost:4200")
public class GamificationTestController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/award-xp")
    public ResponseEntity<?> testAwardXP(
            @RequestParam(defaultValue = "10") Integer xpAmount,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.awardXP(user, xpAmount, "Test manuel");
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test réussi ! +" + xpAmount + " XP attribués. " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test: " + e.getMessage()));
        }
    }

    @PostMapping("/simulate-quiz")
    public ResponseEntity<?> testQuizPassed(
            @RequestParam(defaultValue = "85.0") Double score,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.onQuizPassed(user, score);
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test quiz réussi ! Score: " + score + "%. " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test quiz: " + e.getMessage()));
        }
    }

    @PostMapping("/simulate-course")
    public ResponseEntity<?> testCourseCompleted(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.onCourseCompleted(user);
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test cours terminé réussi ! " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test cours: " + e.getMessage()));
        }
    }

    @PostMapping("/simulate-lesson")
    public ResponseEntity<?> testLessonCompleted(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.onLessonCompleted(user);
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test leçon terminée réussi ! " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test leçon: " + e.getMessage()));
        }
    }

    @PostMapping("/simulate-enrollment")
    public ResponseEntity<?> testCourseEnrollment(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.onCourseEnrollment(user);
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test inscription cours réussi ! " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test inscription: " + e.getMessage()));
        }
    }

    @PostMapping("/simulate-login")
    public ResponseEntity<?> testFirstLogin(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.recordLogin(user, "127.0.0.1", "Test Browser");
            
            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Test connexion réussi ! " +
                "Total XP: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors du test connexion: " + e.getMessage()));
        }
    }

    @PostMapping("/check-challenges")
    public ResponseEntity<?> testChallengeCheck(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            gamificationService.checkAllActiveChallenges(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Vérification des défis terminée ! Consultez l'admin pour voir les résultats."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la vérification des défis: " + e.getMessage()));
        }
    }

    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        try {
            User user = userRepository.findByEmailAndArchivedFalse(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UserXP userXP = gamificationService.getUserXP(user);
            
            return ResponseEntity.ok(new MessageResponse(
                "Vos stats: " +
                "XP Total: " + userXP.getTotalXP() + 
                ", Niveau: " + userXP.getCurrentLevel() +
                ", XP pour niveau suivant: " + userXP.getXpToNextLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la récupération des stats: " + e.getMessage()));
        }
    }
}