package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ParcoursRequest;
import com.kawi_niveau.backend.dto.ParcoursResponse;
import com.kawi_niveau.backend.service.ParcoursService;
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
public class ParcoursController {

    @Autowired
    private ParcoursService parcoursService;

    // Créer un nouveau parcours
    @PostMapping
    public ResponseEntity<?> createParcours(@Valid @RequestBody ParcoursRequest request, 
                                           Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            ParcoursResponse response = parcoursService.createParcours(request, formateurEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la création du parcours: " + e.getMessage());
        }
    }

    // Obtenir tous les parcours du formateur connecté
    @GetMapping("/mes-parcours")
    public ResponseEntity<?> getMesParcours(Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getMesParcours(formateurEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours: " + e.getMessage());
        }
    }

    // Obtenir un parcours par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getParcoursById(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            ParcoursResponse parcours = parcoursService.getParcoursById(id, userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération du parcours: " + e.getMessage());
        }
    }

    // Mettre à jour un parcours
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParcours(@PathVariable Long id, 
                                           @Valid @RequestBody ParcoursRequest request,
                                           Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            ParcoursResponse response = parcoursService.updateParcours(id, request, formateurEmail);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour du parcours: " + e.getMessage());
        }
    }

    // Supprimer un parcours
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParcours(@PathVariable Long id, Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            parcoursService.deleteParcours(id, formateurEmail);
            return ResponseEntity.ok().body("Parcours supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la suppression du parcours: " + e.getMessage());
        }
    }

    // Publier/dépublier un parcours
    @PutMapping("/{id}/toggle-publish")
    public ResponseEntity<?> togglePublishParcours(@PathVariable Long id, Authentication authentication) {
        try {
            System.out.println("DEBUG: Tentative de publication du parcours " + id);
            
            if (authentication == null) {
                System.err.println("DEBUG: Authentication est null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié");
            }
            
            String formateurEmail = authentication.getName();
            System.out.println("DEBUG: Utilisateur authentifié: " + formateurEmail);
            
            ParcoursResponse response = parcoursService.togglePublishParcours(id, formateurEmail);
            System.out.println("DEBUG: Publication réussie, nouveau statut: " + response.getIsPublished());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("DEBUG: Erreur de publication: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de la publication du parcours: " + e.getMessage());
        }
    }

    // Obtenir les parcours publiés (pour les apprenants)
    @GetMapping("/publies")
    public ResponseEntity<?> getParcoursPublies(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getParcoursPublies(userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours: " + e.getMessage());
        }
    }

    // Rechercher des parcours publiés
    @GetMapping("/rechercher")
    public ResponseEntity<?> rechercherParcours(@RequestParam String terme, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.rechercherParcours(terme, userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la recherche: " + e.getMessage());
        }
    }

    // Obtenir les parcours par catégorie
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<?> getParcoursParCategorie(@PathVariable String categorie, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getParcoursParCategorie(categorie, userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours: " + e.getMessage());
        }
    }

    // Obtenir les parcours populaires
    @GetMapping("/populaires")
    public ResponseEntity<?> getParcoursPopulaires(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getParcoursPopulaires(userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours populaires: " + e.getMessage());
        }
    }

    // S'inscrire à un parcours
    @PostMapping("/{id}/inscription")
    public ResponseEntity<?> sInscrireAuParcours(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            parcoursService.sInscrireAuParcours(id, userEmail);
            return ResponseEntity.ok().body("{\"message\": \"Inscription réussie au parcours\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    // Se désinscrire d'un parcours
    @DeleteMapping("/{id}/inscription")
    public ResponseEntity<?> seDesinscrireDuParcours(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            parcoursService.seDesinscrireDuParcours(id, userEmail);
            return ResponseEntity.ok().body("{\"message\": \"Désinscription réussie du parcours\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la désinscription: " + e.getMessage());
        }
    }

    // Obtenir les parcours de l'utilisateur connecté
    @GetMapping("/mes-inscriptions")
    public ResponseEntity<?> getMesInscriptions(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getMesInscriptions(userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des inscriptions: " + e.getMessage());
        }
    }

    // Obtenir les parcours en cours de l'utilisateur
    @GetMapping("/mes-inscriptions/en-cours")
    public ResponseEntity<?> getMesInscriptionsEnCours(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getMesInscriptionsEnCours(userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours en cours: " + e.getMessage());
        }
    }

    // Obtenir les parcours terminés de l'utilisateur
    @GetMapping("/mes-inscriptions/termines")
    public ResponseEntity<?> getMesInscriptionsTerminees(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<ParcoursResponse> parcours = parcoursService.getMesInscriptionsTerminees(userEmail);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des parcours terminés: " + e.getMessage());
        }
    }

    // Statistiques d'un parcours (pour le formateur)
    @GetMapping("/{id}/statistiques")
    public ResponseEntity<?> getStatistiquesParcours(@PathVariable Long id, Authentication authentication) {
        try {
            String formateurEmail = authentication.getName();
            ParcoursResponse parcours = parcoursService.getParcoursById(id, formateurEmail);
            
            // Vérifier que l'utilisateur est le formateur du parcours
            if (!parcours.getFormateurEmail().equals(formateurEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Vous n'êtes pas autorisé à voir les statistiques de ce parcours");
            }
            
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la récupération des statistiques: " + e.getMessage());
        }
    }

    // Forcer la mise à jour de la progression d'un parcours spécifique
    @PostMapping("/{id}/forcer-mise-a-jour")
    public ResponseEntity<?> forcerMiseAJourProgression(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            parcoursService.forcerMiseAJourProgression(id, userEmail);
            return ResponseEntity.ok().body("{\"message\": \"Progression mise à jour avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }
}