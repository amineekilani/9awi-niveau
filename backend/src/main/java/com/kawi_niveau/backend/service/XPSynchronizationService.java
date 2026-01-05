package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.*;
import com.kawi_niveau.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service pour synchroniser les XP entre le système global et les parcours
 */
@Service
@Transactional
public class XPSynchronizationService {

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private GamificationService gamificationService;

    /**
     * Synchronise les XP d'un utilisateur après completion d'un parcours
     */
    public void synchronizeXPAfterParcoursCompletion(User user, ParcoursApprentissage parcours, Integer xpEarned) {
        try {
            System.out.println("🔄 Synchronisation XP pour " + user.getEmail() + " - Parcours: " + parcours.getTitre());
            
            // 1. Vérifier l'état actuel des XP globaux
            Optional<UserXP> userXPOpt = userXPRepository.findByUser(user);
            int xpAvant = userXPOpt.map(UserXP::getTotalXP).orElse(0);
            System.out.println("💰 XP avant: " + xpAvant);

            // 2. Attribuer les XP au système global si pas déjà fait
            if (xpEarned != null && xpEarned > 0) {
                try {
                    gamificationService.awardXP(user, xpEarned, "Parcours terminé: " + parcours.getTitre());
                    System.out.println("✅ XP attribués au système global: +" + xpEarned);
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur attribution XP global: " + e.getMessage());
                }
            }

            // 3. Vérifier l'état après attribution
            userXPOpt = userXPRepository.findByUser(user);
            int xpApres = userXPOpt.map(UserXP::getTotalXP).orElse(0);
            System.out.println("💰 XP après: " + xpApres + " (+" + (xpApres - xpAvant) + ")");

            // 4. Déclencher les événements de gamification pour badges/niveaux
            try {
                gamificationService.onParcoursCompleted(user, parcours.getTitre(), xpEarned != null ? xpEarned : 0);
                System.out.println("🎯 Événements gamification déclenchés");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur événements gamification: " + e.getMessage());
            }

            System.out.println("✅ Synchronisation XP terminée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la synchronisation XP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recalcule et synchronise tous les XP d'un utilisateur
     */
    public void recalculateAndSynchronizeUserXP(User user) {
        try {
            System.out.println("🔄 Recalcul complet XP pour " + user.getEmail());

            // 1. Calculer les XP totaux des parcours terminés
            int totalXPParcours = inscriptionRepository.findByUser(user).stream()
                .filter(ParcoursInscription::getIsCompleted)
                .mapToInt(inscription -> inscription.getPointsGagnes() != null ? inscription.getPointsGagnes() : 0)
                .sum();

            System.out.println("📊 XP total des parcours: " + totalXPParcours);

            // 2. Vérifier les XP globaux actuels
            Optional<UserXP> userXPOpt = userXPRepository.findByUser(user);
            int xpGlobauxActuels = userXPOpt.map(UserXP::getTotalXP).orElse(0);
            System.out.println("📊 XP globaux actuels: " + xpGlobauxActuels);

            // 3. Si il y a une différence, ajuster
            if (totalXPParcours > 0 && xpGlobauxActuels < totalXPParcours) {
                int difference = totalXPParcours - xpGlobauxActuels;
                System.out.println("⚖️ Différence détectée: +" + difference + " XP à ajouter");
                
                try {
                    gamificationService.awardXP(user, difference, "Synchronisation XP parcours");
                    System.out.println("✅ XP synchronisés: +" + difference);
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur synchronisation: " + e.getMessage());
                }
            } else {
                System.out.println("✅ XP déjà synchronisés");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du recalcul XP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtient un résumé des XP d'un utilisateur
     */
    public String getXPSummary(User user) {
        StringBuilder summary = new StringBuilder();
        
        try {
            // XP globaux
            Optional<UserXP> userXPOpt = userXPRepository.findByUser(user);
            int xpGlobaux = userXPOpt.map(UserXP::getTotalXP).orElse(0);
            int niveau = userXPOpt.map(UserXP::getCurrentLevel).orElse(1);

            // XP des parcours
            int xpParcours = inscriptionRepository.findByUser(user).stream()
                .filter(ParcoursInscription::getIsCompleted)
                .mapToInt(inscription -> inscription.getPointsGagnes() != null ? inscription.getPointsGagnes() : 0)
                .sum();

            summary.append("=== RÉSUMÉ XP ===\n");
            summary.append("Utilisateur: ").append(user.getEmail()).append("\n");
            summary.append("XP Globaux: ").append(xpGlobaux).append("\n");
            summary.append("Niveau: ").append(niveau).append("\n");
            summary.append("XP Parcours: ").append(xpParcours).append("\n");
            summary.append("Différence: ").append(xpGlobaux - xpParcours).append("\n");
            summary.append("Synchronisé: ").append(xpGlobaux >= xpParcours ? "OUI" : "NON").append("\n");

        } catch (Exception e) {
            summary.append("Erreur: ").append(e.getMessage()).append("\n");
        }

        return summary.toString();
    }
}