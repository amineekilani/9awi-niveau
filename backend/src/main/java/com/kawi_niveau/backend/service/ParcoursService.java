package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.*;
import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParcoursService {

    @Autowired
    private ParcoursRepository parcoursRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private ParcoursValidationService validationService;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private ResultatQuizRepository resultatQuizRepository;

    // Créer un nouveau parcours
    public ParcoursResponse createParcours(ParcoursRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        if (formateur.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Seuls les formateurs peuvent créer des parcours");
        }

        ParcoursApprentissage parcours = new ParcoursApprentissage();
        parcours.setTitre(request.getTitre());
        parcours.setDescription(request.getDescription());
        parcours.setThumbnailUrl(request.getThumbnailUrl());
        parcours.setCategorie(request.getCategorie());
        parcours.setNiveauDifficulte(request.getNiveauDifficulte());
        parcours.setDureeEstimeeHeures(request.getDureeEstimeeHeures());
        parcours.setPrerequis(request.getPrerequis());
        parcours.setTypeParcours(request.getTypeParcours());
        parcours.setPointsBonus(request.getPointsBonus());
        parcours.setBadgeCompletion(request.getBadgeCompletion());
        parcours.setCertificatEnabled(request.getCertificatEnabled());
        parcours.setIsPublished(request.getIsPublished());
        parcours.setFormateur(formateur);

        parcours = parcoursRepository.save(parcours);

        // Créer les étapes si fournies
        if (request.getEtapes() != null && !request.getEtapes().isEmpty()) {
            createEtapes(parcours, request.getEtapes());
        }

        return convertToResponse(parcours, formateurEmail);
    }

    // Mettre à jour un parcours
    public ParcoursResponse updateParcours(Long parcoursId, ParcoursRequest request, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        parcours.setTitre(request.getTitre());
        parcours.setDescription(request.getDescription());
        parcours.setThumbnailUrl(request.getThumbnailUrl());
        parcours.setCategorie(request.getCategorie());
        parcours.setNiveauDifficulte(request.getNiveauDifficulte());
        parcours.setDureeEstimeeHeures(request.getDureeEstimeeHeures());
        parcours.setPrerequis(request.getPrerequis());
        parcours.setTypeParcours(request.getTypeParcours());
        parcours.setPointsBonus(request.getPointsBonus());
        parcours.setBadgeCompletion(request.getBadgeCompletion());
        parcours.setCertificatEnabled(request.getCertificatEnabled());
        parcours.setIsPublished(request.getIsPublished());

        parcours = parcoursRepository.save(parcours);

        return convertToResponse(parcours, formateurEmail);
    }

    // Obtenir tous les parcours d'un formateur
    public List<ParcoursResponse> getMesParcours(String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        List<ParcoursApprentissage> parcours = parcoursRepository.findByFormateurOrderByCreatedAtDesc(formateur);
        
        return parcours.stream()
                .map(p -> convertToResponse(p, formateurEmail))
                .collect(Collectors.toList());
    }

    // Obtenir un parcours par ID
    public ParcoursResponse getParcoursById(Long parcoursId, String userEmail) {
        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        return convertToResponse(parcours, userEmail);
    }

    // Supprimer un parcours
    @Transactional
    public void deleteParcours(Long parcoursId, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        // Vérifier s'il y a des inscriptions actives
        long inscriptionsActives = inscriptionRepository.countByParcours(parcours) - 
                                   inscriptionRepository.countByParcoursAndIsCompletedTrue(parcours);
        if (inscriptionsActives > 0) {
            throw new RuntimeException("Impossible de supprimer un parcours avec des inscriptions actives");
        }

        // Supprimer explicitement les étapes d'abord
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        for (ParcoursEtape etape : etapes) {
            etapeRepository.delete(etape);
        }
        
        // Puis supprimer le parcours
        parcoursRepository.delete(parcours);
        
        // Forcer le flush pour s'assurer que la suppression est commitée
        parcoursRepository.flush();
    }

    // Publier/dépublier un parcours
    public ParcoursResponse togglePublishParcours(Long parcoursId, String formateurEmail) {
        User formateur = userRepository.findByEmail(formateurEmail)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findByIdAndFormateur(parcoursId, formateur)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé ou non autorisé"));

        // Vérifier que le parcours a au moins une étape avant publication
        if (!parcours.getIsPublished()) {
            long nombreEtapes = etapeRepository.countByParcours(parcours);
            if (nombreEtapes == 0) {
                throw new RuntimeException("Un parcours doit avoir au moins une étape pour être publié");
            }
        }

        parcours.setIsPublished(!parcours.getIsPublished());
        parcours = parcoursRepository.save(parcours);

        return convertToResponse(parcours, formateurEmail);
    }

    // Créer les étapes d'un parcours
    private void createEtapes(ParcoursApprentissage parcours, List<ParcoursEtapeRequest> etapesRequest) {
        for (ParcoursEtapeRequest etapeRequest : etapesRequest) {
            Cours cours = coursRepository.findById(etapeRequest.getCoursId())
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé: " + etapeRequest.getCoursId()));

            ParcoursEtape etape = new ParcoursEtape();
            etape.setParcours(parcours);
            etape.setCours(cours);
            etape.setOrdreEtape(etapeRequest.getOrdreEtape());
            etape.setNiveauEtape(etapeRequest.getNiveauEtape());
            etape.setIsObligatoire(etapeRequest.getIsObligatoire());
            etape.setScoreMinimum(etapeRequest.getScoreMinimum());
            etape.setPourcentageCompletionRequis(etapeRequest.getPourcentageCompletionRequis());
            etape.setQuizObligatoires(etapeRequest.getQuizObligatoires());
            etape.setDescription(etapeRequest.getDescription());

            etapeRepository.save(etape);
        }
    }

    // Convertir une entité en réponse
    private ParcoursResponse convertToResponse(ParcoursApprentissage parcours, String userEmail) {
        ParcoursResponse response = new ParcoursResponse();
        
        response.setId(parcours.getId());
        response.setTitre(parcours.getTitre());
        response.setDescription(parcours.getDescription());
        response.setThumbnailUrl(parcours.getThumbnailUrl());
        response.setCategorie(parcours.getCategorie());
        response.setNiveauDifficulte(parcours.getNiveauDifficulte());
        response.setDureeEstimeeHeures(parcours.getDureeEstimeeHeures());
        response.setPrerequis(parcours.getPrerequis());
        response.setTypeParcours(parcours.getTypeParcours());
        response.setPointsBonus(parcours.getPointsBonus());
        response.setBadgeCompletion(parcours.getBadgeCompletion());
        response.setCertificatEnabled(parcours.getCertificatEnabled());
        response.setIsPublished(parcours.getIsPublished());
        response.setCreatedAt(parcours.getCreatedAt());
        response.setUpdatedAt(parcours.getUpdatedAt());

        // Informations du formateur
        String formateurNom = parcours.getFormateur().getFirstName() != null && parcours.getFormateur().getLastName() != null
                ? parcours.getFormateur().getFirstName() + " " + parcours.getFormateur().getLastName()
                : parcours.getFormateur().getEmail();
        response.setFormateurNom(formateurNom);
        response.setFormateurEmail(parcours.getFormateur().getEmail());

        // Statistiques
        response.setNombreEtapes(parcours.getEtapes() != null ? parcours.getEtapes().size() : 0);
        response.setNombreInscriptions((int) inscriptionRepository.countByParcours(parcours));
        response.setNombreCompletions((int) inscriptionRepository.countByParcoursAndIsCompletedTrue(parcours));
        response.setProgressionMoyenne(inscriptionRepository.getAverageProgressionByParcours(parcours));

        // Étapes avec validation si utilisateur connecté
        if (parcours.getEtapes() != null) {
            User user = null;
            if (userEmail != null) {
                user = userRepository.findByEmail(userEmail).orElse(null);
            }
            
            final User finalUser = user; // Variable finale pour la lambda
            List<ParcoursEtapeResponse> etapesResponse = parcours.getEtapes().stream()
                    .sorted((a, b) -> a.getOrdreEtape().compareTo(b.getOrdreEtape()))
                    .map(etape -> convertEtapeToResponse(etape, finalUser, parcours.getEtapes()))
                    .collect(Collectors.toList());
            response.setEtapes(etapesResponse);
        }

        // Informations pour l'utilisateur connecté
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user).orElse(null);
                if (inscription != null) {
                    response.setIsInscrit(true);
                    response.setProgressionUtilisateur(inscription.getProgressionPourcentage());
                    response.setEtapeCouranteUtilisateur(inscription.getEtapeCourante());
                }
            }
        }

        return response;
    }

    // ===== MÉTHODES POUR LES APPRENANTS =====

    // Obtenir tous les parcours publiés
    public List<ParcoursResponse> getParcoursPublies(String userEmail) {
        List<ParcoursApprentissage> parcours = parcoursRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
        return parcours.stream()
                .map(p -> convertToResponse(p, userEmail))
                .collect(Collectors.toList());
    }

    // Rechercher des parcours publiés
    public List<ParcoursResponse> rechercherParcours(String terme, String userEmail) {
        List<ParcoursApprentissage> parcours = parcoursRepository.searchPublishedParcours(terme);
        return parcours.stream()
                .map(p -> convertToResponse(p, userEmail))
                .collect(Collectors.toList());
    }

    // Obtenir les parcours par catégorie
    public List<ParcoursResponse> getParcoursParCategorie(String categorie, String userEmail) {
        List<ParcoursApprentissage> parcours = parcoursRepository.findByCategorieAndIsPublishedTrueOrderByCreatedAtDesc(categorie);
        return parcours.stream()
                .map(p -> convertToResponse(p, userEmail))
                .collect(Collectors.toList());
    }

    // Obtenir les parcours populaires (les plus inscrits)
    public List<ParcoursResponse> getParcoursPopulaires(String userEmail) {
        List<ParcoursApprentissage> parcours = parcoursRepository.findPopularParcours();
        return parcours.stream()
                .limit(10) // Limiter aux 10 plus populaires
                .map(p -> convertToResponse(p, userEmail))
                .collect(Collectors.toList());
    }

    // S'inscrire à un parcours
    public void sInscrireAuParcours(Long parcoursId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        if (!parcours.getIsPublished()) {
            throw new RuntimeException("Ce parcours n'est pas publié");
        }

        // Vérifier si l'utilisateur n'est pas déjà inscrit
        if (inscriptionRepository.existsByParcoursAndUser(parcours, user)) {
            throw new RuntimeException("Vous êtes déjà inscrit à ce parcours");
        }

        System.out.println("🎯 INSCRIPTION PARCOURS: " + parcours.getTitre() + " pour " + user.getEmail());

        // Créer l'inscription au parcours
        ParcoursInscription inscription = new ParcoursInscription();
        inscription.setParcours(parcours);
        inscription.setUser(user);
        inscription.setDateInscription(LocalDateTime.now());
        inscription.setEtapeCourante(1);
        inscription.setProgressionPourcentage(0);
        inscription.setIsCompleted(false);

        inscriptionRepository.save(inscription);
        System.out.println("✅ Inscription parcours créée");

        // NOUVEAU: Auto-inscription aux cours des étapes
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        System.out.println("📋 Nombre d'étapes à traiter: " + etapes.size());
        
        for (ParcoursEtape etape : etapes) {
            Cours cours = etape.getCours();
            System.out.println("🔍 Vérification cours: " + cours.getTitre() + " (ID: " + cours.getId() + ")");
            
            // Vérifier si pas déjà inscrit au cours
            if (!enrollmentRepository.existsByUserAndCours(user, cours)) {
                Enrollment enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCours(cours);
                enrollment.setProgress(0.0f);
                // enrolledAt sera défini automatiquement par @PrePersist
                enrollmentRepository.save(enrollment);
                
                System.out.println("✅ Auto-inscription créée pour cours: " + cours.getTitre());
            } else {
                System.out.println("ℹ️ Déjà inscrit au cours: " + cours.getTitre());
            }
        }
        
        System.out.println("🎉 Inscription parcours terminée avec auto-inscriptions cours");
    }

    // Se désinscrire d'un parcours
    public void seDesinscrireDuParcours(Long parcoursId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas inscrit à ce parcours"));

        inscriptionRepository.delete(inscription);
    }

    // Obtenir toutes les inscriptions d'un utilisateur
    public List<ParcoursResponse> getMesInscriptions(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<ParcoursInscription> inscriptions = inscriptionRepository.findByUserOrderByDateInscriptionDesc(user);
        
        return inscriptions.stream()
                .map(inscription -> {
                    ParcoursResponse response = convertToResponse(inscription.getParcours(), userEmail);
                    // Ajouter les informations d'inscription
                    response.setDateInscription(inscription.getDateInscription());
                    response.setDateCompletion(inscription.getDateCompletion());
                    response.setProgressionUtilisateur(inscription.getProgressionPourcentage());
                    response.setEtapeCouranteUtilisateur(inscription.getEtapeCourante());
                    response.setPointsGagnesUtilisateur(inscription.getPointsGagnes());
                    response.setIsCompletedUtilisateur(inscription.getIsCompleted());
                    response.setCertificatGenere(inscription.getCertificatGenere());
                    response.setCertificatUrl(inscription.getCertificatUrl());
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Obtenir les inscriptions en cours d'un utilisateur
    public List<ParcoursResponse> getMesInscriptionsEnCours(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<ParcoursInscription> inscriptions = inscriptionRepository.findByUserAndIsCompletedFalseOrderByDateInscriptionDesc(user);
        
        return inscriptions.stream()
                .map(inscription -> {
                    ParcoursResponse response = convertToResponse(inscription.getParcours(), userEmail);
                    // Ajouter les informations d'inscription
                    response.setDateInscription(inscription.getDateInscription());
                    response.setProgressionUtilisateur(inscription.getProgressionPourcentage());
                    response.setEtapeCouranteUtilisateur(inscription.getEtapeCourante());
                    response.setPointsGagnesUtilisateur(inscription.getPointsGagnes());
                    response.setIsCompletedUtilisateur(inscription.getIsCompleted());
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Obtenir les inscriptions terminées d'un utilisateur
    public List<ParcoursResponse> getMesInscriptionsTerminees(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<ParcoursInscription> inscriptions = inscriptionRepository.findByUserAndIsCompletedTrueOrderByDateCompletionDesc(user);
        
        return inscriptions.stream()
                .map(inscription -> {
                    ParcoursResponse response = convertToResponse(inscription.getParcours(), userEmail);
                    // Ajouter les informations d'inscription
                    response.setDateInscription(inscription.getDateInscription());
                    response.setDateCompletion(inscription.getDateCompletion());
                    response.setProgressionUtilisateur(inscription.getProgressionPourcentage());
                    response.setEtapeCouranteUtilisateur(inscription.getEtapeCourante());
                    response.setPointsGagnesUtilisateur(inscription.getPointsGagnes());
                    response.setIsCompletedUtilisateur(inscription.getIsCompleted());
                    response.setCertificatGenere(inscription.getCertificatGenere());
                    response.setCertificatUrl(inscription.getCertificatUrl());
                    return response;
                })
                .collect(Collectors.toList());
    }

    // Convertir une étape en réponse avec validation
    private ParcoursEtapeResponse convertEtapeToResponse(ParcoursEtape etape, User user, List<ParcoursEtape> toutesEtapes) {
        ParcoursEtapeResponse response = new ParcoursEtapeResponse();
        
        response.setId(etape.getId());
        response.setCoursId(etape.getCours().getId());
        response.setCoursTitle(etape.getCours().getTitre());
        response.setCoursDescription(etape.getCours().getDescription());
        response.setCoursThumbnailUrl(etape.getCours().getThumbnailUrl());
        response.setCoursNiveauDifficulte(etape.getCours().getNiveauDifficulte());
        response.setCoursCategorie(etape.getCours().getCategorie());
        response.setOrdreEtape(etape.getOrdreEtape());
        response.setNiveauEtape(etape.getNiveauEtape());
        response.setIsObligatoire(etape.getIsObligatoire());
        response.setScoreMinimum(etape.getScoreMinimum());
        response.setPourcentageCompletionRequis(etape.getPourcentageCompletionRequis());
        response.setQuizObligatoires(etape.getQuizObligatoires());
        response.setDescription(etape.getDescription());
        response.setCreatedAt(etape.getCreatedAt());

        // Validation et progression si utilisateur connecté
        if (user != null) {
            // Récupérer les étapes précédentes pour la validation linéaire
            List<ParcoursEtape> etapesPrecedentes = toutesEtapes.stream()
                    .filter(e -> e.getOrdreEtape() < etape.getOrdreEtape())
                    .collect(Collectors.toList());

            // Valider l'étape
            boolean isDebloque = validationService.isEtapeDebloquee(etape, user, etapesPrecedentes);
            boolean isComplete = validationService.isEtapeComplete(etape, user);
            
            response.setIsDebloque(isDebloque);
            response.setIsComplete(isComplete);

            // Récupérer les données de progression
            ParcoursValidationService.EtapeValidationResult validation = 
                    validationService.validateEtapeConditions(etape, user);
            
            response.setProgressionCours((int) Math.round(validation.getProgressionCours()));
            response.setScoreObtenu((int) Math.round(validation.getScoreObtenu()));
        } else {
            // Pas d'utilisateur connecté, valeurs par défaut
            response.setIsDebloque(false);
            response.setIsComplete(false);
            response.setProgressionCours(0);
            response.setScoreObtenu(0);
        }

        return response;
    }

    // Forcer la mise à jour de la progression d'un parcours spécifique
    public void forcerMiseAJourProgression(Long parcoursId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

        // Vérifier si l'utilisateur est inscrit
        ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user)
                .orElseThrow(() -> new RuntimeException("Vous n'êtes pas inscrit à ce parcours"));

        // Forcer le recalcul de la progression
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        
        int etapesCompletes = 0;
        int etapeCourante = 1;
        
        for (ParcoursEtape etape : etapes) {
            boolean isComplete = validationService.isEtapeComplete(etape, user);
            if (isComplete) {
                etapesCompletes++;
            } else {
                etapeCourante = etape.getOrdreEtape();
                break;
            }
        }
        
        // Mettre à jour l'inscription
        int progressionPourcentage = etapes.isEmpty() ? 0 : (etapesCompletes * 100) / etapes.size();
        inscription.setProgressionPourcentage(progressionPourcentage);
        inscription.setEtapeCourante(etapeCourante);
        inscription.setIsCompleted(etapesCompletes == etapes.size());
        
        if (inscription.getIsCompleted() && inscription.getDateCompletion() == null) {
            inscription.setDateCompletion(LocalDateTime.now());
        }
        
        inscriptionRepository.save(inscription);
    }

    // NOUVEAU: Méthode de debug pour diagnostiquer les problèmes de progression
    public String debugProgression(Long parcoursId, String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            ParcoursApprentissage parcours = parcoursRepository.findById(parcoursId)
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé"));

            ParcoursInscription inscription = inscriptionRepository.findByParcoursAndUser(parcours, user)
                    .orElseThrow(() -> new RuntimeException("Vous n'êtes pas inscrit à ce parcours"));

            StringBuilder debug = new StringBuilder();
            debug.append("=== DEBUG PROGRESSION PARCOURS ===\\n");
            debug.append("Parcours: ").append(parcours.getTitre()).append("\\n");
            debug.append("Utilisateur: ").append(userEmail).append("\\n");
            debug.append("Progression actuelle: ").append(inscription.getProgressionPourcentage()).append("%\\n");
            debug.append("Étape courante: ").append(inscription.getEtapeCourante()).append("\\n");
            debug.append("Terminé: ").append(inscription.getIsCompleted()).append("\\n\\n");

            List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
            debug.append("=== DÉTAIL DES ÉTAPES ===\\n");
            
            for (ParcoursEtape etape : etapes) {
                debug.append("Étape ").append(etape.getOrdreEtape()).append(": ").append(etape.getCours().getTitre()).append("\\n");
                debug.append("  - Score minimum requis: ").append(etape.getScoreMinimum()).append("%\\n");
                debug.append("  - Completion requise: ").append(etape.getPourcentageCompletionRequis()).append("%\\n");
                debug.append("  - Quiz obligatoires: ").append(etape.getQuizObligatoires()).append("\\n");
                
                // Vérifier l'inscription au cours
                Optional<Enrollment> enrollment = enrollmentRepository.findByUserAndCours(user, etape.getCours());
                if (enrollment.isPresent()) {
                    debug.append("  - Progression cours: ").append(enrollment.get().getProgress()).append("%\\n");
                    
                    // Vérifier les quiz
                    List<Quiz> quizzes = quizRepository.findByCours(etape.getCours());
                    if (!quizzes.isEmpty()) {
                        double meilleurScore = 0;
                        for (Quiz quiz : quizzes) {
                            Optional<ResultatQuiz> resultat = resultatQuizRepository.findFirstByUserAndQuizOrderByScoreDesc(user, quiz);
                            if (resultat.isPresent()) {
                                meilleurScore = Math.max(meilleurScore, resultat.get().getScore());
                            }
                        }
                        debug.append("  - Meilleur score quiz: ").append(meilleurScore).append("%\\n");
                    } else {
                        debug.append("  - Aucun quiz dans ce cours\\n");
                    }
                    
                    // Validation finale
                    boolean isComplete = validationService.isEtapeComplete(etape, user);
                    debug.append("  - STATUT: ").append(isComplete ? "✅ VALIDÉE" : "❌ NON VALIDÉE").append("\\n");
                } else {
                    debug.append("  - ❌ PAS INSCRIT AU COURS\\n");
                }
                debug.append("\\n");
            }

            return debug.toString();
        } catch (Exception e) {
            return "Erreur lors du debug: " + e.getMessage();
        }
    }

    /**
     * Forcer la vérification de tous les parcours d'un utilisateur
     */
    public void forceCheckAllParcoursCompletion(User user) {
        System.out.println("🔧 DEBUT forceCheckAllParcoursCompletion pour: " + user.getEmail());
        
        try {
            // Récupérer toutes les inscriptions de l'utilisateur
            List<ParcoursInscription> inscriptions = inscriptionRepository.findByUser(user);
            System.out.println("📋 Nombre d'inscriptions trouvées: " + inscriptions.size());
            
            for (ParcoursInscription inscription : inscriptions) {
                ParcoursApprentissage parcours = inscription.getParcours();
                System.out.println("🔍 Vérification parcours: " + parcours.getTitre());
                
                // Recalculer la progression
                List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
                int etapesCompletes = 0;
                int etapeCourante = 1;
                boolean parcoursComplete = true;
                
                for (ParcoursEtape etape : etapes) {
                    boolean isComplete = validationService.isEtapeComplete(etape, user);
                    if (isComplete) {
                        etapesCompletes++;
                    } else {
                        parcoursComplete = false;
                        if (etapeCourante == etape.getOrdreEtape()) {
                            etapeCourante = etape.getOrdreEtape();
                        }
                    }
                }
                
                // Calculer le pourcentage
                int progressionPourcentage = etapes.isEmpty() ? 0 : (etapesCompletes * 100) / etapes.size();
                
                // Mettre à jour l'inscription
                boolean wasCompleted = inscription.getIsCompleted();
                inscription.setProgressionPourcentage(progressionPourcentage);
                inscription.setEtapeCourante(etapeCourante);
                inscription.setIsCompleted(parcoursComplete);
                
                // Si le parcours vient d'être terminé
                if (parcoursComplete && !wasCompleted) {
                    inscription.setDateCompletion(LocalDateTime.now());
                    
                    // Attribuer les points
                    if (parcours.getPointsBonus() != null && parcours.getPointsBonus() > 0) {
                        inscription.setPointsGagnes(parcours.getPointsBonus());
                        
                        // Attribuer les XP au système global
                        try {
                            gamificationService.awardXP(user, parcours.getPointsBonus(), "Parcours terminé: " + parcours.getTitre());
                            System.out.println("💰 Points attribués: +" + parcours.getPointsBonus() + " XP");
                        } catch (Exception e) {
                            System.err.println("⚠️ Erreur attribution XP: " + e.getMessage());
                        }
                    }
                    
                    System.out.println("🎉 Parcours terminé: " + parcours.getTitre());
                }
                
                inscriptionRepository.save(inscription);
                System.out.println("📊 Progression mise à jour: " + progressionPourcentage + "%");
            }
            
            System.out.println("✅ forceCheckAllParcoursCompletion terminé avec succès");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans forceCheckAllParcoursCompletion: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}