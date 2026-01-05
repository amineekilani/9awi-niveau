package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ParcoursNotificationResponse;
import com.kawi_niveau.backend.entity.ParcoursNotification;
import com.kawi_niveau.backend.service.ParcoursNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parcours-notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursNotificationController {

    @Autowired
    private ParcoursNotificationService notificationService;

    /**
     * Obtenir toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<List<ParcoursNotificationResponse>> getUserNotifications(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursNotification> notifications = notificationService.getUserNotifications(userEmail);
            List<ParcoursNotificationResponse> response = notifications.stream()
                .map(ParcoursNotificationResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir les notifications non lues de l'utilisateur connecté
     */
    @GetMapping("/unread")
    public ResponseEntity<List<ParcoursNotificationResponse>> getUnreadNotifications(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursNotification> notifications = notificationService.getUnreadNotifications(userEmail);
            List<ParcoursNotificationResponse> response = notifications.stream()
                .map(ParcoursNotificationResponse::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir le nombre de notifications non lues
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationsCount(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            long count = notificationService.getUnreadNotificationsCount(userEmail);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marquer une notification comme lue
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId, 
                                                      Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            notificationService.markNotificationAsRead(notificationId, userEmail);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            notificationService.markAllNotificationsAsRead(userEmail);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}