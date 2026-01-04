package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.ParcoursInscription;
import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcoursInscriptionRepository extends JpaRepository<ParcoursInscription, Long> {
    
    // Trouver l'inscription d'un utilisateur à un parcours
    Optional<ParcoursInscription> findByParcoursAndUser(ParcoursApprentissage parcours, User user);
    
    // Trouver toutes les inscriptions d'un utilisateur
    List<ParcoursInscription> findByUserOrderByDateInscriptionDesc(User user);
    
    // Trouver les inscriptions actives d'un utilisateur (non terminées)
    List<ParcoursInscription> findByUserAndIsCompletedFalseOrderByDateInscriptionDesc(User user);
    
    // Trouver les inscriptions terminées d'un utilisateur
    List<ParcoursInscription> findByUserAndIsCompletedTrueOrderByDateCompletionDesc(User user);
    
    // Trouver toutes les inscriptions d'un parcours
    List<ParcoursInscription> findByParcoursOrderByDateInscriptionDesc(ParcoursApprentissage parcours);
    
    // Compter les inscriptions d'un parcours
    long countByParcours(ParcoursApprentissage parcours);
    
    // Compter les inscriptions terminées d'un parcours
    long countByParcoursAndIsCompletedTrue(ParcoursApprentissage parcours);
    
    // Vérifier si un utilisateur est inscrit à un parcours
    boolean existsByParcoursAndUser(ParcoursApprentissage parcours, User user);
    
    // Statistiques de progression moyenne d'un parcours
    @Query("SELECT AVG(pi.progressionPourcentage) FROM ParcoursInscription pi WHERE pi.parcours = :parcours")
    Double getAverageProgressionByParcours(@Param("parcours") ParcoursApprentissage parcours);
    
    // Trouver les inscriptions par étape courante
    List<ParcoursInscription> findByParcoursAndEtapeCourante(ParcoursApprentissage parcours, Integer etapeCourante);
}