package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.service.ParcoursProgressionDiagnosticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnostic/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursProgressionDiagnosticController {

    @Autowired
    private ParcoursProgressionDiagnosticService diagnosticService;

    /**
     * Diagnostic complet pour un utilisateur
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<String> diagnosticUtilisateur(@PathVariable String email) {
        try {
            String diagnostic = diagnosticService.diagnosticUtilisateur(email);
            return ResponseEntity.ok(diagnostic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Force la recalculation de la progression
     */
    @PostMapping("/recalcul/{email}")
    public ResponseEntity<String> forceRecalcul(@PathVariable String email) {
        try {
            String result = diagnosticService.forceRecalculProgression(email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Test de création de notification
     */
    @PostMapping("/test-notification/{email}/{parcoursId}")
    public ResponseEntity<String> testNotification(@PathVariable String email, @PathVariable Long parcoursId) {
        try {
            String result = diagnosticService.testNotificationParcours(email, parcoursId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Test d'attribution XP
     */
    @PostMapping("/test-xp/{email}/{amount}")
    public ResponseEntity<String> testXP(@PathVariable String email, @PathVariable int amount) {
        try {
            String result = diagnosticService.testAttributionXP(email, amount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
}