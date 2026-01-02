package com.kawi_niveau.backend.service;

import com.kawi_niveau.backend.dto.ChallengeRequest;
import com.kawi_niveau.backend.dto.ChallengeResponse;
import com.kawi_niveau.backend.entity.Challenge;
import com.kawi_niveau.backend.entity.ChallengeType;
import com.kawi_niveau.backend.repository.ChallengeRepository;
import com.kawi_niveau.backend.repository.UserChallengeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChallengeService {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserChallengeRepository userChallengeRepository;

    public Page<ChallengeResponse> getAllChallenges(Pageable pageable) {
        Page<Challenge> challenges = challengeRepository.findAll(pageable);
        return challenges.map(this::convertToResponse);
    }

    public List<ChallengeResponse> getActiveChallenges() {
        List<Challenge> challenges = challengeRepository.findByIsActiveTrue();
        return challenges.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ChallengeResponse getChallengeById(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Défi non trouvé"));
        return convertToResponse(challenge);
    }

    public ChallengeResponse createChallenge(ChallengeRequest request) {
        if (challengeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Un défi avec ce nom existe déjà");
        }

        Challenge challenge = new Challenge();
        challenge.setName(request.getName());
        challenge.setDescription(request.getDescription());
        challenge.setChallengeType(ChallengeType.valueOf(request.getChallengeType()));
        challenge.setTargetValue(request.getTargetValue());
        challenge.setXpReward(request.getXpReward());
        challenge.setStartDate(request.getStartDate());
        challenge.setEndDate(request.getEndDate());
        challenge.setIsActive(request.isActive());

        Challenge savedChallenge = challengeRepository.save(challenge);
        return convertToResponse(savedChallenge);
    }

    public ChallengeResponse updateChallenge(Long id, ChallengeRequest request) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Défi non trouvé"));

        // Vérifier si le nom existe déjà (sauf pour ce défi)
        if (!challenge.getName().equals(request.getName()) && 
            challengeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Un défi avec ce nom existe déjà");
        }

        challenge.setName(request.getName());
        challenge.setDescription(request.getDescription());
        challenge.setChallengeType(ChallengeType.valueOf(request.getChallengeType()));
        challenge.setTargetValue(request.getTargetValue());
        challenge.setXpReward(request.getXpReward());
        challenge.setStartDate(request.getStartDate());
        challenge.setEndDate(request.getEndDate());
        challenge.setIsActive(request.isActive());

        Challenge savedChallenge = challengeRepository.save(challenge);
        return convertToResponse(savedChallenge);
    }

    public void deleteChallenge(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Défi non trouvé"));

        // Vérifier si des utilisateurs participent à ce défi
        long participants = userChallengeRepository.countParticipantsByChallenge(challenge);
        if (participants > 0) {
            throw new RuntimeException("Impossible de supprimer ce défi car " + participants + " utilisateur(s) y participent");
        }

        challengeRepository.delete(challenge);
    }

    public void toggleChallengeStatus(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Défi non trouvé"));

        Boolean currentStatus = challenge.getIsActive();
        Boolean newStatus = (currentStatus != null) ? !currentStatus : true;
        challenge.setIsActive(newStatus);
        challengeRepository.save(challenge);
    }

    private ChallengeResponse convertToResponse(Challenge challenge) {
        long participantsCount = userChallengeRepository.countParticipantsByChallenge(challenge);
        long completedCount = userChallengeRepository.countCompletedByChallenge(challenge);
        
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                challenge.getChallengeType().name(),
                challenge.getTargetValue(),
                challenge.getXpReward(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getIsActive() != null ? challenge.getIsActive() : false,
                challenge.getCreatedAt(),
                challenge.getUpdatedAt(),
                participantsCount,
                completedCount
        );
    }
}