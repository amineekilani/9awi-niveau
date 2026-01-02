package com.kawi_niveau.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/logo")
@CrossOrigin(origins = "http://localhost:4200")
public class LogoController {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<Resource> getLogo() {
        try {
            Path logoPath = Paths.get(uploadDir).resolve("Logo_9awi_Niveau.png");
            Resource resource = new UrlResource(logoPath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Logo_9awi_Niveau.png\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}