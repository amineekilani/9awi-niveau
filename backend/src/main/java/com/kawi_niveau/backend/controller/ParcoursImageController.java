package com.kawi_niveau.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcours")
@CrossOrigin(origins = "http://localhost:4200")
public class ParcoursImageController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadParcoursImage(@RequestParam("file") MultipartFile file, 
                                                Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            // Vérifier le type de fichier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Le fichier doit être une image");
            }

            // Vérifier la taille (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("L'image ne doit pas dépasser 5MB");
            }

            // Créer le répertoire s'il n'existe pas
            String parcoursUploadDir = uploadDir + "/parcours";
            File directory = new File(parcoursUploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Sauvegarder le fichier
            Path filePath = Paths.get(parcoursUploadDir, filename);
            Files.copy(file.getInputStream(), filePath);

            // Retourner le nom du fichier
            return ResponseEntity.ok().body("{\"filename\":\"" + filename + "\"}");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload: " + e.getMessage());
        }
    }
}