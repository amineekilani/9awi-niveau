package com.kawi_niveau.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LeconFileService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private static final String LECONS_DIR = "lecons";

    public String saveLeconFile(MultipartFile file, String typeContenu) throws IOException {
        // Valider le type de fichier selon le type de contenu
        validateFileType(file, typeContenu);

        // Créer le répertoire s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir, LECONS_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public void deleteLeconFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, LECONS_DIR, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log l'erreur mais ne pas lancer d'exception
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    private void validateFileType(MultipartFile file, String typeContenu) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (contentType == null || filename == null) {
            throw new IllegalArgumentException("Fichier invalide");
        }

        switch (typeContenu) {
            case "PDF":
                if (!contentType.equals("application/pdf")) {
                    throw new IllegalArgumentException("Le fichier doit être un PDF");
                }
                break;
            case "IMAGE":
                if (!contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("Le fichier doit être une image");
                }
                break;
            case "VIDEO":
                if (!contentType.startsWith("video/")) {
                    throw new IllegalArgumentException("Le fichier doit être une vidéo");
                }
                break;
            default:
                throw new IllegalArgumentException("Type de contenu non supporté pour l'upload");
        }
    }
}
