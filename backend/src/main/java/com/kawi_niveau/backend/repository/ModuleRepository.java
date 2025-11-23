package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCoursOrderByOrdreAsc(Cours cours);
    List<Module> findByCoursIdOrderByOrdreAsc(Long coursId);
}
