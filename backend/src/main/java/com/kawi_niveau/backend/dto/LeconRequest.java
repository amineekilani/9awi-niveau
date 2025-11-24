package com.kawi_niveau.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeconRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    @NotNull(message = "Le type de contenu est obligatoire")
    private String typeContenu; // "TEXTE", "PDF", "IMAGE", "VIDEO"
    
    private String contenuTexte; // Pour le type TEXTE
    
    private String fichierUrl; // Pour PDF, IMAGE, VIDEO
    
    private Integer ordre;
    
    private Integer duree; // Durée en minutes
}
