package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.dto.ParcoursRequest;
import com.kawi_niveau.backend.dto.ParcoursResponse;
import com.kawi_niveau.backend.service.ParcoursService;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import com.kawi_niveau.backend.event.CourseCompletedEvent;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursController {

    @Autowired
    private ParcoursService parcoursService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

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

    // NOUVEAU: Endpoint de debug pour diagnostiquer les problèmes de progression
    @PostMapping("/{id}/debug-progression")
    public ResponseEntity<?> debugProgression(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            String debugInfo = parcoursService.debugProgression(id, userEmail);
            return ResponseEntity.ok().body("{\"debug\": \"" + debugInfo + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du debug: " + e.getMessage());
        }
    }

    // NOUVEAU: Endpoint pour déclencher manuellement la mise à jour de progression
    @PostMapping("/{id}/trigger-progression-update")
    public ResponseEntity<?> triggerProgressionUpdate(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            ParcoursApprentissage parcours = parcoursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            // Déclencher manuellement l'événement de mise à jour
            // Simuler la completion d'un cours pour déclencher la mise à jour
            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            if (!etapes.isEmpty()) {
                // Prendre le dernier cours du parcours
                Cours dernierCours = etapes.get(etapes.size() - 1).getCours();
                
                // Publier un événement de course completed pour déclencher la mise à jour
                eventPublisher.publishEvent(new CourseCompletedEvent(this, user, dernierCours, 100.0f));
                
                return ResponseEntity.ok().body("{\"message\": \"Événement de mise à jour déclenché avec succès\"}");
            } else {
                return ResponseEntity.badRequest().body("Aucune étape trouvée dans le parcours");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du déclenchement: " + e.getMessage());
        }
    }

    // NOUVEAU: Endpoint de debug ULTRA détaillé pour traquer le problème
    @PostMapping("/{id}/debug-ultra")
    public ResponseEntity<?> debugUltra(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            System.out.println("🔍🔍🔍 DEBUG ULTRA - Début pour " + userEmail + " sur parcours " + id);
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            ParcoursApprentissage parcours = parcoursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user)
                    .orElseThrow(() -> new RuntimeException("Pas d'inscription trouvée"));

            System.out.println("📊 ÉTAT ACTUEL:");
            System.out.println("   - Progression: " + inscription.getProgressionPourcentage() + "%");
            System.out.println("   - Étape courante: " + inscription.getEtapeCourante());
            System.out.println("   - Terminé: " + inscription.getIsCompleted());

            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            System.out.println("📋 NOMBRE D'ÉTAPES: " + etapes.size());

            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== DEBUG ULTRA DÉTAILLÉ ===\\n");
            debugInfo.append("Parcours: ").append(parcours.getTitre()).append("\\n");
            debugInfo.append("Utilisateur: ").append(userEmail).append("\\n");
            debugInfo.append("Progression BDD: ").append(inscription.getProgressionPourcentage()).append("%\\n\\n");

            int etapesValidees = 0;
            for (int i = 0; i < etapes.size(); i++) {
                ParcoursEtape etape = etapes.get(i);
                System.out.println("\\n🔍 === ANALYSE ÉTAPE " + etape.getOrdreEtape() + " ===");
                System.out.println("Cours: " + etape.getCours().getTitre() + " (ID: " + etape.getCours().getId() + ")");
                
                debugInfo.append("=== ÉTAPE ").append(etape.getOrdreEtape()).append(" ===\\n");
                debugInfo.append("Cours: ").append(etape.getCours().getTitre()).append(" (ID: ").append(etape.getCours().getId()).append(")\\n");
                
                // 1. Vérifier l'inscription au cours
                Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
                if (enrollment.isEmpty()) {
                    System.out.println("❌ PAS INSCRIT AU COURS");
                    debugInfo.append("❌ PROBLÈME: Pas inscrit au cours\\n\\n");
                    continue;
                }
                
                Enrollment enroll = enrollment.get();
                System.out.println("✅ Inscrit au cours - Progression: " + enroll.getProgress() + "%");
                debugInfo.append("✅ Inscrit - Progression: ").append(enroll.getProgress()).append("%\\n");
                
                // 2. Afficher les conditions de l'étape
                System.out.println("📋 CONDITIONS ÉTAPE:");
                Integer completionRequis = etape.getPourcentageCompletionRequis();
                Integer scoreMinimum = etape.getScoreMinimum();
                Boolean quizObligatoires = etape.getQuizObligatoires();
                
                System.out.println("   - Completion requise: " + (completionRequis != null ? completionRequis + "%" : "NULL"));
                System.out.println("   - Score minimum: " + (scoreMinimum != null ? scoreMinimum + "%" : "NULL"));
                System.out.println("   - Quiz obligatoires: " + (quizObligatoires != null ? quizObligatoires : "NULL"));
                
                debugInfo.append("Conditions:\\n");
                debugInfo.append("  - Completion: ").append(completionRequis != null ? completionRequis + "%" : "NULL").append("\\n");
                debugInfo.append("  - Score min: ").append(scoreMinimum != null ? scoreMinimum + "%" : "NULL").append("\\n");
                debugInfo.append("  - Quiz oblig: ").append(quizObligatoires != null ? quizObligatoires : "NULL").append("\\n");
                
                // 3. Test de validation ÉTAPE PAR ÉTAPE
                boolean etapeValide = true;
                String raisonEchec = "";
                
                // Test A: Completion
                if (completionRequis != null && completionRequis > 0) {
                    if (enroll.getProgress() < completionRequis) {
                        etapeValide = false;
                        raisonEchec = "Progression " + enroll.getProgress() + "% < " + completionRequis + "% (requis)";
                        System.out.println("❌ TEST COMPLETION: " + raisonEchec);
                    } else {
                        System.out.println("✅ TEST COMPLETION: " + enroll.getProgress() + "% >= " + completionRequis + "%");
                    }
                } else {
                    // Défaut: 100%
                    if (enroll.getProgress() < 100.0f) {
                        etapeValide = false;
                        raisonEchec = "Progression " + enroll.getProgress() + "% < 100% (défaut car NULL/0)";
                        System.out.println("❌ TEST COMPLETION (défaut): " + raisonEchec);
                    } else {
                        System.out.println("✅ TEST COMPLETION (défaut): " + enroll.getProgress() + "% >= 100%");
                    }
                }
                
                // Test B: Score (si étape encore valide)
                if (etapeValide && scoreMinimum != null && scoreMinimum > 0) {
                    List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                    double meilleurScore = 0.0;
                    for (Quiz quiz : quizzes) {
                        Optional<ResultatQuiz> resultat = resultatQuizRepository
                                .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                        if (resultat.isPresent()) {
                            meilleurScore = Math.max(meilleurScore, resultat.get().getScore());
                        }
                    }
                    
                    if (meilleurScore < scoreMinimum) {
                        etapeValide = false;
                        raisonEchec = "Score " + meilleurScore + "% < " + scoreMinimum + "% (requis)";
                        System.out.println("❌ TEST SCORE: " + raisonEchec);
                    } else {
                        System.out.println("✅ TEST SCORE: " + meilleurScore + "% >= " + scoreMinimum + "%");
                    }
                    debugInfo.append("  - Score obtenu: ").append(meilleurScore).append("%\\n");
                }
                
                // Test C: Quiz obligatoires (si étape encore valide)
                if (etapeValide && quizObligatoires != null && quizObligatoires) {
                    List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                    if (quizzes.isEmpty()) {
                        System.out.println("⚠️ TEST QUIZ: Quiz obligatoires mais aucun quiz dans le cours");
                        debugInfo.append("  - ⚠️ Quiz obligatoires mais pas de quiz\\n");
                    } else {
                        boolean tousReussis = true;
                        for (Quiz quiz : quizzes) {
                            Optional<ResultatQuiz> resultat = resultatQuizRepository
                                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                            if (resultat.isEmpty() || resultat.get().getScore() < 60.0) {
                                tousReussis = false;
                                break;
                            }
                        }
                        if (!tousReussis) {
                            etapeValide = false;
                            raisonEchec = "Quiz obligatoires non réussis (< 60%)";
                            System.out.println("❌ TEST QUIZ: " + raisonEchec);
                        } else {
                            System.out.println("✅ TEST QUIZ: Tous les quiz réussis");
                        }
                    }
                }
                
                // Résultat final de l'étape
                if (etapeValide) {
                    etapesValidees++;
                    System.out.println("🎉 ÉTAPE " + etape.getOrdreEtape() + " VALIDÉE");
                    debugInfo.append("RÉSULTAT: ✅ VALIDÉE\\n");
                } else {
                    System.out.println("💥 ÉTAPE " + etape.getOrdreEtape() + " NON VALIDÉE: " + raisonEchec);
                    debugInfo.append("RÉSULTAT: ❌ NON VALIDÉE\\n");
                    debugInfo.append("RAISON: ").append(raisonEchec).append("\\n");
                }
                
                debugInfo.append("\\n");
            }
            
            // Calcul final
            int progressionCalculee = etapes.isEmpty() ? 0 : (etapesValidees * 100) / etapes.size();
            System.out.println("\\n📊 CALCUL FINAL:");
            System.out.println("   - Étapes validées: " + etapesValidees + "/" + etapes.size());
            System.out.println("   - Progression calculée: " + progressionCalculee + "%");
            System.out.println("   - Progression en BDD: " + inscription.getProgressionPourcentage() + "%");
            
            debugInfo.append("=== RÉSULTAT FINAL ===\\n");
            debugInfo.append("Étapes validées: ").append(etapesValidees).append("/").append(etapes.size()).append("\\n");
            debugInfo.append("Progression calculée: ").append(progressionCalculee).append("%\\n");
            debugInfo.append("Progression BDD: ").append(inscription.getProgressionPourcentage()).append("%\\n");
            
            if (progressionCalculee != inscription.getProgressionPourcentage()) {
                System.out.println("🚨 INCOHÉRENCE DÉTECTÉE!");
                debugInfo.append("🚨 INCOHÉRENCE DÉTECTÉE!\\n");
                
                // Forcer la mise à jour immédiatement
                inscription.setProgressionPourcentage(progressionCalculee);
                inscription.setIsCompleted(etapesValidees == etapes.size());
                if (inscription.getIsCompleted() && inscription.getDateCompletion() == null) {
                    inscription.setDateCompletion(java.time.LocalDateTime.now());
                }
                inscriptionRepository.save(inscription);
                
                System.out.println("✅ CORRECTION APPLIQUÉE: " + progressionCalculee + "%");
                debugInfo.append("✅ CORRECTION APPLIQUÉE: ").append(progressionCalculee).append("%\\n");
            }

            return ResponseEntity.ok().body("{\"debug\": \"" + debugInfo.toString().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erreur debug ultra: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors du debug ultra: " + e.getMessage());
        }
    }
    @PostMapping("/{id}/debug-detaille")
    public ResponseEntity<?> debugDetaille(@PathVariable Long id, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            System.out.println("🔍 DEBUG DÉTAILLÉ - Début pour " + userEmail + " sur parcours " + id);
            
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            ParcoursApprentissage parcours = parcoursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user)
                    .orElseThrow(() -> new RuntimeException("Pas d'inscription trouvée"));

            System.out.println("📊 État actuel: " + inscription.getProgressionPourcentage() + "% - Étape " + inscription.getEtapeCourante());

            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            System.out.println("📋 Nombre d'étapes: " + etapes.size());

            StringBuilder debugInfo = new StringBuilder();
            debugInfo.append("=== DEBUG DÉTAILLÉ PARCOURS ===\\n");
            debugInfo.append("Parcours: ").append(parcours.getTitre()).append("\\n");
            debugInfo.append("Utilisateur: ").append(userEmail).append("\\n");
            debugInfo.append("Progression actuelle: ").append(inscription.getProgressionPourcentage()).append("%\\n");
            debugInfo.append("Étape courante: ").append(inscription.getEtapeCourante()).append("\\n\\n");

            int etapesValidees = 0;
            for (ParcoursEtape etape : etapes) {
                System.out.println("🔍 Analyse étape " + etape.getOrdreEtape() + ": " + etape.getCours().getTitre());
                
                debugInfo.append("=== ÉTAPE ").append(etape.getOrdreEtape()).append(" ===\\n");
                debugInfo.append("Cours: ").append(etape.getCours().getTitre()).append("\\n");
                
                // Conditions de l'étape
                debugInfo.append("Conditions:\\n");
                debugInfo.append("  - Score minimum: ").append(etape.getScoreMinimum() != null ? etape.getScoreMinimum() + "%" : "NULL").append("\\n");
                debugInfo.append("  - Completion requise: ").append(etape.getPourcentageCompletionRequis() != null ? etape.getPourcentageCompletionRequis() + "%" : "NULL").append("\\n");
                debugInfo.append("  - Quiz obligatoires: ").append(etape.getQuizObligatoires() != null ? etape.getQuizObligatoires() : "NULL").append("\\n");
                
                // Vérifier l'inscription au cours
                Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
                if (enrollment.isPresent()) {
                    float progress = enrollment.get().getProgress();
                    debugInfo.append("État actuel:\\n");
                    debugInfo.append("  - Progression cours: ").append(progress).append("%\\n");
                    System.out.println("  📈 Progression cours: " + progress + "%");
                    
                    // Test de validation étape par étape
                    boolean isComplete = false;
                    String raisonEchec = "";
                    
                    // Test 1: Pourcentage de completion
                    Integer completionRequis = etape.getPourcentageCompletionRequis();
                    if (completionRequis == null) {
                        // Si NULL, on considère 100% par défaut
                        if (progress < 100.0f) {
                            raisonEchec = "Progression " + progress + "% < 100% (défaut car NULL)";
                        }
                    } else if (progress < completionRequis) {
                        raisonEchec = "Progression " + progress + "% < " + completionRequis + "% (requis)";
                    }
                    
                    // Test 2: Score minimum (si pas d'échec précédent)
                    if (raisonEchec.isEmpty() && etape.getScoreMinimum() != null && etape.getScoreMinimum() > 0) {
                        // Calculer le meilleur score
                        double meilleurScore = 0.0;
                        List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                        for (Quiz quiz : quizzes) {
                            Optional<ResultatQuiz> resultat = resultatQuizRepository
                                    .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                            if (resultat.isPresent()) {
                                meilleurScore = Math.max(meilleurScore, resultat.get().getScore());
                            }
                        }
                        debugInfo.append("  - Meilleur score quiz: ").append(meilleurScore).append("%\\n");
                        
                        if (meilleurScore < etape.getScoreMinimum()) {
                            raisonEchec = "Score " + meilleurScore + "% < " + etape.getScoreMinimum() + "% (requis)";
                        }
                    }
                    
                    // Test 3: Quiz obligatoires (si pas d'échec précédent)
                    if (raisonEchec.isEmpty() && etape.getQuizObligatoires() != null && etape.getQuizObligatoires()) {
                        List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                        if (quizzes.isEmpty()) {
                            debugInfo.append("  - ⚠️ Quiz obligatoires mais aucun quiz dans le cours\\n");
                        } else {
                            boolean tousQuizReussis = true;
                            for (Quiz quiz : quizzes) {
                                Optional<ResultatQuiz> resultat = resultatQuizRepository
                                        .findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                                if (resultat.isEmpty() || resultat.get().getScore() < 60.0) {
                                    tousQuizReussis = false;
                                    break;
                                }
                            }
                            if (!tousQuizReussis) {
                                raisonEchec = "Quiz obligatoires non réussis (score < 60%)";
                            }
                        }
                    }
                    
                    // Résultat final
                    if (raisonEchec.isEmpty()) {
                        isComplete = true;
                        etapesValidees++;
                        debugInfo.append("RÉSULTAT: ✅ ÉTAPE VALIDÉE\\n");
                        System.out.println("  ✅ Étape " + etape.getOrdreEtape() + " validée");
                    } else {
                        debugInfo.append("RÉSULTAT: ❌ ÉTAPE NON VALIDÉE\\n");
                        debugInfo.append("RAISON: ").append(raisonEchec).append("\\n");
                        System.out.println("  ❌ Étape " + etape.getOrdreEtape() + " non validée: " + raisonEchec);
                    }
                } else {
                    debugInfo.append("❌ PAS INSCRIT AU COURS\\n");
                    System.out.println("  ❌ Pas inscrit au cours " + etape.getCours().getTitre());
                }
                
                debugInfo.append("\\n");
            }
            
            // Calcul final
            int progressionCalculee = etapes.isEmpty() ? 0 : (etapesValidees * 100) / etapes.size();
            debugInfo.append("=== CALCUL FINAL ===\\n");
            debugInfo.append("Étapes validées: ").append(etapesValidees).append("/").append(etapes.size()).append("\\n");
            debugInfo.append("Progression calculée: ").append(progressionCalculee).append("%\\n");
            debugInfo.append("Progression en base: ").append(inscription.getProgressionPourcentage()).append("%\\n");
            
            System.out.println("📊 RÉSULTAT: " + etapesValidees + "/" + etapes.size() + " = " + progressionCalculee + "%");
            
            if (progressionCalculee != inscription.getProgressionPourcentage()) {
                debugInfo.append("⚠️ INCOHÉRENCE DÉTECTÉE - Mise à jour nécessaire\\n");
                System.out.println("⚠️ INCOHÉRENCE: Calculé=" + progressionCalculee + "% vs Base=" + inscription.getProgressionPourcentage() + "%");
            }

            return ResponseEntity.ok().body("{\"debug\": \"" + debugInfo.toString().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Erreur debug détaillé: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors du debug détaillé: " + e.getMessage());
        }
    }
}