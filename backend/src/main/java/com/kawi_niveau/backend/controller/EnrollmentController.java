package com.kawi_niveau.backend.controller;

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
        EnrollmentResponse response = enrollmentService.markLeconAsCompleted(user.getId(), coursId, request);
        return ResponseEntity.ok(response);
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
}
