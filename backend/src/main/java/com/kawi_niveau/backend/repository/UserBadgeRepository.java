package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Badge;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    boolean existsByUserAndBadge(User user, Badge badge);

    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END FROM UserBadge ub WHERE ub.user.id = :userId AND ub.badge.id = :badgeId")
    boolean existsByUserIdAndBadgeId(@Param("userId") Long userId, @Param("badgeId") Long badgeId);

    List<UserBadge> findByUser(User user);

    List<UserBadge> findByBadge(Badge badge);

    long countByUser(User user);

    long countByBadge(Badge badge);

    @Query("SELECT COUNT(ub) FROM UserBadge ub")
    long countTotalBadgesEarned();

    @Query("SELECT ub FROM UserBadge ub WHERE ub.user.id = :userId")
    List<UserBadge> findByUserId(Long userId);

    @Query("SELECT COUNT(ub) FROM UserBadge ub WHERE ub.user.id = :userId")
    long countByUserId(Long userId);
}