package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.BadgeNotification;
import com.kawi_niveau.backend.repository.BadgeNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BadgeNotificationService {

    @Autowired
    private BadgeNotificationRepository badgeNotificationRepository;

    /**
     * Obtenir toutes les notifications de badge d'un utilisateur
     */
    public List<BadgeNotification> getUserBadgeNotifications(Long userId) {
        return badgeNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Obtenir les notifications non lues
     */
    public List<BadgeNotification> getUnreadBadgeNotifications(Long userId) {
        return badgeNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Obtenir les nouvelles notifications (pour les popups)
     */
    public List<BadgeNotification> getNewBadgeNotifications(Long userId) {
        return badgeNotificationRepository.findByUserIdAndIsNewTrueOrderByCreatedAtDesc(userId);
    }

    /**
     * Compter les notifications non lues
     */
    public long countUnreadBadgeNotifications(Long userId) {
        return badgeNotificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Compter les nouvelles notifications
     */
    public long countNewBadgeNotifications(Long userId) {
        return badgeNotificationRepository.countNewByUserId(userId);
    }

    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(Long notificationId, Long userId) {
        Optional<BadgeNotification> notification = badgeNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsRead(true);
            badgeNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer une notification comme vue (pour les popups)
     */
    public void markAsViewed(Long notificationId, Long userId) {
        Optional<BadgeNotification> notification = badgeNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsNew(false);
            badgeNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    public void markAllAsRead(Long userId) {
        List<BadgeNotification> notifications = badgeNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        badgeNotificationRepository.saveAll(notifications);
    }
}