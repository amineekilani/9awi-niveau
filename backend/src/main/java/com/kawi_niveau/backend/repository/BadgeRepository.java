package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Badge;
import com.kawi_niveau.backend.entity.BadgeCriteriaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    List<Badge> findByIsActiveTrue();
    
    List<Badge> findByCriteriaTypeAndIsActiveTrue(BadgeCriteriaType criteriaType);
    
    Page<Badge> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Badge b WHERE b.isActive = true")
    long countActiveBadges();
    
    boolean existsByName(String name);
}