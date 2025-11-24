package com.kawi_niveau.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuration pour servir les fichiers statiques (images uploads)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Obtenir le chemin absolu du répertoire uploads/users
        String uploadsUsersPath = Paths.get(uploadDir, "users").toAbsolutePath().toUri().toString();
        
        // Obtenir le chemin absolu du répertoire uploads/lecons
        String uploadsLeconsPath = Paths.get(uploadDir, "lecons").toAbsolutePath().toUri().toString();

        registry
                .addResourceHandler("/images/users/**")
                .addResourceLocations(uploadsUsersPath + "/")
                .setCachePeriod(3600); // Cache 1 heure
                
        registry
                .addResourceHandler("/files/lecons/**")
                .addResourceLocations(uploadsLeconsPath + "/")
                .setCachePeriod(3600); // Cache 1 heure
                
        registry
                .addResourceHandler("/images/lecons/**")
                .addResourceLocations(uploadsLeconsPath + "/")
                .setCachePeriod(3600); // Cache 1 heure
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/images/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        registry.addMapping("/files/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
