package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.EnrollmentTestService;
import com.kawi_niveau.backend.service.ParcoursProgressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur temporaire pour tester la progression automatique
 */
@RestController
@RequestMapping("/api/test-progression")
@CrossOrigin(origins = "http://localhost:4200")
public class ProgressionTestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentTestService enrollmentTestService;

    @Autowired
    private ParcoursProgressionService progressionService;

    /**
     * Test pour déclencher manuellement la progression d'un cours
     */
    @PostMapping("/cours/{coursId}/force-complete")
    public ResponseEntity<?> forceCompleteCours(@PathVariable Long coursId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🧪 TEST FORCE COMPLETE - Cours: " + coursId + " - User: " + userEmail);
            
            // Déclencher le test de progression
            enrollmentTestService.testMarkLeconAsCompleted(user.getId(), coursId, 1L); // Leçon fictive
            
            return ResponseEntity.ok(Map.of(
                "message", "Test de progression déclenché",
                "coursId", coursId,
                "userId", user.getId()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur test force complete: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Test pour déclencher manuellement la progression des parcours
     */
    @PostMapping("/parcours/force-update")
    public ResponseEntity<?> forceUpdateParcours(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🧪 TEST FORCE UPDATE PARCOURS - User: " + userEmail);
            
            // Déclencher le recalcul de tous les parcours
            progressionService.recalculerProgressionUtilisateur(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Recalcul progression parcours déclenché",
                "user", userEmail
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur test force update parcours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}