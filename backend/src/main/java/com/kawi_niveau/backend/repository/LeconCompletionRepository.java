package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.LeconCompletion;
import com.kawi_niveau.backend.entity.Enrollment;
import com.kawi_niveau.backend.entity.Lecon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeconCompletionRepository extends JpaRepository<LeconCompletion, Long> {
    Optional<LeconCompletion> findByEnrollmentAndLecon(Enrollment enrollment, Lecon lecon);
    List<LeconCompletion> findByEnrollment(Enrollment enrollment);
    boolean existsByEnrollmentAndLecon(Enrollment enrollment, Lecon lecon);
    long countByEnrollment(Enrollment enrollment);
}
