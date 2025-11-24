package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByFormateurAndArchivedFalse(User formateur);
    List<Cours> findByFormateurOrderByCreatedAtDesc(User formateur);
    List<Cours> findByFormateur(User formateur);
    List<Cours> findByArchivedFalse();
}
