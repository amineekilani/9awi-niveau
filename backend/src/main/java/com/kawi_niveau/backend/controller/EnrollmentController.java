package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ApprenantProgressionResponse;
import com.kawi_niveau.backend.dto.EnrollmentRequest;
import com.kawi_niveau.backend.dto.EnrollmentResponse;
import com.kawi_niveau.backend.dto.LeconCompletionRequest;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "http://localhost:4200")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollInCourse(
            @Valid @RequestBody EnrollmentRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        EnrollmentResponse response = enrollmentService.enrollInCourse(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getUserEnrollments(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        List<EnrollmentResponse> enrollments = enrollmentService.getUserEnrollments(user.getId());
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<EnrollmentResponse> getEnrollmentDetails(
            @PathVariable Long coursId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentDetails(user.getId(), coursId);
        return ResponseEntity.ok(enrollment);
    }

    @PostMapping("/cours/{coursId}/complete-lecon")
    public ResponseEntity<EnrollmentResponse> markLeconAsCompleted(
            @PathVariable Long coursId,
            @Valid @RequestBody LeconCompletionRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        System.out.println("🎯 API APPELÉE: markLeconAsCompleted - Cours: " + coursId + " - Leçon: " + request.getLeconId() + " - User: " + email);
        
        EnrollmentResponse response = enrollmentService.markLeconAsCompleted(user.getId(), coursId, request);
        return ResponseEntity.ok(response);
    }

    // NOUVEAU: Endpoint de debug pour forcer la mise à jour de progression
    @PostMapping("/cours/{coursId}/force-update-progress")
    public ResponseEntity<?> forceUpdateProgress(
            @PathVariable Long coursId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            System.out.println("🔧 FORCE UPDATE PROGRESS - Cours: " + coursId + " - User: " + email);
            
            // Obtenir l'enrollment et forcer la mise à jour
            EnrollmentResponse response = enrollmentService.getEnrollmentDetails(user.getId(), coursId);
            
            return ResponseEntity.ok().body("{\"message\": \"Progression vérifiée\", \"progress\": " + response.getProgress() + "}");
        } catch (Exception e) {
            System.err.println("❌ Erreur force update: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @DeleteMapping("/cours/{coursId}/lecons/{leconId}/completion")
    public ResponseEntity<EnrollmentResponse> unmarkLeconAsCompleted(
            @PathVariable Long coursId,
            @PathVariable Long leconId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        EnrollmentResponse response = enrollmentService.unmarkLeconAsCompleted(user.getId(), coursId, leconId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cours/{coursId}/completed-lecons")
    public ResponseEntity<List<Long>> getCompletedLeconIds(
            @PathVariable Long coursId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        List<Long> completedIds = enrollmentService.getCompletedLeconIds(user.getId(), coursId);
        return ResponseEntity.ok(completedIds);
    }

    @GetMapping("/cours/{coursId}/apprenants")
    public ResponseEntity<List<ApprenantProgressionResponse>> getApprenantsProgression(
            @PathVariable Long coursId,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmailAndArchivedFalse(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Vérifier que l'utilisateur est formateur
        if (!"FORMATEUR".equals(user.getRole().name())) {
            throw new RuntimeException("Accès non autorisé");
        }
        
        List<ApprenantProgressionResponse> apprenants = enrollmentService.getApprenantsProgression(coursId);
        return ResponseEntity.ok(apprenants);
    }
}
