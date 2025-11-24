package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ResultatQuiz;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultatQuizRepository extends JpaRepository<ResultatQuiz, Long> {
    List<ResultatQuiz> findByUserOrderByDatePassedDesc(User user);
    List<ResultatQuiz> findByQuizOrderByDatePassedDesc(Quiz quiz);
    List<ResultatQuiz> findByUserAndQuizOrderByDatePassedDesc(User user, Quiz quiz);
    Optional<ResultatQuiz> findFirstByUserAndQuizOrderByScoreDesc(User user, Quiz quiz);
}
