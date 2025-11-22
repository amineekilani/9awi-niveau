package com.kawi_niveau.backend.controller;

import com.kawi_niveau.backend.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "http://localhost:4200")
public class ImageUploadController {

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Upload une image de profil utilisateur
     * @param file Le fichier image
     * @return Le nom du fichier sauvegardé
     */
    @PostMapping("/users/upload")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new com.kawi_niveau.backend.dto.MessageResponse("Le fichier est vide"));
            }

            String filename = imageUploadService.saveProfileImage(file);
            return ResponseEntity.ok(new ImageUploadResponse(filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new com.kawi_niveau.backend.dto.MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.kawi_niveau.backend.dto.MessageResponse("Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    /**
     * Récupère une image de profil utilisateur
     * @param filename Le nom du fichier
     * @return Le fichier image
     */
    @GetMapping("/users/{filename}")
    public ResponseEntity<?> getProfileImage(@PathVariable String filename) {
        try {
            // Validation de sécurité: éviter les path traversal
            if (filename.contains("..") || filename.contains("/")) {
                return ResponseEntity.badRequest().build();
            }

            Path imagePath = imageUploadService.getImagePath(filename);
            
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = Files.readAllBytes(imagePath);
            
            // Déterminer le type de contenu basé sur l'extension
            String contentType = getContentType(filename);
            
            return ResponseEntity.ok()
                    .header("Access-Control-Allow-Origin", "http://localhost:4200")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                    .header("Cache-Control", "public, max-age=3600")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Détermine le type MIME basé sur l'extension du fichier
     * @param filename Le nom du fichier
     * @return Le type MIME
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "image/jpeg";
        };
    }

    /**
     * Classe interne pour la réponse d'upload
     */
    public static class ImageUploadResponse {
        public String filename;
        public String url;

        public ImageUploadResponse(String filename) {
            this.filename = filename;
            this.url = "/images/users/" + filename;
        }

        public String getFilename() {
            return filename;
        }

        public String getUrl() {
            return url;
        }
    }
}
