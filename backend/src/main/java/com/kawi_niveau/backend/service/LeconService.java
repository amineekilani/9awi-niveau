package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.LeconRequest;
import com.kawi_niveau.backend.dto.LeconResponse;
import com.kawi_niveau.backend.entity.Lecon;
import com.kawi_niveau.backend.entity.Module;
import com.kawi_niveau.backend.entity.TypeContenu;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.LeconRepository;
import com.kawi_niveau.backend.repository.ModuleRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeconService {

    @Autowired
    private LeconRepository leconRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeconFileService leconFileService;

    public LeconResponse createLecon(Long moduleId, LeconRequest request, String email) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est le formateur du cours
        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter des leçons à ce module");
        }

        Lecon lecon = new Lecon();
        lecon.setTitre(request.getTitre());
        lecon.setTypeContenu(TypeContenu.valueOf(request.getTypeContenu()));
        lecon.setContenuTexte(request.getContenuTexte());
        lecon.setFichierUrl(request.getFichierUrl());
        lecon.setOrdre(request.getOrdre() != null ? request.getOrdre() : getNextOrdre(module));
        lecon.setDuree(request.getDuree());
        lecon.setModule(module);

        Lecon savedLecon = leconRepository.save(lecon);
        return mapToResponse(savedLecon);
    }

    public LeconResponse createLeconWithFile(Long moduleId, String titre, String typeContenu, 
                                            Integer ordre, Integer duree, MultipartFile file, String email) throws IOException {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter des leçons à ce module");
        }

        // Sauvegarder le fichier
        String filename = leconFileService.saveLeconFile(file, typeContenu);

        Lecon lecon = new Lecon();
        lecon.setTitre(titre);
        lecon.setTypeContenu(TypeContenu.valueOf(typeContenu));
        lecon.setFichierUrl(filename);
        lecon.setOrdre(ordre != null ? ordre : getNextOrdre(module));
        lecon.setDuree(duree);
        lecon.setModule(module);

        Lecon savedLecon = leconRepository.save(lecon);
        return mapToResponse(savedLecon);
    }

    public LeconResponse updateLecon(Long leconId, LeconRequest request, String email) {
        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!lecon.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette leçon");
        }

        lecon.setTitre(request.getTitre());
        lecon.setTypeContenu(TypeContenu.valueOf(request.getTypeContenu()));
        lecon.setContenuTexte(request.getContenuTexte());
        
        if (request.getFichierUrl() != null) {
            lecon.setFichierUrl(request.getFichierUrl());
        }
        
        if (request.getOrdre() != null) {
            lecon.setOrdre(request.getOrdre());
        }
        
        lecon.setDuree(request.getDuree());

        Lecon updatedLecon = leconRepository.save(lecon);
        return mapToResponse(updatedLecon);
    }

    public LeconResponse updateLeconFile(Long leconId, MultipartFile file, String email) throws IOException {
        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!lecon.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette leçon");
        }

        // Supprimer l'ancien fichier si existe
        if (lecon.getFichierUrl() != null) {
            leconFileService.deleteLeconFile(lecon.getFichierUrl());
        }

        // Sauvegarder le nouveau fichier
        String filename = leconFileService.saveLeconFile(file, lecon.getTypeContenu().name());
        lecon.setFichierUrl(filename);

        Lecon updatedLecon = leconRepository.save(lecon);
        return mapToResponse(updatedLecon);
    }

    public void deleteLecon(Long leconId, String email) {
        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!lecon.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette leçon");
        }

        // Supprimer le fichier si existe
        if (lecon.getFichierUrl() != null) {
            leconFileService.deleteLeconFile(lecon.getFichierUrl());
        }

        leconRepository.delete(lecon);
    }

    public List<LeconResponse> getLeconsByModule(Long moduleId) {
        List<Lecon> lecons = leconRepository.findByModuleIdOrderByOrdreAsc(moduleId);
        return lecons.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public LeconResponse getLeconById(Long leconId) {
        Lecon lecon = leconRepository.findById(leconId)
                .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));
        return mapToResponse(lecon);
    }

    private int getNextOrdre(Module module) {
        List<Lecon> lecons = leconRepository.findByModuleOrderByOrdreAsc(module);
        if (lecons.isEmpty()) {
            return 1;
        }
        return lecons.get(lecons.size() - 1).getOrdre() + 1;
    }

    private LeconResponse mapToResponse(Lecon lecon) {
        return new LeconResponse(
                lecon.getId(),
                lecon.getTitre(),
                lecon.getTypeContenu().name(),
                lecon.getContenuTexte(),
                lecon.getFichierUrl(),
                lecon.getOrdre(),
                lecon.getDuree(),
                lecon.getCreatedAt(),
                lecon.getUpdatedAt(),
                lecon.getModule().getId()
        );
    }
}
