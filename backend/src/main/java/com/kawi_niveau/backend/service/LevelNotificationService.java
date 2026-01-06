package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.LevelNotificationResponse;
import com.kawi_niveau.backend.entity.Level;
import com.kawi_niveau.backend.entity.LevelNotification;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.LevelNotificationRepository;
import com.kawi_niveau.backend.repository.LevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class LevelNotificationService {

    @Autowired
    private LevelNotificationRepository levelNotificationRepository;

    @Autowired
    private LevelRepository levelRepository;

    /**
     * Créer une notification de montée de niveau
     */
    public void createLevelUpNotification(User user, Integer oldLevel, Integer newLevel, Integer totalXP, Integer xpGained) {
        try {
            // Récupérer les informations du nouveau niveau
            Optional<Level> levelInfo = levelRepository.findByLevel(newLevel);
            String levelName = levelInfo.map(Level::getName).orElse("Niveau " + newLevel);

            LevelNotification notification = new LevelNotification();
            notification.setUser(user);
            notification.setOldLevel(oldLevel);
            notification.setNewLevel(newLevel);
            notification.setLevelName(levelName);
            notification.setTotalXP(totalXP);
            notification.setXpGained(xpGained);
            notification.setIsRead(false);
            notification.setIsNew(true);

            levelNotificationRepository.save(notification);

            System.out.println("✅ Notification de niveau créée: " + user.getEmail() + " - Niveau " + oldLevel + " → " + newLevel);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de notification de niveau: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtenir toutes les notifications de niveau d'un utilisateur
     */
    public List<LevelNotificationResponse> getUserLevelNotifications(Long userId) {
        List<LevelNotification> notifications = levelNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les notifications non lues
     */
    public List<LevelNotificationResponse> getUnreadLevelNotifications(Long userId) {
        List<LevelNotification> notifications = levelNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les nouvelles notifications (pour les popups)
     */
    public List<LevelNotificationResponse> getNewLevelNotifications(Long userId) {
        List<LevelNotification> notifications = levelNotificationRepository.findByUserIdAndIsNewTrueOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Compter les notifications non lues
     */
    public long countUnreadLevelNotifications(Long userId) {
        return levelNotificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Compter les nouvelles notifications
     */
    public long countNewLevelNotifications(Long userId) {
        return levelNotificationRepository.countNewByUserId(userId);
    }

    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(Long notificationId, Long userId) {
        Optional<LevelNotification> notification = levelNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsRead(true);
            levelNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer une notification comme vue (pour les popups)
     */
    public void markAsViewed(Long notificationId, Long userId) {
        Optional<LevelNotification> notification = levelNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsNew(false);
            levelNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    public void markAllAsRead(Long userId) {
        List<LevelNotification> notifications = levelNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        levelNotificationRepository.saveAll(notifications);
    }

    /**
     * Convertir en DTO de réponse
     */
    private LevelNotificationResponse convertToResponse(LevelNotification notification) {
        LevelNotificationResponse response = new LevelNotificationResponse();
        response.setId(notification.getId());
        response.setOldLevel(notification.getOldLevel());
        response.setNewLevel(notification.getNewLevel());
        response.setLevelName(notification.getLevelName());
        response.setTotalXP(notification.getTotalXP());
        response.setXpGained(notification.getXpGained());
        response.setIsRead(notification.getIsRead());
        response.setIsNew(notification.getIsNew());
        response.setCreatedAt(notification.getCreatedAt());
        response.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
        return response;
    }

    /**
     * Calculer le temps écoulé
     */
    private String calculateTimeAgo(Long timestamp) {
        if (timestamp == null) return "Inconnu";
        
        Instant now = Instant.now();
        Instant notificationTime = Instant.ofEpochMilli(timestamp);
        
        long minutes = ChronoUnit.MINUTES.between(notificationTime, now);
        long hours = ChronoUnit.HOURS.between(notificationTime, now);
        long days = ChronoUnit.DAYS.between(notificationTime, now);
        
        if (minutes < 1) {
            return "À l'instant";
        } else if (minutes < 60) {
            return minutes + " min";
        } else if (hours < 24) {
            return hours + " h";
        } else if (days < 7) {
            return days + " j";
        } else {
            return days / 7 + " sem";
        }
    }
}