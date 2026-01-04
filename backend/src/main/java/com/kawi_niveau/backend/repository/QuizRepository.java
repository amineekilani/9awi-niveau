package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Module;
import com.kawi_niveau.backend.entity.Quiz;
import com.kawi_niveau.backend.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByModule(Module module);
    Optional<Quiz> findByModuleId(Long moduleId);
    
    @Query("SELECT q FROM Quiz q JOIN q.module m WHERE m.cours = :cours")
    List<Quiz> findByCours(@Param("cours") Cours cours);
}
