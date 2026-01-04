package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ParcoursEtape;
import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.Cours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcoursEtapeRepository extends JpaRepository<ParcoursEtape, Long> {
    
    // Trouver toutes les étapes d'un parcours ordonnées
    List<ParcoursEtape> findByParcoursOrderByOrdreEtape(ParcoursApprentissage parcours);
    
    // Trouver toutes les étapes qui utilisent un cours spécifique
    List<ParcoursEtape> findByCours(Cours cours);
    
    // Trouver une étape par parcours et ordre
    Optional<ParcoursEtape> findByParcoursAndOrdreEtape(ParcoursApprentissage parcours, Integer ordreEtape);
    
    // Trouver les étapes d'un niveau spécifique
    List<ParcoursEtape> findByParcoursAndNiveauEtapeOrderByOrdreEtape(ParcoursApprentissage parcours, Integer niveauEtape);
    
    // Compter les étapes d'un parcours
    long countByParcours(ParcoursApprentissage parcours);
    
    // Compter les étapes obligatoires d'un parcours
    long countByParcoursAndIsObligatoireTrue(ParcoursApprentissage parcours);
    
    // Supprimer toutes les étapes d'un parcours
    @Modifying
    @Transactional
    void deleteByParcours(ParcoursApprentissage parcours);
    
    // Trouver la prochaine étape après un ordre donné
    @Query("SELECT e FROM ParcoursEtape e WHERE e.parcours = :parcours AND e.ordreEtape > :ordreActuel " +
           "ORDER BY e.ordreEtape ASC")
    List<ParcoursEtape> findNextEtapes(@Param("parcours") ParcoursApprentissage parcours, 
                                       @Param("ordreActuel") Integer ordreActuel);
    
    // Trouver les étapes précédentes
    @Query("SELECT e FROM ParcoursEtape e WHERE e.parcours = :parcours AND e.ordreEtape < :ordreActuel " +
           "ORDER BY e.ordreEtape DESC")
    List<ParcoursEtape> findPreviousEtapes(@Param("parcours") ParcoursApprentissage parcours, 
                                           @Param("ordreActuel") Integer ordreActuel);
}