package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcoursRepository extends JpaRepository<ParcoursApprentissage, Long> {
    
    // Trouver tous les parcours d'un formateur
    List<ParcoursApprentissage> findByFormateurOrderByCreatedAtDesc(User formateur);
    
    // Trouver les parcours publiés
    List<ParcoursApprentissage> findByIsPublishedTrueOrderByCreatedAtDesc();
    
    // Trouver les parcours par catégorie
    List<ParcoursApprentissage> findByCategorieAndIsPublishedTrueOrderByCreatedAtDesc(String categorie);
    
    // Trouver un parcours par ID et formateur (pour sécurité)
    Optional<ParcoursApprentissage> findByIdAndFormateur(Long id, User formateur);
    
    // Compter les parcours d'un formateur
    long countByFormateur(User formateur);
    
    // Compter les parcours publiés d'un formateur
    long countByFormateurAndIsPublishedTrue(User formateur);
    
    // Recherche par titre ou description
    @Query("SELECT p FROM ParcoursApprentissage p WHERE " +
           "(LOWER(p.titre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "p.isPublished = true ORDER BY p.createdAt DESC")
    List<ParcoursApprentissage> searchPublishedParcours(@Param("searchTerm") String searchTerm);
    
    // Parcours populaires (avec le plus d'inscriptions)
    @Query("SELECT p FROM ParcoursApprentissage p WHERE p.isPublished = true " +
           "ORDER BY SIZE(p.inscriptions) DESC")
    List<ParcoursApprentissage> findPopularParcours();
}