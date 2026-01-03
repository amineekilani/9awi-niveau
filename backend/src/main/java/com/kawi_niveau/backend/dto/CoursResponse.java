package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursResponse {
    private Long id;
    private String titre;
    private String description;
    private Long createdAt;
    private Long updatedAt;
    private boolean archived;
    private Long archivedAt;
    private String categorie;
    private String thumbnailUrl;
    private String keywords;
    private NiveauDifficulte niveauDifficulte;
    private String niveauDifficulteDisplay;
    private Long formateurId;
    private String formateurNom;
    private String formateurDomaine;
}
