package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ExerciceElement;
import com.kawi_niveau.backend.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciceElementRepository extends JpaRepository<ExerciceElement, Long> {
    List<ExerciceElement> findByExerciceOrderByPositionOrdreAsc(Exercice exercice);
}