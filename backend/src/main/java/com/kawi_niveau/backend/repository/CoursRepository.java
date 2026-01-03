package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {
    List<Cours> findByFormateurAndArchivedFalse(User formateur);

    List<Cours> findByFormateurOrderByCreatedAtDesc(User formateur);

    List<Cours> findByFormateur(User formateur);

    List<Cours> findByArchivedFalse();

    // Méthodes pour les niveaux de difficulté
    List<Cours> findByNiveauDifficulteAndArchivedFalse(NiveauDifficulte niveau);
    
    List<Cours> findByCategorieAndArchivedFalse(String categorie);
    
    List<Cours> findByCategorieAndNiveauDifficulteAndArchivedFalse(String categorie, NiveauDifficulte niveau);

    @Query("SELECT DISTINCT c.categorie FROM Cours c WHERE c.categorie IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT c FROM Cours c WHERE c.archived = false AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.keywords, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCours(@Param("keyword") String keyword);

    // Recherches avec filtres combinés
    @Query("SELECT c FROM Cours c WHERE c.archived = false AND " +
            "c.categorie = :categorie AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.keywords, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCoursWithKeywordAndCategory(@Param("keyword") String keyword, @Param("categorie") String categorie);

    @Query("SELECT c FROM Cours c WHERE c.archived = false AND " +
            "c.niveauDifficulte = :niveau AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.keywords, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCoursWithKeywordAndNiveau(@Param("keyword") String keyword, @Param("niveau") NiveauDifficulte niveau);

    @Query("SELECT c FROM Cours c WHERE c.archived = false AND " +
            "c.categorie = :categorie AND " +
            "c.niveauDifficulte = :niveau AND " +
            "(LOWER(c.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(c.keywords, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Cours> searchCoursWithAllFilters(@Param("keyword") String keyword, @Param("categorie") String categorie, @Param("niveau") NiveauDifficulte niveau);
}
