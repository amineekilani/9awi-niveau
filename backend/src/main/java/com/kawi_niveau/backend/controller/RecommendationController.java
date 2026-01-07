package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.CoursRecommendationResponse;
import com.kawi_niveau.backend.dto.ParcoursRecommendationResponse;
import com.kawi_niveau.backend.dto.RecommendationRequest;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserPreferences;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.repository.UserPreferencesRepository;
import com.kawi_niveau.backend.service.AIRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:4200")
public class RecommendationController {

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    /**
     * Obtenir des recommandations personnalisées basées sur le profil utilisateur
     */
    @GetMapping("/personalized")
    public ResponseEntity<?> getPersonalizedRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "5") Integer maxResults) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<ParcoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getPersonalizedRecommendations(user, maxResults);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }

    /**
     * Obtenir des recommandations basées sur des critères spécifiques
     */
    @PostMapping("/by-criteria")
    public ResponseEntity<?> getRecommendationsByCriteria(
            Authentication authentication,
            @RequestBody RecommendationRequest request) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<ParcoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getRecommendationsByCriteria(user, request);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations par critères: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }

    /**
     * Obtenir les préférences utilisateur
     */
    @GetMapping("/preferences")
    public ResponseEntity<?> getUserPreferences(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Optional<UserPreferences> preferences = userPreferencesRepository.findByUser(user);
            
            if (preferences.isPresent()) {
                return ResponseEntity.ok(preferences.get());
            } else {
                // Créer des préférences par défaut
                UserPreferences defaultPrefs = new UserPreferences();
                defaultPrefs.setUser(user);
                return ResponseEntity.ok(defaultPrefs);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des préférences: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des préférences: " + e.getMessage());
        }
    }

    /**
     * Sauvegarder les préférences utilisateur
     */
    @PostMapping("/preferences")
    public ResponseEntity<?> saveUserPreferences(
            Authentication authentication,
            @RequestBody UserPreferences preferences) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier si des préférences existent déjà
            Optional<UserPreferences> existingPrefs = userPreferencesRepository.findByUser(user);
            
            UserPreferences prefsToSave;
            if (existingPrefs.isPresent()) {
                prefsToSave = existingPrefs.get();
                // Mettre à jour les champs
                prefsToSave.setPreferredCategories(preferences.getPreferredCategories());
                prefsToSave.setPreferredDifficulty(preferences.getPreferredDifficulty());
                prefsToSave.setLearningStyle(preferences.getLearningStyle());
                prefsToSave.setTimeAvailabilityHours(preferences.getTimeAvailabilityHours());
                prefsToSave.setLearningGoals(preferences.getLearningGoals());
                prefsToSave.setInterests(preferences.getInterests());
                prefsToSave.setCareerFocus(preferences.getCareerFocus());
                prefsToSave.setPreferredDurationMin(preferences.getPreferredDurationMin());
                prefsToSave.setPreferredDurationMax(preferences.getPreferredDurationMax());
                prefsToSave.setChallengePreference(preferences.getChallengePreference());
                prefsToSave.setCertificationImportant(preferences.getCertificationImportant());
            } else {
                prefsToSave = preferences;
                prefsToSave.setUser(user);
            }

            UserPreferences savedPrefs = userPreferencesRepository.save(prefsToSave);
            return ResponseEntity.ok(savedPrefs);

        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }

    /**
     * Obtenir des recommandations rapides (top 3)
     */
    @GetMapping("/quick")
    public ResponseEntity<?> getQuickRecommendations(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<ParcoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getPersonalizedRecommendations(user, 3);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations rapides: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }

    /**
     * Obtenir des recommandations de cours personnalisées
     */
    @GetMapping("/cours/personalized")
    public ResponseEntity<?> getPersonalizedCoursRecommendations(
            Authentication authentication,
            @RequestParam(defaultValue = "8") Integer maxResults) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<CoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getPersonalizedCoursRecommendations(user, maxResults);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations de cours: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations de cours: " + e.getMessage());
        }
    }

    /**
     * Obtenir des recommandations de cours basées sur des critères
     */
    @PostMapping("/cours/by-criteria")
    public ResponseEntity<?> getCoursRecommendationsByCriteria(
            Authentication authentication,
            @RequestBody RecommendationRequest request) {
        
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<CoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getCoursRecommendationsByCriteria(user, request);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations de cours par critères: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }

    /**
     * Obtenir des recommandations rapides de cours (top 3)
     */
    @GetMapping("/cours/quick")
    public ResponseEntity<?> getQuickCoursRecommendations(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<CoursRecommendationResponse> recommendations = 
                    aiRecommendationService.getPersonalizedCoursRecommendations(user, 3);

            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des recommandations rapides de cours: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }
}