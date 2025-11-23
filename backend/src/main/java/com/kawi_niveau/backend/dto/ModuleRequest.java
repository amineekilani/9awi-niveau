package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModuleRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    private String contenu;
    
    private Integer ordre;
}
