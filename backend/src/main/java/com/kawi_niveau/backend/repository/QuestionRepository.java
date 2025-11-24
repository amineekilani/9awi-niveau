package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Question;
import com.kawi_niveau.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizOrderByOrdreAsc(Quiz quiz);
}
