package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Challenge;
import com.kawi_niveau.backend.entity.User;
import com.kawi_niveau.backend.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    
    Optional<UserChallenge> findByUserAndChallenge(User user, Challenge challenge);
    
    List<UserChallenge> findByUser(User user);
    
    List<UserChallenge> findByChallenge(Challenge challenge);
    
    List<UserChallenge> findByUserAndIsCompletedTrue(User user);
    
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.user = :user AND uc.isCompleted = true")
    long countCompletedChallengesByUser(User user);
    
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge = :challenge")
    long countParticipantsByChallenge(Challenge challenge);
    
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge = :challenge AND uc.isCompleted = true")
    long countCompletedByChallenge(Challenge challenge);
    
    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.isCompleted = true")
    long countTotalChallengesCompleted();
}