package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.entity.ChallengeNotification;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.ChallengeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenge-notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class ChallengeNotificationController {

    @Autowired
    private ChallengeNotificationService challengeNotificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtenir les notifications non lues
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ChallengeNotification>> getUnreadChallengeNotifications(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<ChallengeNotification> notifications = challengeNotificationService.getUnreadChallengeNotifications(user.get().getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des notifications de défi non lues: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compter les notifications non lues
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            long count = challengeNotificationService.countUnreadChallengeNotifications(user.get().getId());
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des notifications de défi non lues: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            challengeNotificationService.markAsRead(id, user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Notification marquée comme lue"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage comme lu: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PutMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            challengeNotificationService.markAllAsRead(user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Toutes les notifications marquées comme lues"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage global comme lu: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}