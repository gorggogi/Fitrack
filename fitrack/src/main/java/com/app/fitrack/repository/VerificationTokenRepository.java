package com.app.fitrack.repository;

import com.app.fitrack.model.VerificationToken;

import jakarta.transaction.Transactional;

import com.app.fitrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByCode(String code);    
    Optional<VerificationToken> findByUser(User user);  
    @Transactional
    @Modifying
    @Query("DELETE FROM VerificationToken v WHERE v.user = :user")
    void deleteByUser(User user);
}