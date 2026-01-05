package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ParcoursProgressionService {

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private ParcoursEtapeRepository etapeRepository;

    @Autowired
    private ParcoursValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ParcoursNotificationService notificationService;

    @Autowired
    private CertificateService certificateService;

    /**
     * Met à jour la progression d'un utilisateur dans tous ses parcours
     * Appelé quand l'utilisateur progresse dans un cours
     */
    public void updateProgressionParcours(User user, Cours cours) {
        // Trouver tous les parcours où ce cours est une étape
        List<ParcoursEtape> etapesAvecCeCours = etapeRepository.findByCours(cours);
        
        for (ParcoursEtape etape : etapesAvecCeCours) {
            ParcoursApprentissage parcours = etape.getParcours();
            
            // Vérifier si l'utilisateur est inscrit à ce parcours
            Optional<ParcoursInscription> inscriptionOpt = 
                    inscriptionRepository.findByParcoursAndUser(parcours, user);
            
            if (inscriptionOpt.isPresent()) {
                updateProgressionParcours(inscriptionOpt.get());
            }
        }
    }

    /**
     * Met à jour la progression d'une inscription spécifique
     */
    public void updateProgressionParcours(ParcoursInscription inscription) {
        ParcoursApprentissage parcours = inscription.getParcours();
        User user = inscription.getUser();
        
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        
        if (etapes.isEmpty()) {
            return;
        }

        int etapesCompletes = 0;
        int etapeCourante = 1; // Commencer à 1 par défaut
        int pointsGagnes = 0; // Variable manquante
        boolean parcoursComplete = true;

        // Calculer la progression
        for (ParcoursEtape etape : etapes) {
            boolean isComplete = validationService.isEtapeComplete(etape, user);
            
            if (isComplete) {
                etapesCompletes++;
                // Ajouter les points de l'étape (si définis)
                if (etape.getParcours().getPointsBonus() != null) {
                    pointsGagnes += etape.getParcours().getPointsBonus() / etapes.size();
                }
            } else {
                parcoursComplete = false;
                // Si c'est la première étape non complète et qu'on n'a pas encore défini l'étape courante
                if (etapeCourante == 1 && etapesCompletes == 0) {
                    // Première étape non complète = étape courante
                    etapeCourante = etape.getOrdreEtape();
                } else if (etapesCompletes > 0 && etapeCourante == 1) {
                    // Il y a des étapes complètes, cette étape non complète devient l'étape courante
                    etapeCourante = etape.getOrdreEtape();
                }
            }
        }
        
        // Si toutes les étapes sont complètes, l'étape courante est la dernière
        if (parcoursComplete && !etapes.isEmpty()) {
            etapeCourante = etapes.get(etapes.size() - 1).getOrdreEtape();
        }
        
        // Sécurité : s'assurer que l'étape courante est valide
        if (etapeCourante < 1 && !etapes.isEmpty()) {
            etapeCourante = etapes.get(0).getOrdreEtape(); // Première étape par défaut
        }
        
        System.out.println("📊 Calcul progression: " + etapesCompletes + "/" + etapes.size() + " étapes complètes");
        System.out.println("📍 Étape courante calculée: " + etapeCourante);

        // Calculer le pourcentage de progression
        int progressionPourcentage = (etapesCompletes * 100) / etapes.size();

        // Mettre à jour l'inscription
        boolean wasCompleted = inscription.getIsCompleted();
        inscription.setProgressionPourcentage(progressionPourcentage);
        inscription.setEtapeCourante(etapeCourante);
        inscription.setPointsGagnes(pointsGagnes);
        inscription.setIsCompleted(parcoursComplete);

        if (parcoursComplete && inscription.getDateCompletion() == null) {
            inscription.setDateCompletion(LocalDateTime.now());
            
            // NOUVEAU: Attribuer les récompenses et créer les notifications
            if (!wasCompleted) {
                onParcoursCompleted(inscription, user, parcours);
            }
        }

        inscriptionRepository.save(inscription);
        
        System.out.println("📊 Progression parcours mise à jour: " + parcours.getTitre() + " - " + progressionPourcentage + "%");
    }

    /**
     * Gère la completion d'un parcours (récompenses + notifications)
     */
    private void onParcoursCompleted(ParcoursInscription inscription, User user, ParcoursApprentissage parcours) {
        try {
            System.out.println("🎉 Parcours terminé: " + parcours.getTitre() + " par " + user.getEmail());
            
            // 1. Attribuer les XP au système global
            if (parcours.getPointsBonus() != null && parcours.getPointsBonus() > 0) {
                try {
                    gamificationService.awardXP(user, parcours.getPointsBonus(), "Parcours terminé: " + parcours.getTitre());
                    gamificationService.onParcoursCompleted(user, parcours.getTitre(), parcours.getPointsBonus());
                    System.out.println("💰 Points bonus attribués: +" + parcours.getPointsBonus() + " XP");
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur attribution XP: " + e.getMessage());
                }
            }
            
            // 2. Générer le certificat si activé
            if (parcours.getCertificatEnabled()) {
                try {
                    System.out.println("🏆 Génération du certificat pour le parcours: " + parcours.getTitre());
                    String certificatUrl = certificateService.generateCertificate(inscription);
                    inscription.setCertificatGenere(true);
                    inscription.setCertificatUrl(certificatUrl);
                    System.out.println("✅ Certificat généré et URL mise à jour: " + certificatUrl);
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur génération certificat: " + e.getMessage());
                    // Ne pas faire échouer la completion pour une erreur de certificat
                }
            }
            
            // 3. Créer la notification
            try {
                notificationService.createParcoursCompletionNotification(
                    user, parcours, inscription.getPointsGagnes(), 
                    inscription.getCertificatGenere(), inscription.getCertificatUrl()
                );
                System.out.println("📢 Notification de parcours créée");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur création notification: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la completion du parcours: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recalcule la progression de tous les parcours d'un utilisateur
     */
    public void recalculerProgressionUtilisateur(User user) {
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByUser(user);
        
        for (ParcoursInscription inscription : inscriptions) {
            updateProgressionParcours(inscription);
        }
    }

    /**
     * Recalcule la progression de tous les parcours d'un utilisateur par email
     */
    public void recalculerProgressionUtilisateur(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user != null) {
            recalculerProgressionUtilisateur(user);
        }
    }

    /**
     * Recalcule la progression de toutes les inscriptions d'un parcours
     */
    public void recalculerProgressionParcours(ParcoursApprentissage parcours) {
        List<ParcoursInscription> inscriptions = inscriptionRepository.findByParcours(parcours);
        
        for (ParcoursInscription inscription : inscriptions) {
            updateProgressionParcours(inscription);
        }
    }

    /**
     * Vérifie si un utilisateur peut accéder à l'étape suivante
     */
    public boolean peutAccederEtapeSuivante(User user, ParcoursApprentissage parcours, int etapeActuelle) {
        List<ParcoursEtape> etapes = etapeRepository.findByParcoursOrderByOrdreEtape(parcours);
        
        if (etapeActuelle >= etapes.size()) {
            return false; // Pas d'étape suivante
        }

        ParcoursEtape etapeSuivante = etapes.stream()
                .filter(e -> e.getOrdreEtape() == etapeActuelle + 1)
                .findFirst()
                .orElse(null);

        if (etapeSuivante == null) {
            return false;
        }

        List<ParcoursEtape> etapesPrecedentes = etapes.stream()
                .filter(e -> e.getOrdreEtape() < etapeSuivante.getOrdreEtape())
                .toList();

        return validationService.isEtapeDebloquee(etapeSuivante, user, etapesPrecedentes);
    }
}