package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByUser(User user);
    Optional<UserPreferences> findByUserId(Long userId);
    boolean existsByUser(User user);
}