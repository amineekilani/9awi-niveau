package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Lecon;
import com.kawi_niveau.backend.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeconRepository extends JpaRepository<Lecon, Long> {
    List<Lecon> findByModuleOrderByOrdreAsc(Module module);
    List<Lecon> findByModuleIdOrderByOrdreAsc(Long moduleId);
}
