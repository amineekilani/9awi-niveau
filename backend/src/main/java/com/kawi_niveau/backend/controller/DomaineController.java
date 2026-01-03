package com.kawi_niveau.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/domaines")
@CrossOrigin(origins = "http://localhost:4200")
public class DomaineController {

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllDomaines() {
        List<Map<String, String>> domaines = Arrays.asList(
            Map.of("nom", "Développement Web", "icone", "code", "couleur", "#3B82F6"),
            Map.of("nom", "Développement Mobile", "icone", "smartphone", "couleur", "#10B981"),
            Map.of("nom", "Data Science", "icone", "bar-chart", "couleur", "#8B5CF6"),
            Map.of("nom", "Intelligence Artificielle", "icone", "cpu", "couleur", "#F59E0B"),
            Map.of("nom", "Design UI/UX", "icone", "palette", "couleur", "#EF4444"),
            Map.of("nom", "Cybersécurité", "icone", "shield", "couleur", "#DC2626"),
            Map.of("nom", "DevOps", "icone", "server", "couleur", "#059669"),
            Map.of("nom", "Marketing Digital", "icone", "trending-up", "couleur", "#7C3AED"),
            Map.of("nom", "Gestion de Projet", "icone", "briefcase", "couleur", "#0891B2"),
            Map.of("nom", "Business Intelligence", "icone", "pie-chart", "couleur", "#EA580C"),
            Map.of("nom", "Réseaux et Systèmes", "icone", "wifi", "couleur", "#1F2937"),
            Map.of("nom", "Base de Données", "icone", "database", "couleur", "#374151")
        );
        
        return ResponseEntity.ok(domaines);
    }
}