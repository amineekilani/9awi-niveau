package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.ParcoursIntegrationService;
import com.kawi_niveau.backend.service.ParcoursProgressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion de la progression des parcours
 * Inclut des endpoints de secours pour l'actualisation manuelle
 */
@RestController
@RequestMapping("/api/parcours-progression")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursProgressionController {

    @Autowired
    private ParcoursProgressionService progressionService;

    @Autowired
    private ParcoursIntegrationService integrationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint de secours pour actualiser manuellement la progression
     */
    @PostMapping("/actualiser")
    public ResponseEntity<?> actualiserProgression(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🔄 Actualisation manuelle de la progression pour: " + email);
            
            // Recalculer toute la progression de l'utilisateur
            integrationService.recalculerProgressionComplete(user.getId());
            
            System.out.println("✅ Progression actualisée avec succès pour: " + email);
            
            return ResponseEntity.ok().body("{\"message\": \"Progression actualisée avec succès\", \"user\": \"" + email + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'actualisation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Endpoint de debug pour forcer la mise à jour d'un cours spécifique
     */
    @PostMapping("/actualiser-cours/{coursId}")
    public ResponseEntity<?> actualiserProgressionCours(
            @PathVariable Long coursId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🔄 Actualisation progression cours " + coursId + " pour: " + email);
            
            integrationService.onCoursProgressUpdated(user.getId(), coursId);
            
            System.out.println("✅ Progression cours actualisée avec succès");
            
            return ResponseEntity.ok().body("{\"message\": \"Progression cours actualisée\", \"coursId\": " + coursId + "}");
        } catch (Exception e) {
            System.err.println("❌ Erreur actualisation cours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Endpoint de test pour vérifier le système de progression
     */
    @GetMapping("/test-progression")
    public ResponseEntity<?> testProgression(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🧪 Test du système de progression pour: " + email);
            
            // Recalculer la progression pour tester
            progressionService.recalculerProgressionUtilisateur(user);
            
            return ResponseEntity.ok().body("{\"message\": \"Test progression terminé\", \"user\": \"" + email + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erreur test progression: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}