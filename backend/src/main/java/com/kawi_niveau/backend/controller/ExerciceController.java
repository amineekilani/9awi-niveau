package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exercice")
@CrossOrigin(origins = "http://localhost:4200")
public class ExerciceController {

    @Autowired
    private ExerciceService exerciceService;

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<?> createExercice(
            @PathVariable Long moduleId,
            @Valid @RequestBody ExerciceRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ExerciceResponse exercice = exerciceService.createExercice(moduleId, request, email);
            return ResponseEntity.ok(exercice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{exerciceId}")
    public ResponseEntity<?> updateExercice(
            @PathVariable Long exerciceId,
            @Valid @RequestBody ExerciceRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ExerciceResponse exercice = exerciceService.updateExercice(exerciceId, request, email);
            return ResponseEntity.ok(exercice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{exerciceId}")
    public ResponseEntity<?> deleteExercice(@PathVariable Long exerciceId, Authentication authentication) {
        try {
            String email = authentication.getName();
            exerciceService.deleteExercice(exerciceId, email);
            return ResponseEntity.ok(new MessageResponse("Exercice supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getExerciceByModuleId(@PathVariable Long moduleId) {
        try {
            ExerciceResponse exercice = exerciceService.getExerciceByModuleId(moduleId);
            if (exercice == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(exercice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{exerciceId}")
    public ResponseEntity<?> getExerciceById(@PathVariable Long exerciceId) {
        try {
            ExerciceResponse exercice = exerciceService.getExerciceById(exerciceId);
            return ResponseEntity.ok(exercice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{exerciceId}/element")
    public ResponseEntity<?> addElement(
            @PathVariable Long exerciceId,
            @Valid @RequestBody ExerciceElementRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ExerciceElementResponse element = exerciceService.addElement(exerciceId, request, email);
            return ResponseEntity.ok(element);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/element/{elementId}")
    public ResponseEntity<?> updateElement(
            @PathVariable Long elementId,
            @Valid @RequestBody ExerciceElementRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            ExerciceElementResponse element = exerciceService.updateElement(elementId, request, email);
            return ResponseEntity.ok(element);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/element/{elementId}")
    public ResponseEntity<?> deleteElement(@PathVariable Long elementId, Authentication authentication) {
        try {
            String email = authentication.getName();
            exerciceService.deleteElement(elementId, email);
            return ResponseEntity.ok(new MessageResponse("Élément supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}