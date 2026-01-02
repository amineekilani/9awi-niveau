package com.kawi_niveau.backend.repository;

import com.kawi_niveau.backend.entity.Role;
import com.kawi_niveau.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndArchivedFalse(String email);
    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByResetToken(String resetToken);
    Optional<User> findByDeleteToken(String deleteToken);
    
    // Keep original method for internal use (like archiving)
    Optional<User> findByEmail(String email);
    
    // Admin methods
    List<User> findByArchivedFalse();
    long countByArchivedFalse();
    long countByRoleAndArchivedFalse(Role role);
}