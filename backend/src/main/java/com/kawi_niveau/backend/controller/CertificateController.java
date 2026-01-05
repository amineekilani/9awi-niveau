package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.entity.ParcoursInscription;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.ParcoursInscriptionRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import com.kawi_niveau.backend.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des certificats de parcours
 */
@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ParcoursInscriptionRepository inscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Télécharger un certificat de parcours par ID de parcours (pour compatibilité frontend)
     */
    @GetMapping("/download/parcours/{parcoursId}")
    public ResponseEntity<Resource> downloadCertificateByParcours(
            @PathVariable Long parcoursId,
            Authentication authentication) {
        
        try {
            System.out.println("📥 Demande téléchargement certificat par parcours - Parcours ID: " + parcoursId);
            
            // Vérifier l'authentification
            String email = authentication.getName();
            System.out.println("🔍 Email utilisateur connecté: " + email);
            
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            System.out.println("👤 Utilisateur trouvé - ID: " + user.getId() + ", Email: " + user.getEmail());

            // Trouver l'inscription de cet utilisateur pour ce parcours
            ParcoursInscription inscription = inscriptionRepository.findByUserIdAndParcoursId(user.getId(), parcoursId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée pour ce parcours"));
            
            System.out.println("📋 Inscription trouvée - ID: " + inscription.getId() + ", Parcours: " + inscription.getParcours().getTitre());

            // Vérifier que le parcours est terminé
            if (!inscription.getIsCompleted()) {
                throw new RuntimeException("Le parcours n'est pas encore terminé");
            }

            // Vérifier que le certificat est généré
            if (!inscription.getCertificatGenere()) {
                throw new RuntimeException("Le certificat n'est pas encore généré");
            }

            // Récupérer le fichier certificat
            Resource certificateResource = certificateService.getCertificateResource(inscription.getId());
            
            // Générer le nom de fichier pour le téléchargement
            String downloadFileName = certificateService.getDownloadFileName(inscription);

            System.out.println("✅ Téléchargement certificat autorisé pour: " + email);
            System.out.println("📄 Fichier: " + downloadFileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF) // PDF maintenant !
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + downloadFileName + "\"")
                    .body(certificateResource);

        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement certificat par parcours: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    /**
     * Télécharger un certificat de parcours
     */
    @GetMapping("/download/{inscriptionId}")
    public ResponseEntity<Resource> downloadCertificate(
            @PathVariable Long inscriptionId,
            Authentication authentication) {
        
        try {
            System.out.println("📥 Demande téléchargement certificat - Inscription: " + inscriptionId);
            
            // Vérifier l'authentification
            String email = authentication.getName();
            System.out.println("🔍 Email utilisateur connecté: " + email);
            
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            System.out.println("👤 Utilisateur trouvé - ID: " + user.getId() + ", Email: " + user.getEmail());

            // Récupérer l'inscription
            ParcoursInscription inscription = inscriptionRepository.findById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));
            System.out.println("📋 Inscription trouvée - ID: " + inscription.getId() + ", Parcours: " + inscription.getParcours().getTitre());
            System.out.println("👤 Propriétaire inscription - ID: " + inscription.getUser().getId() + ", Email: " + inscription.getUser().getEmail());

            // Vérifier que l'utilisateur est propriétaire de l'inscription
            if (!inscription.getUser().getId().equals(user.getId())) {
                System.err.println("❌ ERREUR PERMISSION: User connecté ID=" + user.getId() + " vs Propriétaire inscription ID=" + inscription.getUser().getId());
                throw new RuntimeException("Accès non autorisé à ce certificat");
            }
            System.out.println("✅ Vérification propriétaire OK");

            // Vérifier que le parcours est terminé
            if (!inscription.getIsCompleted()) {
                throw new RuntimeException("Le parcours n'est pas encore terminé");
            }

            // Vérifier que le certificat est généré
            if (!inscription.getCertificatGenere()) {
                throw new RuntimeException("Le certificat n'est pas encore généré");
            }

            // Récupérer le fichier certificat
            Resource certificateResource = certificateService.getCertificateResource(inscriptionId);
            
            // Générer le nom de fichier pour le téléchargement
            String downloadFileName = certificateService.getDownloadFileName(inscription);

            System.out.println("✅ Téléchargement certificat autorisé pour: " + email);
            System.out.println("📄 Fichier: " + downloadFileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF) // PDF maintenant !
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + downloadFileName + "\"")
                    .body(certificateResource);

        } catch (Exception e) {
            System.err.println("❌ Erreur téléchargement certificat: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

    /**
     * Vérifier si un certificat est disponible
     */
    @GetMapping("/check/{inscriptionId}")
    public ResponseEntity<?> checkCertificate(
            @PathVariable Long inscriptionId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            ParcoursInscription inscription = inscriptionRepository.findById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

            if (!inscription.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Accès non autorisé");
            }

            boolean available = inscription.getIsCompleted() && 
                              inscription.getCertificatGenere() && 
                              certificateService.certificateExists(inscriptionId);

            return ResponseEntity.ok().body("{\"available\": " + available + 
                                          ", \"completed\": " + inscription.getIsCompleted() + 
                                          ", \"generated\": " + inscription.getCertificatGenere() + "}");

        } catch (Exception e) {
            System.err.println("❌ Erreur vérification certificat: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Régénérer un certificat (pour les administrateurs/formateurs)
     */
    @PostMapping("/regenerate/{inscriptionId}")
    public ResponseEntity<?> regenerateCertificate(
            @PathVariable Long inscriptionId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmailAndArchivedFalse(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier les permissions (utilisateur propriétaire ou formateur)
            ParcoursInscription inscription = inscriptionRepository.findById(inscriptionId)
                    .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

            boolean isOwner = inscription.getUser().getId().equals(user.getId());
            boolean isFormateur = inscription.getParcours().getFormateur().getId().equals(user.getId());
            
            if (!isOwner && !isFormateur) {
                throw new RuntimeException("Accès non autorisé");
            }

            if (!inscription.getIsCompleted()) {
                throw new RuntimeException("Le parcours n'est pas terminé");
            }

            // Supprimer l'ancien certificat s'il existe
            certificateService.deleteCertificate(inscriptionId);

            // Générer un nouveau certificat
            String certificatUrl = certificateService.generateCertificate(inscription);
            
            // Mettre à jour l'inscription
            inscription.setCertificatGenere(true);
            inscription.setCertificatUrl(certificatUrl);
            inscriptionRepository.save(inscription);

            System.out.println("🔄 Certificat régénéré pour inscription: " + inscriptionId);

            return ResponseEntity.ok().body("{\"message\": \"Certificat régénéré avec succès\", \"url\": \"" + certificatUrl + "\"}");

        } catch (Exception e) {
            System.err.println("❌ Erreur régénération certificat: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}