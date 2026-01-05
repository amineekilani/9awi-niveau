package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.listener.ParcoursProgressionListener;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.ParcoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursTestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcoursService parcoursService;

    /**
     * Endpoint de test pour forcer la vérification des parcours
     */
    @PostMapping("/force-check")
    public ResponseEntity<?> forceParcoursCheck(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
            }

            User user = userOpt.get();
            System.out.println("🔧 FORCE CHECK PARCOURS pour: " + user.getEmail());

            // Forcer la vérification de tous les parcours de l'utilisateur
            parcoursService.forceCheckAllParcoursCompletion(user);

            return ResponseEntity.ok(Map.of(
                "message", "Vérification forcée des parcours terminée",
                "user", user.getEmail()
            ));

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification forcée: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}