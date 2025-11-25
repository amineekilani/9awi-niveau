package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoursRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    private String description;
    
    private String categorie;
    
    private String thumbnailUrl;
}
