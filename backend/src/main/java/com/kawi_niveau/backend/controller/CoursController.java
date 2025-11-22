package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.CoursRequest;
import com.kawi_niveau.backend.dto.CoursResponse;
import com.kawi_niveau.backend.dto.MessageResponse;
import com.kawi_niveau.backend.service.CoursService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cours")
@CrossOrigin(origins = "http://localhost:4200")
public class CoursController {

    @Autowired
    private CoursService coursService;

    @PostMapping
    public ResponseEntity<?> createCours(@Valid @RequestBody CoursRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            CoursResponse cours = coursService.createCours(request, email);
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCours(@PathVariable Long id, @Valid @RequestBody CoursRequest request, Authentication authentication) {
        try {
            String email = authentication.getName();
            CoursResponse cours = coursService.updateCours(id, request, email);
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveCours(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            coursService.archiveCours(id, email);
            return ResponseEntity.ok(new MessageResponse("Cours archivé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/mes-cours")
    public ResponseEntity<List<CoursResponse>> getMesCours(Authentication authentication) {
        String email = authentication.getName();
        List<CoursResponse> cours = coursService.getMesCours(email);
        return ResponseEntity.ok(cours);
    }

    @GetMapping
    public ResponseEntity<List<CoursResponse>> getAllCours() {
        List<CoursResponse> cours = coursService.getAllCours();
        return ResponseEntity.ok(cours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCoursById(@PathVariable Long id) {
        try {
            CoursResponse cours = coursService.getCoursById(id);
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
