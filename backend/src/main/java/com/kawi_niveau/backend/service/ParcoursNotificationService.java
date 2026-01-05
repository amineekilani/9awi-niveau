package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.ParcoursNotificationRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcoursNotificationService {

    @Autowired
    private ParcoursNotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Créer une notification de completion de parcours
     */
    public void createParcoursCompletionNotification(User user, ParcoursApprentissage parcours, 
                                                   Integer xpEarned, boolean certificateReady, 
                                                   String certificateUrl) {
        try {
            String title = "Parcours Terminé !";
            String message = String.format(
                "Félicitations ! Vous avez terminé le parcours \"%s\" et gagné %d points XP !",
                parcours.getTitre(),
                xpEarned != null ? xpEarned : 0
            );

            if (certificateReady) {
                message += " Votre certificat est prêt à être téléchargé !";
            }

            ParcoursNotification notification = new ParcoursNotification(
                user, parcours, ParcoursNotification.NotificationType.PARCOURS_COMPLETED,
                title, message, xpEarned
            );

            notification.setCertificateReady(certificateReady);
            notification.setCertificateUrl(certificateUrl);

            notificationRepository.save(notification);

            System.out.println("📢 Notification de parcours créée pour " + user.getEmail() + 
                             " - Parcours: " + parcours.getTitre());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de notification de parcours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Créer une notification de certificat prêt
     */
    public void createCertificateReadyNotification(User user, ParcoursApprentissage parcours, String certificateUrl) {
        try {
            String title = "Certificat Prêt !";
            String message = String.format(
                "Votre certificat pour le parcours \"%s\" est maintenant disponible au téléchargement !",
                parcours.getTitre()
            );

            ParcoursNotification notification = new ParcoursNotification(
                user, parcours, ParcoursNotification.NotificationType.CERTIFICATE_READY,
                title, message, null
            );

            notification.setCertificateReady(true);
            notification.setCertificateUrl(certificateUrl);

            notificationRepository.save(notification);

            System.out.println("📜 Notification de certificat créée pour " + user.getEmail() + 
                             " - Parcours: " + parcours.getTitre());

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de notification de certificat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtenir toutes les notifications d'un utilisateur
     */
    public List<ParcoursNotification> getUserNotifications(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        return notificationRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
    }

    /**
     * Obtenir les notifications non lues d'un utilisateur
     */
    public List<ParcoursNotification> getUnreadNotifications(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(userOpt.get());
    }

    /**
     * Compter les notifications non lues d'un utilisateur
     */
    public long getUnreadNotificationsCount(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return 0;
        }

        return notificationRepository.countByUserAndIsReadFalse(userOpt.get());
    }

    /**
     * Marquer une notification comme lue
     */
    public void markNotificationAsRead(Long notificationId, String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Optional<ParcoursNotification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            ParcoursNotification notification = notificationOpt.get();
            
            // Vérifier que la notification appartient à l'utilisateur
            if (notification.getUser().getId().equals(userOpt.get().getId())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Marquer toutes les notifications d'un utilisateur comme lues
     */
    public void markAllNotificationsAsRead(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        notificationRepository.markAllAsReadForUser(userOpt.get());
    }
}