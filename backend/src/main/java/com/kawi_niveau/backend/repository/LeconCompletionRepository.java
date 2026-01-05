package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.LeconCompletion;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.Lecon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeconCompletionRepository extends JpaRepository<LeconCompletion, Long> {
    Optional<LeconCompletion> findByEnrollmentAndLecon(Enrollment enrollment, Lecon lecon);
    List<LeconCompletion> findByEnrollment(Enrollment enrollment);
    boolean existsByEnrollmentAndLecon(Enrollment enrollment, Lecon lecon);
    long countByEnrollment(Enrollment enrollment);
    
    // Méthodes supplémentaires pour la progression des parcours
    @Query("SELECT l FROM Lecon l WHERE l.module.cours.id = :coursId")
    List<Lecon> findLeconsByCoursId(@Param("coursId") Long coursId);
    
    @Query("SELECT COUNT(lc) FROM LeconCompletion lc WHERE lc.enrollment.user.id = :userId AND lc.lecon.id = :leconId")
    long countByUserIdAndLeconId(@Param("userId") Long userId, @Param("leconId") Long leconId);
}
