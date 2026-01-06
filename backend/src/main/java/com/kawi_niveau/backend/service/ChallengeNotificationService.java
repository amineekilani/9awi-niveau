package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.ChallengeNotification;
import com.kawi_niveau.backend.repository.ChallengeNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChallengeNotificationService {

    @Autowired
    private ChallengeNotificationRepository challengeNotificationRepository;

    /**
     * Obtenir toutes les notifications de défi d'un utilisateur
     */
    public List<ChallengeNotification> getUserChallengeNotifications(Long userId) {
        return challengeNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Obtenir les notifications non lues
     */
    public List<ChallengeNotification> getUnreadChallengeNotifications(Long userId) {
        return challengeNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Obtenir les nouvelles notifications (pour les popups)
     */
    public List<ChallengeNotification> getNewChallengeNotifications(Long userId) {
        return challengeNotificationRepository.findByUserIdAndIsNewTrueOrderByCreatedAtDesc(userId);
    }

    /**
     * Compter les notifications non lues
     */
    public long countUnreadChallengeNotifications(Long userId) {
        return challengeNotificationRepository.countUnreadByUserId(userId);
    }

    /**
     * Compter les nouvelles notifications
     */
    public long countNewChallengeNotifications(Long userId) {
        return challengeNotificationRepository.countNewByUserId(userId);
    }

    /**
     * Marquer une notification comme lue
     */
    public void markAsRead(Long notificationId, Long userId) {
        Optional<ChallengeNotification> notification = challengeNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsRead(true);
            challengeNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer une notification comme vue (pour les popups)
     */
    public void markAsViewed(Long notificationId, Long userId) {
        Optional<ChallengeNotification> notification = challengeNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getId().equals(userId)) {
            notification.get().setIsNew(false);
            challengeNotificationRepository.save(notification.get());
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    public void markAllAsRead(Long userId) {
        List<ChallengeNotification> notifications = challengeNotificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(notification -> notification.setIsRead(true));
        challengeNotificationRepository.saveAll(notifications);
    }
}