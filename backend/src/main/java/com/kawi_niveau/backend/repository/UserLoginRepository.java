package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {

    List<UserLogin> findByUserOrderByLoginTimeDesc(User user);

    Optional<UserLogin> findFirstByUserOrderByLoginTimeDesc(User user);

    @Query("SELECT COUNT(ul) FROM UserLogin ul WHERE ul.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT ul FROM UserLogin ul WHERE ul.user = :user AND ul.loginTime >= :startTime ORDER BY ul.loginTime DESC")
    List<UserLogin> findByUserAndLoginTimeAfter(@Param("user") User user, @Param("startTime") Long startTime);

    @Query("SELECT COUNT(DISTINCT DATE(FROM_UNIXTIME(ul.loginTime / 1000))) FROM UserLogin ul WHERE ul.user = :user AND ul.loginTime >= :startTime")
    long countDistinctDaysByUserAndLoginTimeAfter(@Param("user") User user, @Param("startTime") Long startTime);

    @Query("SELECT COUNT(ul) FROM UserLogin ul WHERE ul.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT ul FROM UserLogin ul WHERE ul.user.id = :userId AND ul.loginTime >= :startTime ORDER BY ul.loginTime DESC")
    List<UserLogin> findByUserIdAndLoginTimeAfter(@Param("userId") Long userId, @Param("startTime") Long startTime);

    @Query("SELECT COUNT(DISTINCT DATE(FROM_UNIXTIME(ul.loginTime / 1000))) FROM UserLogin ul WHERE ul.user.id = :userId AND ul.loginTime >= :startTime")
    long countDistinctDaysByUserIdAndLoginTimeAfter(@Param("userId") Long userId, @Param("startTime") Long startTime);
}