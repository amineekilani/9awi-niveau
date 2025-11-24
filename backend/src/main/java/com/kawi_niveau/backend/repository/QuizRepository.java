package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Module;
import com.kawi_niveau.backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByModule(Module module);
    Optional<Quiz> findByModuleId(Long moduleId);
}
