package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.LeaderboardResponse;
import com.kawi_niveau.backend.entity.Level;
import com.kawi_niveau.backend.entity.UserXP;
import com.kawi_niveau.backend.repository.LevelRepository;
import com.kawi_niveau.backend.repository.UserBadgeRepository;
import com.kawi_niveau.backend.repository.UserXPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class LeaderboardService {

    @Autowired
    private UserXPRepository userXPRepository;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    public LeaderboardResponse getLeaderboard(int limit) {
        List<UserXP> topUsers = userXPRepository.findAllOrderByTotalXPDesc();
        
        // Si limit est 0, retourner tous les utilisateurs
        if (limit > 0 && topUsers.size() > limit) {
            topUsers = topUsers.subList(0, limit);
        }

        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
        for (int i = 0; i < topUsers.size(); i++) {
            UserXP userXP = topUsers.get(i);
            LeaderboardResponse.LeaderboardEntry entry = convertToLeaderboardEntry(userXP, i + 1);
            entries.add(entry);
        }

        return new LeaderboardResponse(entries);
    }

    public LeaderboardResponse getLeaderboard(Pageable pageable) {
        Page<UserXP> userXPPage = userXPRepository.findAllOrderByTotalXPDesc(pageable);
        
        List<LeaderboardResponse.LeaderboardEntry> entries = new ArrayList<>();
        List<UserXP> content = userXPPage.getContent();
        
        for (int i = 0; i < content.size(); i++) {
            UserXP userXP = content.get(i);
            int rank = (int) (pageable.getOffset() + i + 1);
            LeaderboardResponse.LeaderboardEntry entry = convertToLeaderboardEntry(userXP, rank);
            entries.add(entry);
        }

        return new LeaderboardResponse(entries);
    }

    public LeaderboardResponse.LeaderboardEntry getUserRanking(Long userId) {
        List<UserXP> allUsers = userXPRepository.findAllOrderByTotalXPDesc();
        
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUser().getId().equals(userId)) {
                return convertToLeaderboardEntry(allUsers.get(i), i + 1);
            }
        }
        
        throw new RuntimeException("Utilisateur non trouvé dans le classement");
    }

    private LeaderboardResponse.LeaderboardEntry convertToLeaderboardEntry(UserXP userXP, int rank) {
        // Obtenir le nom du niveau
        String levelName = "Niveau " + userXP.getCurrentLevel();
        Optional<Level> level = levelRepository.findByLevel(userXP.getCurrentLevel());
        if (level.isPresent()) {
            levelName = level.get().getName();
        }

        // Compter les badges
        long badgesCount = userBadgeRepository.countByUser(userXP.getUser());

        return new LeaderboardResponse.LeaderboardEntry(
                userXP.getUser().getId(),
                userXP.getUser().getFirstName(),
                userXP.getUser().getLastName(),
                userXP.getUser().getEmail(),
                userXP.getTotalXP(),
                userXP.getCurrentLevel(),
                levelName,
                badgesCount,
                rank
        );
    }
}