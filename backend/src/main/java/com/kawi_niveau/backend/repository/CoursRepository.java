package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByFormateurAndArchivedFalse(User formateur);

    List<Cours> findByFormateurOrderByCreatedAtDesc(User formateur);

    List<Cours> findByFormateur(User formateur);

    List<Cours> findByArchivedFalse();

    @Query("SELECT DISTINCT c.categorie FROM Cours c WHERE c.categorie IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT c FROM Cours c WHERE c.archived = false AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.keywords, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCours(String keyword);
}
