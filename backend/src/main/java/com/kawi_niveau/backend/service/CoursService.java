package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.CoursRequest;
import com.kawi_niveau.backend.dto.CoursResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<String> getAllCategories() {
        return coursRepository.findDistinctCategories();
    }

    public CoursResponse getCoursById(Long id) {
        Cours cours = coursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
        return mapToResponse(cours);
    }

    private CoursResponse mapToResponse(Cours cours) {
        String formateurNom = cours.getFormateur().getFirstName() + " " + cours.getFormateur().getLastName();
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
                cours.getFormateur().getId(),
                formateurNom);
    }
}
