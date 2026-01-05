package com.kawi_niveau.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les images de parcours
        Path uploadPath = Paths.get(uploadDir);
        String uploadPathString = uploadPath.toFile().getAbsolutePath();
        
        registry.addResourceHandler("/images/parcours/**")
                .addResourceLocations("file:" + uploadPathString + "/parcours/");
        
        // Servir les images de cours (si pas déjà configuré)
        registry.addResourceHandler("/images/cours/**")
                .addResourceLocations("file:" + uploadPathString + "/cours/");
    }
}