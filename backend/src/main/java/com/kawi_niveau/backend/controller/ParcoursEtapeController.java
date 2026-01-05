package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ParcoursEtapeRequest;
import com.kawi_niveau.backend.dto.ParcoursEtapeResponse;
import com.kawi_niveau.backend.service.ParcoursEtapeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursEtapeController {

    @Autowired
    private ParcoursEtapeService etapeService;

    // Ajouter une étape à un parcours
    @PostMapping("/{parcoursId}/etapes")
    public ResponseEntity<?> addEtape(@PathVariable Long parcoursId,
                                     @Valid @RequestBody ParcoursEtapeRequest request,
                                     Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            ParcoursEtapeResponse response = etapeService.addEtape(parcoursId, request, formateurEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout de l'étape: " + e.getMessage());
        }
    }

    // Obtenir toutes les étapes d'un parcours
    @GetMapping("/{parcoursId}/etapes")
    public ResponseEntity<?> getEtapesByParcours(@PathVariable Long parcoursId,
                                                Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursEtapeResponse> etapes = etapeService.getEtapesByParcours(parcoursId, userEmail);
            return ResponseEntity.ok(etapes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des étapes: " + e.getMessage());
        }
    }

    // Mettre à jour une étape
    @PutMapping("/etapes/{etapeId}")
    public ResponseEntity<?> updateEtape(@PathVariable Long etapeId,
                                        @Valid @RequestBody ParcoursEtapeRequest request,
                                        Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            ParcoursEtapeResponse response = etapeService.updateEtape(etapeId, request, formateurEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour de l'étape: " + e.getMessage());
        }
    }

    // Supprimer une étape
    @DeleteMapping("/etapes/{etapeId}")
    public ResponseEntity<?> deleteEtape(@PathVariable Long etapeId,
                                        Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            etapeService.deleteEtape(etapeId, formateurEmail);
            return ResponseEntity.ok().body("{\"message\": \"Étape supprimée avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression de l'étape: " + e.getMessage());
        }
    }

    // Réorganiser les étapes (drag & drop)
    @PutMapping("/{parcoursId}/etapes/reorder")
    public ResponseEntity<?> reorderEtapes(@PathVariable Long parcoursId,
                                          @RequestBody List<Long> nouvelOrdre,
                                          Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            List<ParcoursEtapeResponse> etapes = etapeService.reorderEtapes(parcoursId, nouvelOrdre, formateurEmail);
            return ResponseEntity.ok(etapes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la réorganisation des étapes: " + e.getMessage());
        }
    }

    // Obtenir une étape par ID
    @GetMapping("/etapes/{etapeId}")
    public ResponseEntity<?> getEtapeById(@PathVariable Long etapeId,
                                         Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            ParcoursEtapeResponse etape = etapeService.getEtapeById(etapeId, userEmail);
            return ResponseEntity.ok(etape);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération de l'étape: " + e.getMessage());
        }
    }
}