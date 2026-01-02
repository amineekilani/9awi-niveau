package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserXP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserXPRepository extends JpaRepository<UserXP, Long> {
    
    @Query("SELECT ux FROM UserXP ux WHERE ux.user = :user")
    List<UserXP> findAllByUser(@Param("user") User user);
    
    default Optional<UserXP> findByUser(User user) {
        List<UserXP> results = findAllByUser(user);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        // Si plusieurs résultats, prendre le premier (ou le plus récent)
        return Optional.of(results.get(0));
    }
    
    @Query("SELECT ux FROM UserXP ux ORDER BY ux.totalXP DESC")
    List<UserXP> findAllOrderByTotalXPDesc();
    
    @Query("SELECT ux FROM UserXP ux ORDER BY ux.totalXP DESC")
    Page<UserXP> findAllOrderByTotalXPDesc(Pageable pageable);
    
    @Query("SELECT AVG(ux.totalXP) FROM UserXP ux")
    Double getAverageXP();
    
    @Query("SELECT SUM(ux.totalXP) FROM UserXP ux")
    Long findTotalXPAwarded();
}