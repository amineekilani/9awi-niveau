package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Challenge;
import com.kawi_niveau.backend.entity.ChallengeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    
    List<Challenge> findByIsActiveTrue();
    
    List<Challenge> findByChallengeTypeAndIsActiveTrue(ChallengeType challengeType);
    
    Page<Challenge> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Challenge c WHERE c.isActive = true")
    long countActiveChallenges();
    
    @Query("SELECT c FROM Challenge c WHERE c.isActive = true AND (c.endDate IS NULL OR c.endDate > :currentTime)")
    List<Challenge> findActiveAndNotExpired(Long currentTime);
    
    @Query("SELECT c FROM Challenge c WHERE c.isActive = true AND c.endDate > :currentTime")
    List<Challenge> findByIsActiveTrueAndEndDateAfter(Long currentTime);
    
    boolean existsByName(String name);
}