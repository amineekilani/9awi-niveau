package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.ParcoursNotification;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcoursNotificationRepository extends JpaRepository<ParcoursNotification, Long> {
    
    // Trouver toutes les notifications d'un utilisateur (les plus récentes en premier)
    List<ParcoursNotification> findByUserOrderByCreatedAtDesc(User user);
    
    // Trouver les notifications non lues d'un utilisateur
    List<ParcoursNotification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Compter les notifications non lues d'un utilisateur
    long countByUserAndIsReadFalse(User user);
    
    // Trouver les notifications récentes (dernières 10)
    @Query("SELECT n FROM ParcoursNotification n WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<ParcoursNotification> findRecentNotifications(@Param("user") User user);
    
    // Marquer toutes les notifications d'un utilisateur comme lues
    @Modifying
    @Query("UPDATE ParcoursNotification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user);
    
    // Vérifier si une notification existe déjà pour éviter les doublons
    @Query("SELECT COUNT(n) > 0 FROM ParcoursNotification n WHERE n.user = :user AND n.parcours = :parcours AND n.type = :type")
    boolean existsByUserAndParcoursAndType(@Param("user") User user, @Param("parcours") ParcoursApprentissage parcours, @Param("type") ParcoursNotification.NotificationType type);
}