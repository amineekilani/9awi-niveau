package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ChallengeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeNotificationRepository extends JpaRepository<ChallengeNotification, Long> {
    
    List<ChallengeNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<ChallengeNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<ChallengeNotification> findByUserIdAndIsNewTrueOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(cn) FROM ChallengeNotification cn WHERE cn.user.id = :userId AND cn.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(cn) FROM ChallengeNotification cn WHERE cn.user.id = :userId AND cn.isNew = true")
    long countNewByUserId(@Param("userId") Long userId);
}