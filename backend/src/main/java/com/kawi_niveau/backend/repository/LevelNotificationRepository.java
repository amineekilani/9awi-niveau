package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.LevelNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelNotificationRepository extends JpaRepository<LevelNotification, Long> {
    
    List<LevelNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<LevelNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<LevelNotification> findByUserIdAndIsNewTrueOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(ln) FROM LevelNotification ln WHERE ln.user.id = :userId AND ln.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ln) FROM LevelNotification ln WHERE ln.user.id = :userId AND ln.isNew = true")
    long countNewByUserId(@Param("userId") Long userId);
}