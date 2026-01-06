package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.CoursParNiveauDto;
import com.kawi_niveau.backend.dto.FormateurStatsResponse;
import com.kawi_niveau.backend.entity.Cours;
import com.kawi_niveau.backend.entity.NiveauDifficulte;
import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.repository.CoursRepository;
import com.kawi_niveau.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormateurService {

    @Autowired
    private CoursRepository coursRepository;

    @Autowired
    private UserRepository userRepository;

    public FormateurStatsResponse getFormateurStats(String email) {
        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (formateur.getRole() != Role.FORMATEUR) {
            throw new RuntimeException("Seuls les formateurs peuvent accéder à ces statistiques");
        }

        // Récupérer tous les cours du formateur
        List<Cours> coursList = coursRepository.findByFormateurOrderByCreatedAtDesc(formateur);

        // Calculer les statistiques globales
        int totalCours = coursList.size();
        int coursActifs = (int) coursList.stream().filter(c -> !c.isArchived()).count();

        // Pour l'instant, on met des valeurs par défaut pour totalApprenants et tauxReussiteMoyen
        // Ces données nécessiteraient des tables supplémentaires (inscriptions, résultats, etc.)
        int totalApprenants = 0;
        double tauxReussiteMoyen = 0.0;

        // Calculer la répartition par niveau
        Map<NiveauDifficulte, Long> coursParNiveauMap = coursList.stream()
                .filter(c -> !c.isArchived())
                .collect(Collectors.groupingBy(Cours::getNiveauDifficulte, Collectors.counting()));

        List<CoursParNiveauDto> coursParNiveau = new ArrayList<>();
        for (NiveauDifficulte niveau : NiveauDifficulte.values()) {
            long nombre = coursParNiveauMap.getOrDefault(niveau, 0L);
            double pourcentage = coursActifs > 0 ? (nombre * 100.0) / coursActifs : 0.0;

            coursParNiveau.add(new CoursParNiveauDto(
                    niveau.name(),
                    (int) nombre,
                    pourcentage
            ));
        }

        return new FormateurStatsResponse(
                totalCours,
                coursActifs,
                totalApprenants,
                tauxReussiteMoyen,
                coursParNiveau
        );
    }
}