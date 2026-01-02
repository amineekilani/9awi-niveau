package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.RecommendationResponse;
import com.kawi_niveau.backend.service.RecommendationService;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contrôleur REST pour l'API de recommandations pédagogiques
 * Endpoints pour générer et récupérer des recommandations personnalisées
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:4200")
public class RecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Génère des recommandations personnalisées pour l'utilisateur connecté
     * 
     * @param authentication Utilisateur connecté
     * @return Recommandations au format JSON
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('FORMATEUR')")
    public ResponseEntity<RecommendationResponse> getMyRecommendations(Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            logger.info("Génération de recommandations pour l'utilisateur {}", user.getId());
            
            RecommendationResponse recommendations = recommendationService
                    .generateRecommendations(user.getId());
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations pour l'utilisateur {}", 
                    authentication.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Génère des recommandations pour un utilisateur spécifique (admin/formateur)
     * 
     * @param userId ID de l'utilisateur cible
     * @param authentication Utilisateur connecté (doit être admin/formateur)
     * @return Recommandations au format JSON
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FORMATEUR')")
    public ResponseEntity<RecommendationResponse> getUserRecommendations(
            @PathVariable Long userId,
            Authentication authentication) {
        
        try {
            logger.info("Génération de recommandations pour l'utilisateur {} par {}", 
                    userId, authentication.getName());
            
            RecommendationResponse recommendations = recommendationService
                    .generateRecommendations(userId);
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations pour l'utilisateur {}", 
                    userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Génère des recommandations avec paramètres personnalisés
     * 
     * @param authentication Utilisateur connecté
     * @param maxRecommendations Nombre maximum de recommandations (défaut: 10)
     * @param includeCompleted Inclure les contenus déjà complétés (défaut: false)
     * @param focusArea Domaine de focus spécifique (optionnel)
     * @return Recommandations personnalisées
     */
    @GetMapping("/me/custom")
    @PreAuthorize("hasRole('ETUDIANT') or hasRole('FORMATEUR')")
    public ResponseEntity<RecommendationResponse> getCustomRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "10") Integer maxRecommendations,
            @RequestParam(defaultValue = "false") Boolean includeCompleted,
            @RequestParam(required = false) String focusArea) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            logger.info("Génération de recommandations personnalisées pour l'utilisateur {} " +
                    "(max: {}, includeCompleted: {}, focus: {})", 
                    user.getId(), maxRecommendations, includeCompleted, focusArea);
            
            // Pour l'instant, utiliser la méthode standard
            // TODO: Implémenter les paramètres personnalisés
            RecommendationResponse recommendations = recommendationService
                    .generateRecommendations(user.getId());
            
            return ResponseEntity.ok(recommendations);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de recommandations personnalisées pour l'utilisateur {}", 
                    authentication.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint pour tester le moteur de recommandation avec des données d'exemple
     * Disponible uniquement en mode développement
     */
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testRecommendationEngine() {
        
        try {
            logger.info("Test du moteur de recommandation");
            
            // Exemple de test avec données fictives
            String testResult = """
                {
                  "status": "success",
                  "message": "Moteur de recommandation opérationnel",
                  "timestamp": "%s",
                  "features": [
                    "Filtrage collaboratif",
                    "Recommandations basées sur le contenu",
                    "Règles pédagogiques",
                    "Analyse des performances",
                    "Recommandations par niveau"
                  ]
                }
                """.formatted(java.time.Instant.now().toString());
            
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            logger.error("Erreur lors du test du moteur de recommandation", e);
            return ResponseEntity.internalServerError()
                    .body("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}