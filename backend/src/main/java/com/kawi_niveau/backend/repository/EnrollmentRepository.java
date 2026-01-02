package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserAndCours(User user, Cours cours);
    List<Enrollment> findByUser(User user);
    List<Enrollment> findByCours(Cours cours);
    boolean existsByUserAndCours(User user, Cours cours);
    long countByUserAndProgressGreaterThanEqual(User user, Float progress);
}
