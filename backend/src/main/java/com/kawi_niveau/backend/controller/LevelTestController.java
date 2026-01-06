package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/test/level")
@CrossOrigin(origins = "http://localhost:4200")
public class LevelTestController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Force la vérification des niveaux pour l'utilisateur connecté
     */
    @PostMapping("/force-check")
    public ResponseEntity<MessageResponse> forceCheckLevel(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            gamificationService.forceCheckLevelUp(user.get());
            return ResponseEntity.ok(new MessageResponse("Vérification des niveaux effectuée"));
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification forcée des niveaux: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new MessageResponse("Erreur lors de la vérification des niveaux"));
        }
    }

    /**
     * Ajoute des XP de test à l'utilisateur connecté
     */
    @PostMapping("/add-xp/{amount}")
    public ResponseEntity<MessageResponse> addTestXP(@PathVariable Integer amount, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            gamificationService.awardXP(user.get(), amount, "Test XP");
            return ResponseEntity.ok(new MessageResponse("+" + amount + " XP ajoutés"));
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout d'XP de test: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new MessageResponse("Erreur lors de l'ajout d'XP"));
        }
    }
}