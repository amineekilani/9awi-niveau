package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    Optional<Exercice> findByModule(com.kawi_niveau.backend.entity.Module module);
    Optional<Exercice> findByModuleId(Long moduleId);
}