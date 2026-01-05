package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    
    Optional<Level> findByLevel(Integer level);
    
    @Query("SELECT l FROM Level l WHERE l.xpRequired <= :xp ORDER BY l.level DESC")
    List<Level> findLevelsForXP(@Param("xp") Integer xp);
    
    @Query("SELECT l FROM Level l WHERE l.xpRequired > :currentXp ORDER BY l.level ASC LIMIT 1")
    Optional<Level> findNextLevel(@Param("currentXp") Integer currentXp);
    
    @Query("SELECT l FROM Level l ORDER BY l.level ASC")
    List<Level> findAllOrderByLevel();
}