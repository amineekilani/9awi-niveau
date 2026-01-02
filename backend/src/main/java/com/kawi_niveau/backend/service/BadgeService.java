package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.BadgeRequest;
import com.kawi_niveau.backend.dto.BadgeResponse;
import com.kawi_niveau.backend.entity.Badge;
import com.kawi_niveau.backend.entity.BadgeCriteriaType;
import com.kawi_niveau.backend.repository.BadgeRepository;
import com.kawi_niveau.backend.repository.UserBadgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    public Page<BadgeResponse> getAllBadges(Pageable pageable) {
        Page<Badge> badges = badgeRepository.findAll(pageable);
        return badges.map(this::convertToResponse);
    }

    public List<BadgeResponse> getActiveBadges() {
        List<Badge> badges = badgeRepository.findByIsActiveTrue();
        return badges.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BadgeResponse getBadgeById(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));
        return convertToResponse(badge);
    }

    public BadgeResponse createBadge(BadgeRequest request) {
        if (badgeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Un badge avec ce nom existe déjà");
        }

        Badge badge = new Badge();
        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(request.getIconUrl());
        badge.setCriteriaType(BadgeCriteriaType.valueOf(request.getCriteriaType()));
        badge.setCriteriaValue(request.getCriteriaValue());
        badge.setIsActive(request.isActive());

        Badge savedBadge = badgeRepository.save(badge);
        return convertToResponse(savedBadge);
    }

    public BadgeResponse updateBadge(Long id, BadgeRequest request) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));

        // Vérifier si le nom existe déjà (sauf pour ce badge)
        if (!badge.getName().equals(request.getName()) && 
            badgeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Un badge avec ce nom existe déjà");
        }

        badge.setName(request.getName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(request.getIconUrl());
        badge.setCriteriaType(BadgeCriteriaType.valueOf(request.getCriteriaType()));
        badge.setCriteriaValue(request.getCriteriaValue());
        badge.setIsActive(request.isActive());

        Badge savedBadge = badgeRepository.save(badge);
        return convertToResponse(savedBadge);
    }

    public void deleteBadge(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));

        // Vérifier si des utilisateurs ont ce badge
        long usersWithBadge = userBadgeRepository.countByBadge(badge);
        if (usersWithBadge > 0) {
            throw new RuntimeException("Impossible de supprimer ce badge car " + usersWithBadge + " utilisateur(s) l'ont obtenu");
        }

        badgeRepository.delete(badge);
    }

    public void toggleBadgeStatus(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge non trouvé"));

        System.out.println("=== DEBUG Toggle Badge Status ===");
        System.out.println("Badge ID: " + id);
        System.out.println("Current status: " + badge.getIsActive());
        
        Boolean currentStatus = badge.getIsActive();
        Boolean newStatus = (currentStatus != null) ? !currentStatus : true;
        
        badge.setIsActive(newStatus);
        badgeRepository.save(badge);
        
        System.out.println("New status: " + newStatus);
    }

    private BadgeResponse convertToResponse(Badge badge) {
        long usersCount = userBadgeRepository.countByBadge(badge);
        
        System.out.println("=== DEBUG Badge conversion ===");
        System.out.println("Badge ID: " + badge.getId());
        System.out.println("Badge name: " + badge.getName());
        System.out.println("Badge isActive (raw): " + badge.getIsActive());
        System.out.println("Badge isActive (boolean): " + (badge.getIsActive() != null ? badge.getIsActive() : false));
        
        return new BadgeResponse(
                badge.getId(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getCriteriaType().name(),
                badge.getCriteriaValue(),
                badge.getIsActive() != null ? badge.getIsActive() : false,
                badge.getCreatedAt(),
                badge.getUpdatedAt(),
                usersCount
        );
    }
}