package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.CoursRequest;
import com.kawi_niveau.backend.dto.CoursResponse;
import com.kawi_niveau.backend.dto.NiveauDifficulteResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoursService {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    public CoursResponse createCours(CoursRequest request, String email) {
        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (formateur.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Seuls les formateurs peuvent créer des cours");
        }

        Cours cours = new Cours();
        cours.setTitre(request.getTitre());
        cours.setDescription(request.getDescription());
        cours.setCategorie(request.getCategorie());
        cours.setThumbnailUrl(request.getThumbnailUrl());
        cours.setKeywords(request.getKeywords());
        cours.setNiveauDifficulte(request.getNiveauDifficulte());
        cours.setFormateur(formateur);

        Cours savedCours = coursRepository.save(cours);
        return mapToResponse(savedCours);
    }

    public CoursResponse updateCours(Long id, CoursRequest request, String email) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce cours");
        }

        cours.setTitre(request.getTitre());
        cours.setDescription(request.getDescription());
        cours.setCategorie(request.getCategorie());
        cours.setThumbnailUrl(request.getThumbnailUrl());
        cours.setKeywords(request.getKeywords());
        cours.setNiveauDifficulte(request.getNiveauDifficulte());

        Cours updatedCours = coursRepository.save(cours);
        return mapToResponse(updatedCours);
    }

    public void archiveCours(Long id, String email) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à archiver ce cours");
        }

        cours.setArchived(true);
        cours.setArchivedAt(System.currentTimeMillis());
        coursRepository.save(cours);
    }

    public void unarchiveCours(Long id, String email) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à réactiver ce cours");
        }

        cours.setArchived(false);
        cours.setArchivedAt(null);
        coursRepository.save(cours);
    }

    public List<CoursResponse> getMesCours(String email) {
        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Cours> coursList = coursRepository.findByFormateurOrderByCreatedAtDesc(formateur);
        System.out.println("Nombre de cours trouvés pour le formateur: " + coursList.size());
        coursList.forEach(c -> System.out.println("Cours: " + c.getTitre() + ", Archivé: " + c.isArchived()));
        return coursList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<CoursResponse> getAllCours() {
        List<Cours> coursList = coursRepository.findByArchivedFalse();
        return coursList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<CoursResponse> searchCours(String keyword) {
        List<Cours> coursList = coursRepository.searchCours(keyword);
        return coursList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return coursRepository.findDistinctCategories();
    }

    public List<NiveauDifficulteResponse> getAllNiveauxDifficulte() {
        return Arrays.stream(NiveauDifficulte.values())
                .map(NiveauDifficulteResponse::fromNiveau)
                .collect(Collectors.toList());
    }

    public List<CoursResponse> searchCoursByNiveau(NiveauDifficulte niveau) {
        List<Cours> coursList = coursRepository.findByNiveauDifficulteAndArchivedFalse(niveau);
        return coursList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<CoursResponse> searchCoursAvecFiltres(String keyword, String categorie, NiveauDifficulte niveau) {
        List<Cours> coursList;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (categorie != null && !categorie.trim().isEmpty() && niveau != null) {
                coursList = coursRepository.searchCoursWithAllFilters(keyword, categorie, niveau);
            } else if (categorie != null && !categorie.trim().isEmpty()) {
                coursList = coursRepository.searchCoursWithKeywordAndCategory(keyword, categorie);
            } else if (niveau != null) {
                coursList = coursRepository.searchCoursWithKeywordAndNiveau(keyword, niveau);
            } else {
                coursList = coursRepository.searchCours(keyword);
            }
        } else if (categorie != null && !categorie.trim().isEmpty() && niveau != null) {
            coursList = coursRepository.findByCategorieAndNiveauDifficulteAndArchivedFalse(categorie, niveau);
        } else if (categorie != null && !categorie.trim().isEmpty()) {
            coursList = coursRepository.findByCategorieAndArchivedFalse(categorie);
        } else if (niveau != null) {
            coursList = coursRepository.findByNiveauDifficulteAndArchivedFalse(niveau);
        } else {
            coursList = coursRepository.findByArchivedFalse();
        }
        
        return coursList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public CoursResponse getCoursById(Long id) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        return mapToResponse(cours);
    }

    private CoursResponse mapToResponse(Cours cours) {
        String formateurNom = "";
        if (cours.getFormateur().getFirstName() != null && cours.getFormateur().getLastName() != null) {
            formateurNom = cours.getFormateur().getFirstName() + " " + cours.getFormateur().getLastName();
        } else if (cours.getFormateur().getFirstName() != null) {
            formateurNom = cours.getFormateur().getFirstName();
        } else if (cours.getFormateur().getLastName() != null) {
            formateurNom = cours.getFormateur().getLastName();
        } else {
            formateurNom = cours.getFormateur().getEmail(); // Fallback vers l'email
        }
        
        String formateurDomaine = cours.getFormateur().getDomaineSpecialisation();
        if (formateurDomaine == null || formateurDomaine.trim().isEmpty()) {
            formateurDomaine = "Développement Web"; // Valeur par défaut
        }
        
        return new CoursResponse(
                cours.getId(),
                cours.getTitre(),
                cours.getDescription(),
                cours.getCreatedAt(),
                cours.getUpdatedAt(),
                cours.isArchived(),
                cours.getArchivedAt(),
                cours.getCategorie(),
                cours.getThumbnailUrl(),
                cours.getKeywords(),
                cours.getNiveauDifficulte(),
                cours.getNiveauDifficulte().getDisplayName(),
                cours.getFormateur().getId(),
                formateurNom,
                formateurDomaine);
    }
}
