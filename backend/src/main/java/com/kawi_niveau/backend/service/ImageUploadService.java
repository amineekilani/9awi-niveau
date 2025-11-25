package com.kawi_niveau.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageUploadService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    /**
     * Sauvegarde une image de profil utilisateur
     * @param file Le fichier image
     * @return Le nom du fichier sauvegardé
     * @throws IOException Si une erreur de fichier survient
     */
    public String saveProfileImage(MultipartFile file) throws IOException {
        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier le type de fichier (accepter uniquement les images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Seules les images sont acceptées");
        }

        // Créer le répertoire s'il n'existe pas
        String userUploadDir = uploadDir + File.separator + "users";
        Path uploadPath = Paths.get(userUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom unique pour le fichier
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        return filename;
    }

    /**
     * Supprime une image de profil
     * @param filename Le nom du fichier à supprimer
     */
    public void deleteProfileImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            String userUploadDir = uploadDir + File.separator + "users";
            Path filePath = Paths.get(userUploadDir).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log l'erreur mais ne pas lancer d'exception
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    /**
     * Récupère l'extension du fichier
     * @param filename Le nom du fichier
     * @return L'extension sans le point
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Obtient le chemin complet d'une image
     * @param filename Le nom du fichier
     * @return Le chemin complet du fichier
     */
    public Path getImagePath(String filename) {
        String userUploadDir = uploadDir + File.separator + "users";
        return Paths.get(userUploadDir).resolve(filename);
    }

    /**
     * Sauvegarde un thumbnail de cours
     * @param file Le fichier image
     * @return Le nom du fichier sauvegardé
     * @throws IOException Si une erreur de fichier survient
     */
    public String saveCoursThumbnail(MultipartFile file) throws IOException {
        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier le type de fichier (accepter uniquement les images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Seules les images sont acceptées");
        }

        // Créer le répertoire s'il n'existe pas
        String coursUploadDir = uploadDir + File.separator + "cours";
        Path uploadPath = Paths.get(coursUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom unique pour le fichier
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + "." + fileExtension;

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.write(filePath, file.getBytes());

        return filename;
    }

    /**
     * Obtient le chemin complet d'un thumbnail de cours
     * @param filename Le nom du fichier
     * @return Le chemin complet du fichier
     */
    public Path getCoursThumbnailPath(String filename) {
        String coursUploadDir = uploadDir + File.separator + "cours";
        return Paths.get(coursUploadDir).resolve(filename);
    }

    /**
     * Supprime un thumbnail de cours
     * @param filename Le nom du fichier à supprimer
     */
    public void deleteCoursThumbnail(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            String coursUploadDir = uploadDir + File.separator + "cours";
            Path filePath = Paths.get(coursUploadDir).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log l'erreur mais ne pas lancer d'exception
            System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }
}
