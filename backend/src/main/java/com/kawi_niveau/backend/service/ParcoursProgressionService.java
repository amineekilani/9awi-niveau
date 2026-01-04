package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcoursProgressionService {

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ParcoursValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Met à jour la progression d'un utilisateur dans tous ses parcours
     * Appelé quand l'utilisateur progresse dans un cours
     */
    public void updateProgressionParcours(User user, Cours cours) {
        // Trouver tous les parcours où ce cours est une étape
        List<ParcoursEtape> etapesAvecCeCours = etapeRepository.findByCours(cours);
        
        for (ParcoursEtape etape : etapesAvecCeCours) {
            ParcoursApprentissage parcours = etape.getParcours();
            
            // Vérifier si l'utilisateur est inscrit à ce parcours
            Optional<ParcoursInscription> inscriptionOpt = 
                    inscriptionRepository.findByParcoursAndUser(parcours, user);
            
            if (inscriptionOpt.isPresent()) {
                updateProgressionParcours(inscriptionOpt.get());
            }
        }
    }

    /**
     * Met à jour la progression d'une inscription spécifique
     */
    public void updateProgressionParcours(ParcoursInscription inscription) {
        ParcoursApprentissage parcours = inscription.getParcours();
        User user = inscription.getUser();
        
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        
        if (etapes.isEmpty()) {
            return;
        }

        int etapesCompletes = 0;
        int etapeCourante = 1;
        int pointsGagnes = 0;
        boolean parcoursComplete = true;

        // Calculer la progression
        for (ParcoursEtape etape : etapes) {
            boolean isComplete = validationService.isEtapeComplete(etape, user);
            
            if (isComplete) {
                etapesCompletes++;
                // Ajouter les points de l'étape (si définis)
                if (etape.getParcours().getPointsBonus() != null) {
                    pointsGagnes += etape.getParcours().getPointsBonus() / etapes.size();
                }
            } else {
                parcoursComplete = false;
                if (etapeCourante == etape.getOrdreEtape()) {
                    // Cette étape n'est pas complète, c'est l'étape courante
                    etapeCourante = etape.getOrdreEtape();
                }
            }
        }

        // Calculer le pourcentage de progression
        int progressionPourcentage = (etapesCompletes * 100) / etapes.size();

        // Mettre à jour l'inscription
        inscription.setProgressionPourcentage(progressionPourcentage);
        inscription.setEtapeCourante(etapeCourante);
        inscription.setPointsGagnes(pointsGagnes);
        inscription.setIsCompleted(parcoursComplete);

        if (parcoursComplete && inscription.getDateCompletion() == null) {
            inscription.setDateCompletion(LocalDateTime.now());
        }

        inscriptionRepository.save(inscription);
    }

    /**
     * Recalcule la progression de tous les parcours d'un utilisateur
     */
    public void recalculerProgressionUtilisateur(User user) {
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByUser(user);
        
        for (ParcoursInscription inscription : inscriptions) {
            updateProgressionParcours(inscription);
        }
    }

    /**
     * Recalcule la progression de tous les parcours d'un utilisateur par email
     */
    public void recalculerProgressionUtilisateur(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            recalculerProgressionUtilisateur(user);
        }
    }

    /**
     * Recalcule la progression de toutes les inscriptions d'un parcours
     */
    public void recalculerProgressionParcours(ParcoursApprentissage parcours) {
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByParcours(parcours);
        
        for (ParcoursInscription inscription : inscriptions) {
            updateProgressionParcours(inscription);
        }
    }

    /**
     * Vérifie si un utilisateur peut accéder à l'étape suivante
     */
    public boolean peutAccederEtapeSuivante(User user, ParcoursApprentissage parcours, int etapeActuelle) {
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        
        if (etapeActuelle >= etapes.size()) {
            return false; // Pas d'étape suivante
        }

        ParcoursEtape etapeSuivante = etapes.stream()
                .filter(e -> e.getOrdreEtape() == etapeActuelle + 1)
                .findFirst()
                .orElse(null);

        if (etapeSuivante == null) {
            return false;
        }

        List<ParcoursEtape> etapesPrecedentes = etapes.stream()
                .filter(e -> e.getOrdreEtape() < etapeSuivante.getOrdreEtape())
                .toList();

        return validationService.isEtapeDebloquee(etapeSuivante, user, etapesPrecedentes);
    }
}