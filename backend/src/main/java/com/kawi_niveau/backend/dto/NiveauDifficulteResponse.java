package com.kawi_niveau.backend.dto;

import com.kawi_niveau.backend.entity.NiveauDifficulte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NiveauDifficulteResponse {
    private NiveauDifficulte niveau;
    private String displayName;
    private String description;
    private String badgeColor;
    private String icon;
    
    public static NiveauDifficulteResponse fromNiveau(NiveauDifficulte niveau) {
        NiveauDifficulteResponse response = new NiveauDifficulteResponse();
        response.setNiveau(niveau);
        response.setDisplayName(niveau.getDisplayName());
        
        switch (niveau) {
            case DEBUTANT:
                response.setDescription("Aucun prérequis, concepts de base");
                response.setBadgeColor("success");
                response.setIcon("play-circle");
                break;
            case INTERMEDIAIRE:
                response.setDescription("Connaissances de base requises");
                response.setBadgeColor("warning");
                response.setIcon("trending-up");
                break;
            case AVANCE:
                response.setDescription("Expérience significative nécessaire");
                response.setBadgeColor("danger");
                response.setIcon("zap");
                break;
            case EXPERT:
                response.setDescription("Maîtrise complète du domaine");
                response.setBadgeColor("primary");
                response.setIcon("star");
                break;
        }
        
        return response;
    }
}