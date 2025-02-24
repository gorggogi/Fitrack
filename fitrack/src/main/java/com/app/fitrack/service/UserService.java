package com.app.fitrack.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.UserRepository;

import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;

@Service
public class UserService {
    @Autowired
    private UserRepository repo;



    private String currentUserEmail;

    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        User user = findByEmail(loginRequest.getEmail());
        
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            setCurrentUserEmail(loginRequest.getEmail());
            return new LoginResponse(true, "null");
        }
        
        return new LoginResponse(false, "Invalid email or password. Please try again");
    }
    
    public String save(User user) throws DuplicateEmailException {
        if (repo.findByEmail(user.getEmail()) != null) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }
        repo.save(user);
        return "redirect:/user/success";
    }

    public String getCurrentUserFullName() {
        if (currentUserEmail == null) {
            return "Guest";
        }
        User user = findByEmail(currentUserEmail);
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return "Unknown User";
    }

    public User getCurrentUser() {
        if (currentUserEmail == null) {
            return null;
        }
        return findByEmail(currentUserEmail);
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }

}
