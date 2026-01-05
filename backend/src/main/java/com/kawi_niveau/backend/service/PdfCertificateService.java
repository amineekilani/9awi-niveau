package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.entity.ParcoursApprentissage;
import com.kawi_niveau.backend.entity.ParcoursInscription;
import com.kawi_niveau.backend.entity.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Service moderne de génération de certificats PDF avec Apache PDFBox
 */
@Service
public class PdfCertificateService {
    // Couleurs personnalisées (ajustées pour un meilleur contraste et une apparence plus professionnelle)
    private static final PDColor PRIMARY_COLOR = new PDColor(new float[]{0.024f, 0.235f, 0.875f}, PDDeviceRGB.INSTANCE); // Bleu #063cdf
    private static final PDColor GOLD_COLOR = new PDColor(new float[]{0.961f, 0.620f, 0.043f}, PDDeviceRGB.INSTANCE); // Or #f59e0b
    private static final PDColor GRAY_COLOR = new PDColor(new float[]{0.420f, 0.447f, 0.502f}, PDDeviceRGB.INSTANCE); // Gris #6b7280
    private static final PDColor DARK_GRAY_COLOR = new PDColor(new float[]{0.2f, 0.2f, 0.2f}, PDDeviceRGB.INSTANCE); // Gris foncé pour plus de profondeur

    /**
     * Génère un certificat PDF professionnel avec Apache PDFBox
     */
    public byte[] generatePdfCertificate(ParcoursInscription inscription) throws IOException {
        User user = inscription.getUser();
        ParcoursApprentissage parcours = inscription.getParcours();

        try (PDDocument document = new PDDocument()) {
            // Page A4 paysage
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // Ajout d'un fond subtil pour plus d'élégance (lignes diagonales légères)
                addBackground(contentStream, page);

                // === EN-TÊTE ===
                addHeader(contentStream, page, document);

                // === TITRE PRINCIPAL ===
                addMainTitle(contentStream, page);

                // === INFORMATIONS UTILISATEUR ===
                addUserInfo(contentStream, page, user);

                // === INFORMATIONS PARCOURS ===
                addCourseInfo(contentStream, page, parcours, inscription);

                // === SIGNATURE ET PIED DE PAGE ===
                addFooter(contentStream, page, parcours, inscription);
            }
            // Convertir en bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void addBackground(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Lignes diagonales subtiles pour un effet watermark-like
        contentStream.setStrokingColor(new PDColor(new float[]{0.95f, 0.95f, 0.95f}, PDDeviceRGB.INSTANCE)); // Gris très clair
        contentStream.setLineWidth(0.5f);
        for (float i = -pageHeight; i < pageWidth; i += 100) {
            contentStream.moveTo(i, 0);
            contentStream.lineTo(i + pageHeight, pageHeight);
            contentStream.stroke();
        }
    }

    private void addHeader(PDPageContentStream contentStream, PDPage page, PDDocument document) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Logo stylisé en texte (amélioré avec une meilleure centrage et taille)
        float logoX = pageWidth / 2 - 120; // Meilleur centrage

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 32); // Taille ajustée
        contentStream.setNonStrokingColor(GOLD_COLOR);
        contentStream.newLineAtOffset(logoX + 40, pageHeight - 90); // Alignement ajusté
        contentStream.showText("9awiNiveau");
        contentStream.endText();

        // Sous-titre (police italique pour plus d'élégance)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 14);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - 140, pageHeight - 120);
        contentStream.showText("Plateforme d'Apprentissage Professionnelle");
        contentStream.endText();
    }

    private void addMainTitle(PDPageContentStream contentStream, PDPage page) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Lignes décoratives plus élégantes (avec dégradé simulé par épaisseur variable)
        contentStream.setStrokingColor(GOLD_COLOR);
        contentStream.setLineWidth(4);
        contentStream.moveTo(pageWidth / 2 - 250, pageHeight - 150);
        contentStream.lineTo(pageWidth / 2 + 250, pageHeight - 150);
        contentStream.stroke();

        contentStream.setLineWidth(2);
        contentStream.moveTo(pageWidth / 2 - 250, pageHeight - 152);
        contentStream.lineTo(pageWidth / 2 + 250, pageHeight - 152);
        contentStream.stroke();

        // Titre principal (avec ombre pour profondeur)
        // Ombre
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 40);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - 202, pageHeight - 200);
        contentStream.showText("CERTIFICAT DE REUSSITE");
        contentStream.endText();

        // Texte principal
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 40);
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - 200, pageHeight - 200);
        contentStream.showText("CERTIFICAT DE REUSSITE");
        contentStream.endText();

        // Ligne décorative inférieure
        contentStream.setStrokingColor(GOLD_COLOR);
        contentStream.setLineWidth(4);
        contentStream.moveTo(pageWidth / 2 - 250, pageHeight - 220);
        contentStream.lineTo(pageWidth / 2 + 250, pageHeight - 220);
        contentStream.stroke();

        contentStream.setLineWidth(2);
        contentStream.moveTo(pageWidth / 2 - 250, pageHeight - 222);
        contentStream.lineTo(pageWidth / 2 + 250, pageHeight - 222);
        contentStream.stroke();
    }

    private void addUserInfo(PDPageContentStream contentStream, PDPage page, User user) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // "Décerné à" (police plus grande et centrée)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 20);
        contentStream.setNonStrokingColor(DARK_GRAY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - 60, pageHeight - 270);
        contentStream.showText("Décerné à");
        contentStream.endText();

        // Nom de l'utilisateur (avec soulignement subtil)
        String fullName = user.getFirstName() + " " + user.getLastName();
        float nameWidth = fullName.length() * 10; // Approximation pour centrage
        float nameX = pageWidth / 2 - (nameWidth / 2);

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 32);
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.newLineAtOffset(nameX, pageHeight - 310);
        contentStream.showText(fullName);
        contentStream.endText();

        // Soulignement courbé pour élégance
        contentStream.setStrokingColor(PRIMARY_COLOR);
        contentStream.setLineWidth(2);
        contentStream.moveTo(nameX - 10, pageHeight - 320);
        contentStream.curveTo(nameX + nameWidth / 2, pageHeight - 330, nameX + nameWidth / 2, pageHeight - 330, nameX + nameWidth + 10, pageHeight - 320);
        contentStream.stroke();
    }

    private void addCourseInfo(PDPageContentStream contentStream, PDPage page,
                               ParcoursApprentissage parcours, ParcoursInscription inscription) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Texte de completion (aligné et espacé mieux)
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 16);
        contentStream.setNonStrokingColor(DARK_GRAY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - 220, pageHeight - 360);
        contentStream.showText("Pour avoir terminé avec succès le parcours d'apprentissage");
        contentStream.endText();

        // Nom du parcours (avec guillemets stylisés)
        String parcoursTitre = parcours.getTitre();
        float titreWidth = parcoursTitre.length() * 7; // Approximation
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 2 - (titreWidth / 2), pageHeight - 390);
        contentStream.showText(parcoursTitre);
        contentStream.endText();

        // Détails (dans une boîte pour meilleure organisation)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String completionDate = inscription.getDateCompletion().format(formatter);
        String formateurName = parcours.getFormateur().getFirstName() + " " + parcours.getFormateur().getLastName();

        float detailsY = 440;
        float detailsX = pageWidth / 2 - 200;
        float boxWidth = 400;
        float boxHeight = 100 + (parcours.getDureeEstimeeHeures() != null ? 25 : 0) + (inscription.getPointsGagnes() != null && inscription.getPointsGagnes() > 0 ? 25 : 0);

        // Boîte autour des détails (bordure légère)
        contentStream.setStrokingColor(GRAY_COLOR);
        contentStream.setLineWidth(1);
        contentStream.addRect(detailsX - 20, pageHeight - detailsY - boxHeight + 50, boxWidth + 40, boxHeight);
        contentStream.stroke();

        // Date de completion
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 14);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(detailsX, pageHeight - detailsY);
        contentStream.showText("Date de complétion : " + completionDate);
        contentStream.endText();
        detailsY += 30;

        // Formateur
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 14);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(detailsX, pageHeight - detailsY);
        contentStream.showText("Formateur : " + formateurName);
        contentStream.endText();
        detailsY += 30;

        // Durée si disponible
        if (parcours.getDureeEstimeeHeures() != null) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 14);
            contentStream.setNonStrokingColor(GRAY_COLOR);
            contentStream.newLineAtOffset(detailsX, pageHeight - detailsY);
            contentStream.showText("Durée du parcours : " + parcours.getDureeEstimeeHeures() + " heures");
            contentStream.endText();
            detailsY += 30;
        }

        // Points si disponibles
        if (inscription.getPointsGagnes() != null && inscription.getPointsGagnes() > 0) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 14);
            contentStream.setNonStrokingColor(GRAY_COLOR);
            contentStream.newLineAtOffset(detailsX, pageHeight - detailsY);
            contentStream.showText("Points obtenus : " + inscription.getPointsGagnes() + " XP");
            contentStream.endText();
        }
    }

    private void addFooter(PDPageContentStream contentStream, PDPage page,
                           ParcoursApprentissage parcours, ParcoursInscription inscription) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Lignes de signature plus longues et centrées
        contentStream.setStrokingColor(DARK_GRAY_COLOR);
        contentStream.setLineWidth(1.5f);

        // Signature formateur
        contentStream.moveTo(pageWidth / 4 - 100, 140);
        contentStream.lineTo(pageWidth / 4 + 100, 140);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(pageWidth / 4 - 50, 120);
        contentStream.showText("Formateur");
        contentStream.endText();

        // Signature plateforme
        contentStream.moveTo(3 * pageWidth / 4 - 100, 140);
        contentStream.lineTo(3 * pageWidth / 4 + 100, 140);
        contentStream.stroke();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        contentStream.newLineAtOffset(3 * pageWidth / 4 - 80, 120);
        contentStream.showText("Directeur Pédagogique");
        contentStream.endText();

        // Ligne décorative finale (plus épaisse)
        contentStream.setStrokingColor(GOLD_COLOR);
        contentStream.setLineWidth(3);
        contentStream.moveTo(40, 80);
        contentStream.lineTo(pageWidth - 40, 80);
        contentStream.stroke();

        // Informations de validation (police plus petite et centrée)
        String certificateId = "CERT-" + System.currentTimeMillis();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        String generationInfo = "Certificat ID: " + certificateId + " | " +
                "Généré le " + java.time.LocalDateTime.now().format(formatter) + " | " +
                "9awiNiveau.com";

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.setNonStrokingColor(GRAY_COLOR);
        float infoWidth = generationInfo.length() * 3; // Approximation
        contentStream.newLineAtOffset(pageWidth / 2 - (infoWidth / 2), 50);
        contentStream.showText(generationInfo);
        contentStream.endText();
    }
}