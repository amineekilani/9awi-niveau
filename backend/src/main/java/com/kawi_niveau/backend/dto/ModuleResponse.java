package com.kawi_niveau.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleResponse {
    private Long id;
    private String titre;
    private String contenu;
    private Integer ordre;
    private Long createdAt;
    private Long updatedAt;
    private Long coursId;
}
