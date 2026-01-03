package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoursRequest {
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    private String categorie;

    private String thumbnailUrl;

    private String keywords;

    @NotNull(message = "Le niveau de difficulté est obligatoire")
    private NiveauDifficulte niveauDifficulte = NiveauDifficulte.DEBUTANT;
}
