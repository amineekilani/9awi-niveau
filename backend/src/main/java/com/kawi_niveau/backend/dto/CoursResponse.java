package com.kawi_niveau.backend.dto;

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
    private Long formateurId;
    private String formateurNom;
}
