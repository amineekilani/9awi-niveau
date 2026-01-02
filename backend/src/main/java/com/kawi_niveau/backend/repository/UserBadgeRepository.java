package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Badge;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    boolean existsByUserAndBadge(User user, Badge badge);
    
    List<UserBadge> findByUser(User user);
    
    List<UserBadge> findByBadge(Badge badge);
    
    long countByUser(User user);
    
    long countByBadge(Badge badge);
    
    @Query("SELECT COUNT(ub) FROM UserBadge ub")
    long countTotalBadgesEarned();
}