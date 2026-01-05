package com.kawi_niveau.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursImageController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file,
                                        Authentication authentication) {
        try {
            // Vérifications de sécurité
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("Fichier trop volumineux (max 5MB)");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().body("Nom de fichier invalide");
            }

            // Vérifier l'extension
            String extension = getFileExtension(originalFilename).toLowerCase();
            boolean isValidExtension = false;
            for (String allowedExt : ALLOWED_EXTENSIONS) {
                if (extension.equals(allowedExt)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                return ResponseEntity.badRequest().body("Type de fichier non autorisé. Formats acceptés: JPG, PNG, GIF, WebP");
            }

            // Générer un nom unique
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir, "parcours");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Sauvegarder le fichier
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Retourner le nom du fichier
            Map<String, String> response = new HashMap<>();
            response.put("filename", uniqueFilename);
            response.put("url", "/images/parcours/" + uniqueFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-image/{filename}")
    public ResponseEntity<?> deleteImage(@PathVariable String filename,
                                        Authentication authentication) {
        try {
            // Vérifications de sécurité
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                return ResponseEntity.badRequest().body("Nom de fichier invalide");
            }

            Path filePath = Paths.get(uploadDir, "parcours", filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok().body("{\"message\": \"Image supprimée avec succès\"}");
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}