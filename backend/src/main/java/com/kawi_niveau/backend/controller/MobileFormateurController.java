package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.FormateurStatsResponse;
import com.kawi_niveau.backend.dto.ParcoursResponse;
import com.kawi_niveau.backend.dto.ParcoursProgressionStatsResponse;
import com.kawi_niveau.backend.dto.CoursStatsResponse;
import com.kawi_niveau.backend.service.FormateurService;
import com.kawi_niveau.backend.service.ParcoursService;
import com.kawi_niveau.backend.service.CoursService;
import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.ParcoursInscription;
import com.kawi_niveau.backend.repository.ParcoursRepository;
import com.kawi_niveau.backend.repository.ParcoursInscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile")
@CrossOrigin(origins = "*")
public class MobileFormateurController {

    @Autowired
    private FormateurService formateurService;

    @Autowired
    private ParcoursService parcoursService;

    @Autowired
    private CoursService coursService;

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    // Statistiques du formateur pour mobile
    @GetMapping("/formateur/stats")
    public ResponseEntity<?> getFormateurStats(Authentication authentication) {
        try {
            String email = authentication.getName();
            FormateurStatsResponse stats = formateurService.getFormateurStats(email);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des statistiques: " + e.getMessage());
        }
    }

    // Statistiques d'un cours spécifique pour mobile
    @GetMapping("/cours/{id}/stats")
    public ResponseEntity<?> getCoursStats(@PathVariable Long id, Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            CoursStatsResponse stats = coursService.getCoursStats(id, formateurEmail);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des statistiques du cours: " + e.getMessage());
        }
    }

    // Liste des parcours du formateur pour mobile
    @GetMapping("/formateur/parcours")
    public ResponseEntity<?> getParcoursFormateur(Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getMesParcours(formateurEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours: " + e.getMessage());
        }
    }

    // Statistiques de progression d'un parcours pour mobile
    @GetMapping("/parcours/{id}/progression")
    public ResponseEntity<?> getParcoursProgressionStats(@PathVariable Long id, Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();

            // Vérifier que l'utilisateur est le formateur du parcours
            ParcoursApprentissage parcours = parcoursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            if (!parcours.getFormateur().getEmail().equals(formateurEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Vous n'êtes pas autorisé à voir les statistiques de ce parcours");
            }

            // Calculer les statistiques correctes
            List<ParcoursInscription> inscriptions = inscriptionRepository.findByParcoursId(id);

            int totalInscrits = inscriptions.size();
            int nombreTermines = (int) inscriptions.stream()
                    .filter(ParcoursInscription::getIsCompleted)
                    .count();
            int nombreEnCours = totalInscrits - nombreTermines; // Logique simple et correcte
            int nombreCertificats = (int) inscriptions.stream()
                    .filter(i -> i.getCertificatGenere() != null && i.getCertificatGenere())
                    .count();

            double progressionMoyenne = inscriptions.stream()
                    .mapToDouble(ParcoursInscription::getProgressionPourcentage)
                    .average()
                    .orElse(0.0);

            ParcoursProgressionStatsResponse stats = new ParcoursProgressionStatsResponse(
                    parcours.getId(),
                    parcours.getTitre(),
                    totalInscrits,
                    nombreTermines,
                    nombreEnCours,
                    nombreCertificats,
                    progressionMoyenne
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des statistiques: " + e.getMessage());
        }
    }
}