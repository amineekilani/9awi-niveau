package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.LevelNotificationResponse;
import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.LevelNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/level-notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class LevelNotificationController {

    @Autowired
    private LevelNotificationService levelNotificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtenir toutes les notifications de niveau de l'utilisateur
     */
    @GetMapping
    public ResponseEntity<List<LevelNotificationResponse>> getUserLevelNotifications(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<LevelNotificationResponse> notifications = levelNotificationService.getUserLevelNotifications(user.get().getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des notifications de niveau: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtenir les notifications non lues
     */
    @GetMapping("/unread")
    public ResponseEntity<List<LevelNotificationResponse>> getUnreadLevelNotifications(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<LevelNotificationResponse> notifications = levelNotificationService.getUnreadLevelNotifications(user.get().getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des notifications non lues: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtenir les nouvelles notifications (pour les popups)
     */
    @GetMapping("/new")
    public ResponseEntity<List<LevelNotificationResponse>> getNewLevelNotifications(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<LevelNotificationResponse> notifications = levelNotificationService.getNewLevelNotifications(user.get().getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des nouvelles notifications: " + e.getMessage());
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

            long count = levelNotificationService.countUnreadLevelNotifications(user.get().getId());
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des notifications non lues: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compter les nouvelles notifications
     */
    @GetMapping("/new/count")
    public ResponseEntity<Map<String, Long>> getNewCount(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            long count = levelNotificationService.countNewLevelNotifications(user.get().getId());
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des nouvelles notifications: " + e.getMessage());
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

            levelNotificationService.markAsRead(id, user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Notification marquée comme lue"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage comme lu: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Marquer une notification comme vue (pour les popups)
     */
    @PutMapping("/{id}/viewed")
    public ResponseEntity<MessageResponse> markAsViewed(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            levelNotificationService.markAsViewed(id, user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Notification marquée comme vue"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage comme vue: " + e.getMessage());
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

            levelNotificationService.markAllAsRead(user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Toutes les notifications marquées comme lues"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage global comme lu: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}