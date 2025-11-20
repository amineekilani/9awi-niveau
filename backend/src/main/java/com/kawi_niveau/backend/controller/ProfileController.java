package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponse profile = new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getProvider(),
                user.isEmailVerified(),
                user.getFirstName(),
                user.getLastName(),
                user.getDateOfBirth()
        );

        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if it's already taken
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
            }
            user.setEmail(request.getEmail());
            // If email is changed, mark as not verified
            user.setEmailVerified(false);
        }

        // Update personal information
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        // Update password if provided (only for local users)
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (!"local".equals(user.getProvider())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Cannot change password for OAuth users"));
            }

            // Verify current password
            if (request.getCurrentPassword() == null || !encoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
            }

            user.setPassword(encoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profil mis à jour avec succès"));
    }

    @PostMapping("/request-delete")
    public ResponseEntity<?> requestAccountDeletion(@RequestBody DeleteAccountRequest request, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify email matches
        if (!user.getEmail().equals(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email does not match"));
        }

        // Generate deletion token
        String deleteToken = UUID.randomUUID().toString();
        user.setDeleteToken(deleteToken);
        user.setDeleteTokenExpiry(System.currentTimeMillis() + 3600000); // 1 hour
        userRepository.save(user);

        // Send deletion confirmation email
        try {
            emailService.sendAccountDeletionEmail(user.getEmail(), user.getEmail(), deleteToken);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error sending deletion email: " + e.getMessage()));
        }

        return ResponseEntity.ok(new MessageResponse("Deletion confirmation email sent. Please check your inbox."));
    }

    @DeleteMapping("/confirm-delete")
    public ResponseEntity<?> confirmAccountDeletion(@RequestParam String token) {
        User user = userRepository.findByDeleteToken(token)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid deletion token"));
        }

        if (user.getDeleteTokenExpiry() < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Deletion token has expired"));
        }

        // Delete the user
        userRepository.delete(user);

        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}
