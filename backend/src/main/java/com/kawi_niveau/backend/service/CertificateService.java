package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.ParcoursInscription;
import com.kawi_niveau.backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service de génération et gestion des certificats de parcours
 */
@Service
public class CertificateService {

    @Value("${app.certificates.storage-path:./certificates}")
    private String certificatesStoragePath;

    @Value("${app.certificates.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    private PdfCertificateService pdfCertificateService;

    /**
     * Génère un certificat PDF pour un parcours terminé
     */
    public String generateCertificate(ParcoursInscription inscription) {
        try {
            System.out.println("🏆 Génération certificat pour: " + inscription.getUser().getEmail() + 
                             " - Parcours: " + inscription.getParcours().getTitre());

            // Créer le répertoire de stockage s'il n'existe pas
            Path storageDir = Paths.get(certificatesStoragePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            // Générer un nom de fichier unique
            String fileName = generateFileName(inscription);
            Path certificatePath = storageDir.resolve(fileName);

            // Générer le certificat PDF avec iText 7
            try {
                byte[] pdfBytes = pdfCertificateService.generatePdfCertificate(inscription);
                Files.write(certificatePath, pdfBytes);
                System.out.println("✅ Certificat PDF généré avec iText 7");
            } catch (Exception pdfError) {
                System.err.println("⚠️ Erreur génération PDF, fallback vers texte: " + pdfError.getMessage());
                // Fallback vers le texte si le PDF échoue
                String certificateContent = generateCertificateContent(inscription);
                Files.write(certificatePath, certificateContent.getBytes());
            }

            // Retourner l'URL de téléchargement
            String downloadUrl = "/api/certificates/download/" + inscription.getId();
            
            System.out.println("✅ Certificat généré: " + fileName);
            System.out.println("📁 Chemin: " + certificatePath.toString());
            System.out.println("🔗 URL: " + downloadUrl);

            return downloadUrl;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération certificat: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du certificat", e);
        }
    }

    /**
     * Récupère un certificat pour téléchargement
     */
    public Resource getCertificateResource(Long inscriptionId) {
        try {
            String fileName = "certificate_" + inscriptionId + ".pdf"; // PDF maintenant
            Path certificatePath = Paths.get(certificatesStoragePath).resolve(fileName);

            if (!Files.exists(certificatePath)) {
                throw new RuntimeException("Certificat non trouvé");
            }

            Resource resource = new UrlResource(certificatePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Certificat non lisible");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération certificat: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du certificat", e);
        }
    }

    /**
     * Vérifie si un certificat existe pour une inscription
     */
    public boolean certificateExists(Long inscriptionId) {
        try {
            String fileName = "certificate_" + inscriptionId + ".pdf"; // PDF maintenant
            Path certificatePath = Paths.get(certificatesStoragePath).resolve(fileName);
            return Files.exists(certificatePath);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Génère le nom de fichier du certificat
     */
    private String generateFileName(ParcoursInscription inscription) {
        return "certificate_" + inscription.getId() + ".pdf"; // Maintenant en PDF !
    }

    /**
     * Génère le contenu du certificat
     * TODO: Remplacer par une génération PDF professionnelle
     */
    private String generateCertificateContent(ParcoursInscription inscription) {
        User user = inscription.getUser();
        ParcoursApprentissage parcours = inscription.getParcours();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateCompletion = inscription.getDateCompletion().format(formatter);
        
        StringBuilder content = new StringBuilder();
        content.append("═══════════════════════════════════════════════════════════════\n");
        content.append("                    CERTIFICAT DE RÉUSSITE                     \n");
        content.append("═══════════════════════════════════════════════════════════════\n\n");
        
        content.append("                    Décerné à :                               \n");
        content.append("              ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n\n");
        
        content.append("        Pour avoir terminé avec succès le parcours :         \n");
        content.append("                  \"").append(parcours.getTitre()).append("\"                  \n\n");
        
        content.append("                Date de completion : ").append(dateCompletion).append("                \n");
        content.append("                Formateur : ").append(parcours.getFormateur().getFirstName())
               .append(" ").append(parcours.getFormateur().getLastName()).append("                \n\n");
        
        if (parcours.getDureeEstimeeHeures() != null) {
            content.append("            Durée du parcours : ").append(parcours.getDureeEstimeeHeures()).append(" heures            \n");
        }
        
        if (inscription.getPointsGagnes() != null && inscription.getPointsGagnes() > 0) {
            content.append("              Points obtenus : ").append(inscription.getPointsGagnes()).append(" XP              \n");
        }
        
        content.append("\n═══════════════════════════════════════════════════════════════\n");
        content.append("           Certificat généré le : ").append(LocalDateTime.now().format(formatter)).append("           \n");
        content.append("                      9awi Niveau - Plateforme d'apprentissage                     \n");
        content.append("═══════════════════════════════════════════════════════════════\n");
        
        return content.toString();
    }

    /**
     * Supprime un certificat
     */
    public void deleteCertificate(Long inscriptionId) {
        try {
            String fileName = "certificate_" + inscriptionId + ".pdf"; // PDF maintenant
            Path certificatePath = Paths.get(certificatesStoragePath).resolve(fileName);
            
            if (Files.exists(certificatePath)) {
                Files.delete(certificatePath);
                System.out.println("🗑️ Certificat supprimé: " + fileName);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur suppression certificat: " + e.getMessage());
        }
    }

    /**
     * Obtient le nom de fichier pour le téléchargement
     */
    public String getDownloadFileName(ParcoursInscription inscription) {
        String parcoursTitre = inscription.getParcours().getTitre()
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replaceAll("\\s+", "_");
        
        return "Certificat_" + parcoursTitre + "_" + 
               inscription.getUser().getFirstName() + "_" + 
               inscription.getUser().getLastName() + ".pdf"; // PDF maintenant !
    }
}