package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeconResponse {
    private Long id;
    private String titre;
    private String typeContenu;
    private String contenuTexte;
    private String fichierUrl;
    private Integer ordre;
    private Integer duree;
    private Long createdAt;
    private Long updatedAt;
    private Long moduleId;
}
