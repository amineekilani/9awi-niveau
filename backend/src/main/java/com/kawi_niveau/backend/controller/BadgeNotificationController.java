package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.entity.BadgeNotification;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.BadgeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/badge-notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class BadgeNotificationController {

    @Autowired
    private BadgeNotificationService badgeNotificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtenir les notifications non lues
     */
    @GetMapping("/unread")
    public ResponseEntity<List<BadgeNotification>> getUnreadBadgeNotifications(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> user = userRepository.findByEmail(email);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<BadgeNotification> notifications = badgeNotificationService.getUnreadBadgeNotifications(user.get().getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des notifications de badge non lues: " + e.getMessage());
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

            long count = badgeNotificationService.countUnreadBadgeNotifications(user.get().getId());
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des notifications de badge non lues: " + e.getMessage());
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

            badgeNotificationService.markAsRead(id, user.get().getId());
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

            badgeNotificationService.markAllAsRead(user.get().getId());
            return ResponseEntity.ok(new MessageResponse("Toutes les notifications marquées comme lues"));
        } catch (Exception e) {
            System.err.println("Erreur lors du marquage global comme lu: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}