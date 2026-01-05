package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ResultatExercice;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultatExerciceRepository extends JpaRepository<ResultatExercice, Long> {
    List<ResultatExercice> findByUserOrderByDatePassedDesc(User user);
    List<ResultatExercice> findByExerciceOrderByDatePassedDesc(Exercice exercice);
    List<ResultatExercice> findByUserAndExerciceOrderByDatePassedDesc(User user, Exercice exercice);
    Optional<ResultatExercice> findFirstByUserAndExerciceOrderByScoreDesc(User user, Exercice exercice);
    List<ResultatExercice> findByUserIdAndExerciceIdOrderByDatePassedDesc(Long userId, Long exerciceId);
}