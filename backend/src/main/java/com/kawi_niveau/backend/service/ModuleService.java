package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ModuleRequest;
import com.kawi_niveau.backend.dto.ModuleResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.Module;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.ModuleRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModuleService {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    public ModuleResponse createModule(Long coursId, ModuleRequest request, String email) {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est le formateur du cours
        if (!cours.getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter des modules à ce cours");
        }

        Module module = new Module();
        module.setTitre(request.getTitre());
        module.setContenu(request.getContenu());
        module.setOrdre(request.getOrdre() != null ? request.getOrdre() : getNextOrdre(cours));
        module.setCours(cours);

        Module savedModule = moduleRepository.save(module);
        return mapToResponse(savedModule);
    }

    public ModuleResponse updateModule(Long moduleId, ModuleRequest request, String email) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est le formateur du cours
        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce module");
        }

        module.setTitre(request.getTitre());
        module.setContenu(request.getContenu());
        if (request.getOrdre() != null) {
            module.setOrdre(request.getOrdre());
        }

        Module updatedModule = moduleRepository.save(module);
        return mapToResponse(updatedModule);
    }

    public void deleteModule(Long moduleId, String email) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que l'utilisateur est le formateur du cours
        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce module");
        }

        moduleRepository.delete(module);
    }

    public List<ModuleResponse> getModulesByCours(Long coursId) {
        List<Module> modules = moduleRepository.findByCoursIdOrderByOrdreAsc(coursId);
        return modules.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ModuleResponse getModuleById(Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));
        return mapToResponse(module);
    }

    private int getNextOrdre(Cours cours) {
        List<Module> modules = moduleRepository.findByCoursOrderByOrdreAsc(cours);
        if (modules.isEmpty()) {
            return 1;
        }
        return modules.get(modules.size() - 1).getOrdre() + 1;
    }

    private ModuleResponse mapToResponse(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getTitre(),
                module.getContenu(),
                module.getOrdre(),
                module.getCreatedAt(),
                module.getUpdatedAt(),
                module.getCours().getId()
        );
    }
}
