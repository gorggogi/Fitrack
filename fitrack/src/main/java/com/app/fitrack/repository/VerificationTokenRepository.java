package com.app.fitrack.repository;

import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByCode(String code);    
    Optional<VerificationToken> findByUser(User user);  
    void deleteByUser(User user);
}