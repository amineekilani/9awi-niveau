package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.BadgeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeNotificationRepository extends JpaRepository<BadgeNotification, Long> {
    
    List<BadgeNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<BadgeNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<BadgeNotification> findByUserIdAndIsNewTrueOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(bn) FROM BadgeNotification bn WHERE bn.user.id = :userId AND bn.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(bn) FROM BadgeNotification bn WHERE bn.user.id = :userId AND bn.isNew = true")
    long countNewByUserId(@Param("userId") Long userId);
}