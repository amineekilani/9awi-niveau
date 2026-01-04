package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParcoursService {

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoursRepository coursRepository;

    // Créer un nouveau parcours
    public ParcoursResponse createParcours(ParcoursRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        if (formateur.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Seuls les formateurs peuvent créer des parcours");
        }

        ParcoursApprentissage parcours = new ParcoursApprentissage();
        parcours.setTitre(request.getTitre());
        parcours.setDescription(request.getDescription());
        parcours.setThumbnailUrl(request.getThumbnailUrl());
        parcours.setCategorie(request.getCategorie());
        parcours.setNiveauDifficulte(request.getNiveauDifficulte());
        parcours.setDureeEstimeeHeures(request.getDureeEstimeeHeures());
        parcours.setPrerequis(request.getPrerequis());
        parcours.setTypeParcours(request.getTypeParcours());
        parcours.setPointsBonus(request.getPointsBonus());
        parcours.setBadgeCompletion(request.getBadgeCompletion());
        parcours.setCertificatEnabled(request.getCertificatEnabled());
        parcours.setIsPublished(request.getIsPublished());
        parcours.setFormateur(formateur);

        parcours = parcoursRepository.save(parcours);

        // Créer les étapes si fournies
        if (request.getEtapes() != null && !request.getEtapes().isEmpty()) {
            createEtapes(parcours, request.getEtapes());
        }

        return convertToResponse(parcours, formateurEmail);
    }

    // Mettre à jour un parcours
    public ParcoursResponse updateParcours(Long parcoursId, ParcoursRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        parcours.setTitre(request.getTitre());
        parcours.setDescription(request.getDescription());
        parcours.setThumbnailUrl(request.getThumbnailUrl());
        parcours.setCategorie(request.getCategorie());
        parcours.setNiveauDifficulte(request.getNiveauDifficulte());
        parcours.setDureeEstimeeHeures(request.getDureeEstimeeHeures());
        parcours.setPrerequis(request.getPrerequis());
        parcours.setTypeParcours(request.getTypeParcours());
        parcours.setPointsBonus(request.getPointsBonus());
        parcours.setBadgeCompletion(request.getBadgeCompletion());
        parcours.setCertificatEnabled(request.getCertificatEnabled());
        parcours.setIsPublished(request.getIsPublished());

        parcours = parcoursRepository.save(parcours);

        return convertToResponse(parcours, formateurEmail);
    }

    // Obtenir tous les parcours d'un formateur
    public List<ParcoursResponse> getMesParcours(String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        List<ParcoursApprentissage> parcours = parcoursRepository.findByFormateurOrderByCreatedAtDesc(formateur);
        
        return parcours.stream()
                .map(p -> convertToResponse(p, formateurEmail))
                .collect(Collectors.toList());
    }

    // Obtenir un parcours par ID
    public ParcoursResponse getParcoursById(Long parcoursId, String userEmail) {
        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        return convertToResponse(parcours, userEmail);
    }

    // Supprimer un parcours
    @Transactional
    public void deleteParcours(Long parcoursId, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        // Vérifier s'il y a des inscriptions actives
        long inscriptionsActives = inscriptionRepository.countByParcours(parcours) - 
                                   inscriptionRepository.countByParcoursAndIsCompletedTrue(parcours);
        if (inscriptionsActives > 0) {
            throw new RuntimeException("Impossible de supprimer un parcours avec des inscriptions actives");
        }

        // Supprimer explicitement les étapes d'abord
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        for (ParcoursEtape etape : etapes) {
            etapeRepository.delete(etape);
        }
        
        // Puis supprimer le parcours
        parcoursRepository.delete(parcours);
        
        // Forcer le flush pour s'assurer que la suppression est commitée
        parcoursRepository.flush();
    }

    // Publier/dépublier un parcours
    public ParcoursResponse togglePublishParcours(Long parcoursId, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        // Vérifier que le parcours a au moins une étape avant publication
        if (!parcours.getIsPublished()) {
            long nombreEtapes = etapeRepository.countByParcours(parcours);
            if (nombreEtapes == 0) {
                throw new RuntimeException("Un parcours doit avoir au moins une étape pour être publié");
            }
        }

        parcours.setIsPublished(!parcours.getIsPublished());
        parcours = parcoursRepository.save(parcours);

        return convertToResponse(parcours, formateurEmail);
    }

    // Créer les étapes d'un parcours
    private void createEtapes(ParcoursApprentissage parcours, List<ParcoursEtapeRequest> etapesRequest) {
        for (ParcoursEtapeRequest etapeRequest : etapesRequest) {
            Cours cours = coursRepository.findById(etapeRequest.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé: " + etapeRequest.getCoursId()));

            ParcoursEtape etape = new ParcoursEtape();
            etape.setParcours(parcours);
            etape.setCours(cours);
            etape.setOrdreEtape(etapeRequest.getOrdreEtape());
            etape.setNiveauEtape(etapeRequest.getNiveauEtape());
            etape.setIsObligatoire(etapeRequest.getIsObligatoire());
            etape.setScoreMinimum(etapeRequest.getScoreMinimum());
            etape.setPourcentageCompletionRequis(etapeRequest.getPourcentageCompletionRequis());
            etape.setQuizObligatoires(etapeRequest.getQuizObligatoires());
            etape.setDescription(etapeRequest.getDescription());

            etapeRepository.save(etape);
        }
    }

    // Convertir une entité en réponse
    private ParcoursResponse convertToResponse(ParcoursApprentissage parcours, String userEmail) {
        ParcoursResponse response = new ParcoursResponse();
        
        response.setId(parcours.getId());
        response.setTitre(parcours.getTitre());
        response.setDescription(parcours.getDescription());
        response.setThumbnailUrl(parcours.getThumbnailUrl());
        response.setCategorie(parcours.getCategorie());
        response.setNiveauDifficulte(parcours.getNiveauDifficulte());
        response.setDureeEstimeeHeures(parcours.getDureeEstimeeHeures());
        response.setPrerequis(parcours.getPrerequis());
        response.setTypeParcours(parcours.getTypeParcours());
        response.setPointsBonus(parcours.getPointsBonus());
        response.setBadgeCompletion(parcours.getBadgeCompletion());
        response.setCertificatEnabled(parcours.getCertificatEnabled());
        response.setIsPublished(parcours.getIsPublished());
        response.setCreatedAt(parcours.getCreatedAt());
        response.setUpdatedAt(parcours.getUpdatedAt());

        // Informations du formateur
        String formateurNom = parcours.getFormateur().getFirstName() != null && parcours.getFormateur().getLastName() != null
                ? parcours.getFormateur().getFirstName() + " " + parcours.getFormateur().getLastName()
                : parcours.getFormateur().getEmail();
        response.setFormateurNom(formateurNom);
        response.setFormateurEmail(parcours.getFormateur().getEmail());

        // Statistiques
        response.setNombreEtapes(parcours.getEtapes() != null ? parcours.getEtapes().size() : 0);
        response.setNombreInscriptions((int) inscriptionRepository.countByParcours(parcours));
        response.setNombreCompletions((int) inscriptionRepository.countByParcoursAndIsCompletedTrue(parcours));
        response.setProgressionMoyenne(inscriptionRepository.getAverageProgressionByParcours(parcours));

        // Étapes
        if (parcours.getEtapes() != null) {
            List<ParcoursEtapeResponse> etapesResponse = parcours.getEtapes().stream()
                    .map(this::convertEtapeToResponse)
                    .collect(Collectors.toList());
            response.setEtapes(etapesResponse);
        }

        // Informations pour l'utilisateur connecté
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user).orElse(null);
                if (inscription != null) {
                    response.setIsInscrit(true);
                    response.setProgressionUtilisateur(inscription.getProgressionPourcentage());
                    response.setEtapeCouranteUtilisateur(inscription.getEtapeCourante());
                }
            }
        }

        return response;
    }

    // Convertir une étape en réponse
    private ParcoursEtapeResponse convertEtapeToResponse(ParcoursEtape etape) {
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

        return response;
    }
}