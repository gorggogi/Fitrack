package com.app.fitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.fitrack.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
@Query ("SELECT u FROM User u WHERE u.email=?1") 
User findByEmail (String email);

@Modifying
@Query("UPDATE User u SET u.verified = true WHERE u.id = :userId")
void updateUserVerified(@Param("userId") Long userId);

}
