package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.ExerciceResultatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercice-resultats")
@CrossOrigin(origins = "http://localhost:4200")
public class ExerciceResultatController {

    @Autowired
    private ExerciceResultatService exerciceResultatService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/exercice/{exerciceId}/submit")
    public ResponseEntity<ResultatExerciceResponse> submitExercice(
            @PathVariable Long exerciceId,
            @RequestBody ExerciceSubmissionRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            ResultatExerciceResponse resultat = exerciceResultatService.submitExercice(user.getId(), exerciceId, request);
            return ResponseEntity.ok(resultat);
        } catch (Exception e) {
            System.err.println("Erreur lors de la soumission de l'exercice: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la soumission de l'exercice: " + e.getMessage());
        }
    }

    @GetMapping("/exercice/{exerciceId}/attempts")
    public ResponseEntity<List<ExerciceAttemptResponse>> getUserExerciceAttempts(
            @PathVariable Long exerciceId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        List<ExerciceAttemptResponse> attempts = exerciceResultatService.getUserExerciceAttempts(user.getId(), exerciceId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/exercice/{exerciceId}/best-score")
    public ResponseEntity<ExerciceAttemptResponse> getBestScore(
            @PathVariable Long exerciceId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        ExerciceAttemptResponse bestScore = exerciceResultatService.getBestScore(user.getId(), exerciceId);
        return ResponseEntity.ok(bestScore);
    }

    @GetMapping("/{resultatId}")
    public ResponseEntity<ResultatExerciceResponse> getResultatDetails(
            @PathVariable Long resultatId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        ResultatExerciceResponse resultat = exerciceResultatService.getResultatDetails(user.getId(), resultatId);
        return ResponseEntity.ok(resultat);
    }
}