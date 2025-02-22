package com.app.fitrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fitrack.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
@Query ("SELECT u FROM User u WHERE u.email=?1") 
User findByEmail (String email);
}
