package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ParcoursEtapeRequest;
import com.kawi_niveau.backend.dto.ParcoursEtapeResponse;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParcoursEtapeService {

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcoursValidationService validationService;

    // Ajouter une étape à un parcours
    @Transactional
    public ParcoursEtapeResponse addEtape(Long parcoursId, ParcoursEtapeRequest request, String formateurEmail) {
        return addEtapeToParcours(parcoursId, request, formateurEmail);
    }

    // Obtenir une étape par ID
    public ParcoursEtapeResponse getEtapeById(Long etapeId, String userEmail) {
        ParcoursEtape etape = etapeRepository.findById(etapeId)
                .orElseThrow(() -> new RuntimeException("Étape non trouvée"));

        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        List<ParcoursEtape> toutesEtapes = etapeRepository.findByParcoursOrderByOrdreEtape(etape.getParcours());
        
        return convertToResponseWithValidation(etape, user, toutesEtapes);
    }

    // Ajouter une étape à un parcours
    @Transactional
    public ParcoursEtapeResponse addEtapeToParcours(Long parcoursId, ParcoursEtapeRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        Cours cours = coursRepository.findById(request.getCoursId())
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Vérifier que le cours appartient au formateur
        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous ne pouvez ajouter que vos propres cours au parcours");
        }

        // Vérifier que le cours n'est pas déjà dans le parcours
        boolean coursDejaPresent = etapeRepository.findByParcoursOrderByOrdreEtape(parcours)
                .stream()
                .anyMatch(etape -> etape.getCours().getId().equals(cours.getId()));

        if (coursDejaPresent) {
            throw new RuntimeException("Ce cours est déjà présent dans le parcours");
        }

        // Décaler les étapes existantes si nécessaire
        List<ParcoursEtape> etapesExistantes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        int nouvelOrdre = request.getOrdreEtape();
        
        // Décaler toutes les étapes qui ont un ordre >= au nouvel ordre
        for (ParcoursEtape etapeExistante : etapesExistantes) {
            if (etapeExistante.getOrdreEtape() >= nouvelOrdre) {
                etapeExistante.setOrdreEtape(etapeExistante.getOrdreEtape() + 1);
                etapeRepository.save(etapeExistante);
            }
        }

        ParcoursEtape etape = new ParcoursEtape();
        etape.setParcours(parcours);
        etape.setCours(cours);
        etape.setOrdreEtape(nouvelOrdre);
        etape.setNiveauEtape(request.getNiveauEtape());
        etape.setIsObligatoire(request.getIsObligatoire());
        etape.setScoreMinimum(request.getScoreMinimum());
        etape.setPourcentageCompletionRequis(request.getPourcentageCompletionRequis());
        etape.setQuizObligatoires(request.getQuizObligatoires());
        etape.setDescription(request.getDescription());

        etape = etapeRepository.save(etape);

        return convertToResponse(etape);
    }

    // Obtenir toutes les étapes d'un parcours avec validation
    public List<ParcoursEtapeResponse> getEtapesByParcours(Long parcoursId, String userEmail) {
        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);

        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        final User finalUser = user;
        return etapes.stream()
                .map(etape -> convertToResponseWithValidation(etape, finalUser, etapes))
                .collect(Collectors.toList());
    }

    // Mettre à jour une étape
    @Transactional
    public ParcoursEtapeResponse updateEtape(Long etapeId, ParcoursEtapeRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursEtape etape = etapeRepository.findById(etapeId)
                .orElseThrow(() -> new RuntimeException("Étape non trouvée"));

        // Vérifier que le formateur est propriétaire du parcours
        if (!etape.getParcours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette étape");
        }

        // Si le cours change, vérifier qu'il appartient au formateur
        if (!etape.getCours().getId().equals(request.getCoursId())) {
            Cours nouveauCours = coursRepository.findById(request.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

            if (!nouveauCours.getFormateur().getId().equals(formateur.getId())) {
                throw new RuntimeException("Vous ne pouvez utiliser que vos propres cours");
            }

            etape.setCours(nouveauCours);
        }

        // Si l'ordre change, gérer le décalage des autres étapes
        if (!etape.getOrdreEtape().equals(request.getOrdreEtape())) {
            int ancienOrdre = etape.getOrdreEtape();
            int nouvelOrdre = request.getOrdreEtape();
            
            List<ParcoursEtape> autresEtapes = etapeRepository.findByParcoursOrderByOrdreEtape(etape.getParcours())
                    .stream()
                    .filter(e -> !e.getId().equals(etapeId))
                    .collect(Collectors.toList());
            
            // Décaler les autres étapes selon le mouvement
            for (ParcoursEtape autreEtape : autresEtapes) {
                int ordreActuel = autreEtape.getOrdreEtape();
                
                if (nouvelOrdre > ancienOrdre) {
                    // Déplacement vers le bas : décaler vers le haut les étapes entre ancienOrdre et nouvelOrdre
                    if (ordreActuel > ancienOrdre && ordreActuel <= nouvelOrdre) {
                        autreEtape.setOrdreEtape(ordreActuel - 1);
                        etapeRepository.save(autreEtape);
                    }
                } else {
                    // Déplacement vers le haut : décaler vers le bas les étapes entre nouvelOrdre et ancienOrdre
                    if (ordreActuel >= nouvelOrdre && ordreActuel < ancienOrdre) {
                        autreEtape.setOrdreEtape(ordreActuel + 1);
                        etapeRepository.save(autreEtape);
                    }
                }
            }
            
            etape.setOrdreEtape(nouvelOrdre);
        }

        etape.setNiveauEtape(request.getNiveauEtape());
        etape.setIsObligatoire(request.getIsObligatoire());
        etape.setScoreMinimum(request.getScoreMinimum());
        etape.setPourcentageCompletionRequis(request.getPourcentageCompletionRequis());
        etape.setQuizObligatoires(request.getQuizObligatoires());
        etape.setDescription(request.getDescription());

        etape = etapeRepository.save(etape);

        return convertToResponse(etape);
    }

    // Supprimer une étape
    @Transactional
    public void deleteEtape(Long etapeId, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursEtape etape = etapeRepository.findById(etapeId)
                .orElseThrow(() -> new RuntimeException("Étape non trouvée"));

        // Vérifier que le formateur est propriétaire du parcours
        if (!etape.getParcours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette étape");
        }

        int ordreSupprimer = etape.getOrdreEtape();
        ParcoursApprentissage parcours = etape.getParcours();

        // Supprimer l'étape d'abord
        etapeRepository.delete(etape);
        etapeRepository.flush(); // Forcer la suppression immédiate

        // Récupérer les étapes à décaler après la suppression
        List<ParcoursEtape> etapesADecaler = etapeRepository.findByParcoursOrderByOrdreEtape(parcours)
                .stream()
                .filter(e -> e.getOrdreEtape() > ordreSupprimer)
                .collect(Collectors.toList());

        // Décaler les étapes une par une
        for (ParcoursEtape etapeADecaler : etapesADecaler) {
            etapeADecaler.setOrdreEtape(etapeADecaler.getOrdreEtape() - 1);
            etapeRepository.save(etapeADecaler);
        }
        
        // Forcer le commit de toutes les modifications
        etapeRepository.flush();
    }

    // Réorganiser les étapes (drag & drop)
    public List<ParcoursEtapeResponse> reorderEtapes(Long parcoursId, List<Long> nouvelOrdre, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);

        // Vérifier que tous les IDs fournis correspondent aux étapes existantes
        if (etapes.size() != nouvelOrdre.size() || 
            !etapes.stream().map(ParcoursEtape::getId).collect(Collectors.toSet())
                    .equals(nouvelOrdre.stream().collect(Collectors.toSet()))) {
            throw new RuntimeException("Liste d'étapes invalide");
        }

        // Réorganiser les étapes
        for (int i = 0; i < nouvelOrdre.size(); i++) {
            Long etapeId = nouvelOrdre.get(i);
            ParcoursEtape etape = etapes.stream()
                    .filter(e -> e.getId().equals(etapeId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Étape non trouvée: " + etapeId));
            
            etape.setOrdreEtape(i + 1);
            etapeRepository.save(etape);
        }

        // Retourner les étapes réorganisées
        return getEtapesByParcours(parcoursId, formateurEmail);
    }

    // Convertir une étape en réponse
    private ParcoursEtapeResponse convertToResponse(ParcoursEtape etape) {
        return convertToResponseWithValidation(etape, null, null);
    }

    // Convertir une étape en réponse avec validation
    private ParcoursEtapeResponse convertToResponseWithValidation(ParcoursEtape etape, User user, List<ParcoursEtape> toutesEtapes) {
        ParcoursEtapeResponse response = new ParcoursEtapeResponse();
        
        response.setId(etape.getId());
        response.setCoursId(etape.getCours().getId());
        response.setCoursTitle(etape.getCours().getTitre());
        response.setCoursDescription(etape.getCours().getDescription());
        response.setCoursThumbnailUrl(etape.getCours().getThumbnailUrl());
        response.setCoursNiveauDifficulte(etape.getCours().getNiveauDifficulte());
        response.setCoursCategorie(etape.getCours().getCategorie());
        response.setOrdreEtape(etape.getOrdreEtape());
        response.setNiveauEtape(etape.getNiveauEtape());
        response.setIsObligatoire(etape.getIsObligatoire());
        response.setScoreMinimum(etape.getScoreMinimum());
        response.setPourcentageCompletionRequis(etape.getPourcentageCompletionRequis());
        response.setQuizObligatoires(etape.getQuizObligatoires());
        response.setDescription(etape.getDescription());
        response.setCreatedAt(etape.getCreatedAt());

        // Validation et progression si utilisateur connecté
        if (user != null && toutesEtapes != null) {
            // Récupérer les étapes précédentes pour la validation linéaire
            List<ParcoursEtape> etapesPrecedentes = toutesEtapes.stream()
                    .filter(e -> e.getOrdreEtape() < etape.getOrdreEtape())
                    .collect(Collectors.toList());

            // Valider l'étape
            boolean isDebloque = validationService.isEtapeDebloquee(etape, user, etapesPrecedentes);
            boolean isComplete = validationService.isEtapeComplete(etape, user);
            
            response.setIsDebloque(isDebloque);
            response.setIsComplete(isComplete);

            // Récupérer les données de progression
            ParcoursValidationService.EtapeValidationResult validation = 
                    validationService.validateEtapeConditions(etape, user);
            
            response.setProgressionCours((int) Math.round(validation.getProgressionCours()));
            response.setScoreObtenu((int) Math.round(validation.getScoreObtenu()));
        } else {
            // Pas d'utilisateur connecté, valeurs par défaut
            response.setIsDebloque(false);
            response.setIsComplete(false);
            response.setProgressionCours(0);
            response.setScoreObtenu(0);
        }

        return response;
    }
}