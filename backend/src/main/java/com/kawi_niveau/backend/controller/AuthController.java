package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.JwtResponse;
import com.kawi_niveau.backend.dto.LoginRequest;
import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.dto.RegisterRequest;
import com.kawi_niveau.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    com.kawi_niveau.backend.repository.UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Vérifier si l'utilisateur existe et n'est pas archivé
        com.kawi_niveau.backend.entity.User user = userRepository.findByEmailAndArchivedFalse(loginRequest.getEmail())
                .orElse(null);

        // Vérifier si l'utilisateur est un utilisateur local (pas Google OAuth)
        if (user != null && "local".equals(user.getProvider())) {
            // Vérifier si le compte est verrouillé
            if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil() > System.currentTimeMillis()) {
                long remainingMinutes = (user.getAccountLockedUntil() - System.currentTimeMillis()) / 60000;
                return ResponseEntity.status(423).body(new MessageResponse(
                    "Votre compte est temporairement verrouillé suite à plusieurs tentatives de connexion échouées. Réessayez dans " + remainingMinutes + " minute(s)."
                ));
            }

            // Vérifier si l'email est vérifié
            if (!user.isEmailVerified()) {
                throw new com.kawi_niveau.backend.exception.EmailNotVerifiedException(
                    "Veuillez vérifier votre adresse email avant de vous connecter. Consultez votre boîte de réception pour le lien de vérification."
                );
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication.getName());

            // Réinitialiser les tentatives échouées en cas de succès
            if (user != null) {
                user.setFailedLoginAttempts(0);
                user.setLastFailedLogin(null);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
                
                // Enregistrer la connexion pour la gamification
                try {
                    String ipAddress = getClientIpAddress();
                    String userAgent = getUserAgent();
                    gamificationService.recordLogin(user, ipAddress, userAgent);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'enregistrement de connexion pour gamification: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(new JwtResponse(jwt, loginRequest.getEmail(), user.getRole().name()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Gérer les tentatives échouées
            if (user != null && "local".equals(user.getProvider())) {
                int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
                attempts++;
                user.setFailedLoginAttempts(attempts);
                user.setLastFailedLogin(System.currentTimeMillis());

                // Envoyer un email d'alerte après 5 tentatives
                if (attempts == 5) {
                    try {
                        emailService.sendFailedLoginAlertEmail(user.getEmail(), user.getEmail(), attempts);
                    } catch (Exception emailEx) {
                        // Log l'erreur mais ne pas bloquer la réponse
                        System.err.println("Erreur lors de l'envoi de l'email d'alerte: " + emailEx.getMessage());
                    }
                    // Verrouiller le compte pour 15 minutes
                    user.setAccountLockedUntil(System.currentTimeMillis() + 900000); // 15 minutes
                }

                userRepository.save(user);

                if (attempts >= 5) {
                    return ResponseEntity.status(423).body(new MessageResponse(
                        "Trop de tentatives échouées. Votre compte est verrouillé pendant 15 minutes. Un email d'alerte vous a été envoyé."
                    ));
                }
            }

            throw e;
        }
    }

    @Autowired
    private com.kawi_niveau.backend.service.EmailService emailService;

    @Autowired
    private com.kawi_niveau.backend.service.GamificationService gamificationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmailAndArchivedFalse(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
        }

        com.kawi_niveau.backend.entity.User user = new com.kawi_niveau.backend.entity.User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setProvider("local");
        user.setEmailVerified(false);
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setDateOfBirth(registerRequest.getDateOfBirth());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setCreatedAt(System.currentTimeMillis()); // Set registration date
        
        // Generate verification token
        String verificationToken = java.util.UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);

        userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getEmail(), verificationToken);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("User registered but email sending failed: " + e.getMessage()));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully. Please check your email to verify your account."));
    }

    @Autowired
    private com.kawi_niveau.backend.service.OAuth2Service oauth2Service;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody com.kawi_niveau.backend.dto.OAuth2LoginRequest request) {
        try {
            com.kawi_niveau.backend.entity.User user = oauth2Service.processGoogleUser(request.getToken());
            String jwt = jwtUtils.generateJwtToken(user.getEmail());
            
            // Enregistrer la connexion pour la gamification
            try {
                String ipAddress = getClientIpAddress();
                String userAgent = getUserAgent();
                gamificationService.recordLogin(user, ipAddress, userAgent);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'enregistrement de connexion Google pour gamification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(new JwtResponse(jwt, user.getEmail(), user.getRole().name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Google authentication failed: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Backend is working!");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        com.kawi_niveau.backend.entity.User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid verification token"));
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Email verified successfully. You can now login."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody com.kawi_niveau.backend.dto.ForgotPasswordRequest request) {
        com.kawi_niveau.backend.entity.User user = userRepository.findByEmailAndArchivedFalse(request.getEmail())
                .orElse(null);

        if (user == null) {
            // Don't reveal if email exists or not for security
            return ResponseEntity.ok(new MessageResponse("If the email exists, a password reset link has been sent."));
        }

        // Generate reset token
        String resetToken = java.util.UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(System.currentTimeMillis() + 3600000); // 1 hour
        userRepository.save(user);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getEmail(), resetToken);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error sending reset email: " + e.getMessage()));
        }

        return ResponseEntity.ok(new MessageResponse("If the email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.kawi_niveau.backend.dto.ResetPasswordRequest request) {
        com.kawi_niveau.backend.entity.User user = userRepository.findByResetToken(request.getToken())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid reset token"));
        }

        if (user.getResetTokenExpiry() < System.currentTimeMillis()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Reset token has expired"));
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Password reset successfully. You can now login with your new password."));
    }

    // Méthodes utilitaires pour récupérer les informations de connexion
    private String getClientIpAddress() {
        // Cette méthode devrait récupérer l'IP réelle du client
        // Pour l'instant, retourner une valeur par défaut
        return "127.0.0.1";
    }

    private String getUserAgent() {
        // Cette méthode devrait récupérer le User-Agent du client
        // Pour l'instant, retourner une valeur par défaut
        return "Unknown";
    }
}