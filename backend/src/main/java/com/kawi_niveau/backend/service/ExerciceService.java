package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciceService {

    @Autowired
    private ExerciceRepository exerciceRepository;

    @Autowired
    private ExerciceElementRepository exerciceElementRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ExerciceResponse createExercice(Long moduleId, ExerciceRequest request, String email) {
        com.kawi_niveau.backend.entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier que le formateur est propriétaire du cours
        if (!module.getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à créer un exercice pour ce module");
        }

        // Vérifier qu'il n'y a pas déjà un exercice pour ce module
        if (exerciceRepository.findByModule(module).isPresent()) {
            throw new RuntimeException("Un exercice existe déjà pour ce module");
        }

        Exercice exercice = new Exercice();
        exercice.setTitre(request.getTitre());
        exercice.setDescription(request.getDescription());
        exercice.setTypeExercice(request.getTypeExercice());
        exercice.setModule(module);

        Exercice savedExercice = exerciceRepository.save(exercice);

        // Ajouter les éléments si présents
        if (request.getElements() != null && !request.getElements().isEmpty()) {
            for (ExerciceElementRequest er : request.getElements()) {
                ExerciceElement element = new ExerciceElement();
                element.setContenu(er.getContenu());
                element.setTypeElement(er.getTypeElement());
                element.setPositionOrdre(er.getPositionOrdre());
                element.setReponseCorrecte(er.getReponseCorrecte());
                element.setOptions(er.getOptions());
                element.setExercice(savedExercice);
                exerciceElementRepository.save(element);
            }
        }

        return mapToResponse(savedExercice);
    }

    @Transactional
    public ExerciceResponse updateExercice(Long exerciceId, ExerciceRequest request, String email) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!exercice.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet exercice");
        }

        exercice.setTitre(request.getTitre());
        exercice.setDescription(request.getDescription());
        exercice.setTypeExercice(request.getTypeExercice());

        Exercice updatedExercice = exerciceRepository.save(exercice);
        return mapToResponse(updatedExercice);
    }

    @Transactional
    public void deleteExercice(Long exerciceId, String email) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!exercice.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cet exercice");
        }

        exerciceRepository.delete(exercice);
    }

    public ExerciceResponse getExerciceByModuleId(Long moduleId) {
        com.kawi_niveau.backend.entity.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        Exercice exercice = exerciceRepository.findByModule(module)
                .orElse(null);

        return exercice != null ? mapToResponse(exercice) : null;
    }

    public ExerciceResponse getExerciceById(Long exerciceId) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));
        return mapToResponse(exercice);
    }

    @Transactional
    public ExerciceElementResponse addElement(Long exerciceId, ExerciceElementRequest request, String email) {
        Exercice exercice = exerciceRepository.findById(exerciceId)
                .orElseThrow(() -> new RuntimeException("Exercice non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!exercice.getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à ajouter un élément à cet exercice");
        }

        ExerciceElement element = new ExerciceElement();
        element.setContenu(request.getContenu());
        element.setTypeElement(request.getTypeElement());
        element.setPositionOrdre(request.getPositionOrdre());
        element.setReponseCorrecte(request.getReponseCorrecte());
        element.setOptions(request.getOptions());
        element.setExercice(exercice);

        ExerciceElement savedElement = exerciceElementRepository.save(element);
        return mapElementToResponse(savedElement);
    }

    @Transactional
    public ExerciceElementResponse updateElement(Long elementId, ExerciceElementRequest request, String email) {
        ExerciceElement element = exerciceElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Élément non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!element.getExercice().getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cet élément");
        }

        element.setContenu(request.getContenu());
        element.setTypeElement(request.getTypeElement());
        element.setPositionOrdre(request.getPositionOrdre());
        element.setReponseCorrecte(request.getReponseCorrecte());
        element.setOptions(request.getOptions());

        ExerciceElement updatedElement = exerciceElementRepository.save(element);
        return mapElementToResponse(updatedElement);
    }

    @Transactional
    public void deleteElement(Long elementId, String email) {
        ExerciceElement element = exerciceElementRepository.findById(elementId)
                .orElseThrow(() -> new RuntimeException("Élément non trouvé"));

        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!element.getExercice().getModule().getCours().getFormateur().getId().equals(formateur.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cet élément");
        }

        exerciceElementRepository.delete(element);
    }

    private ExerciceResponse mapToResponse(Exercice exercice) {
        List<ExerciceElement> elements = exerciceElementRepository.findByExerciceOrderByPositionOrdreAsc(exercice);
        List<ExerciceElementResponse> elementResponses = elements.stream()
                .map(this::mapElementToResponse)
                .collect(Collectors.toList());

        return new ExerciceResponse(
                exercice.getId(),
                exercice.getTitre(),
                exercice.getDescription(),
                exercice.getTypeExercice(),
                exercice.getModule().getId(),
                elementResponses,
                exercice.getCreatedAt(),
                exercice.getUpdatedAt()
        );
    }

    private ExerciceElementResponse mapElementToResponse(ExerciceElement element) {
        return new ExerciceElementResponse(
                element.getId(),
                element.getContenu(),
                element.getTypeElement(),
                element.getPositionOrdre(),
                element.getReponseCorrecte(),
                element.getOptions(),
                element.getCreatedAt()
        );
    }
}