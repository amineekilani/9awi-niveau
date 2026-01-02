package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ResultatQuiz;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultatQuizRepository extends JpaRepository<ResultatQuiz, Long> {
    List<ResultatQuiz> findByUserOrderByDatePassedDesc(User user);

    List<ResultatQuiz> findByQuizOrderByDatePassedDesc(Quiz quiz);

    List<ResultatQuiz> findByUserAndQuizOrderByDatePassedDesc(User user, Quiz quiz);

    Optional<ResultatQuiz> findFirstByUserAndQuizOrderByScoreDesc(User user, Quiz quiz);

    long countByUserAndScoreGreaterThanEqual(User user, Double score);

    @Query("SELECT COUNT(rq) FROM ResultatQuiz rq WHERE rq.user.id = :userId AND rq.score >= :score")
    long countByUserIdAndScoreGreaterThanEqual(@Param("userId") Long userId, @Param("score") Double score);
}
