package com.app.fitrack.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;
import com.app.fitrack.model.User;
import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.repository.UserRepository;
import com.app.fitrack.repository.VerificationTokenRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}") 
    private String baseUrl;

    private String currentUserEmail;


    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public LoginResponse login(LoginRequest loginRequest) {
        User user = findByEmail(loginRequest.getEmail());
        
        if (user == null || !BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            return new LoginResponse(false, "Invalid email or password. Please try again.");
        }
        
        if (!user.isVerified()) {
            return new LoginResponse(false, "Please verify your email before logging in.");
        }
        
        setCurrentUserEmail(loginRequest.getEmail());
        return new LoginResponse(true, null);
    }

  
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }


    public void registerUser(User user) throws DuplicateEmailException {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }

        user.setPassword(hashPassword(user.getPassword()));
        user.setVerified(false);  
        userRepository.save(user); 
        sendVerificationEmail(user);
    }

    private void sendVerificationEmail(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        VerificationToken verificationToken = new VerificationToken(code, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(verificationToken);

        String subject = "Your Fitrack Verification Code";
        String body = "Your verification code is: " + code + "\nThis code will expire in 30 minutes.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }


    @Transactional
    public String verifyUser(String code) {
        Optional<VerificationToken> tokenOptional = tokenRepository.findByCode(code);

        if (tokenOptional.isEmpty()) {
            return "Invalid verification code.";
        }

        VerificationToken token = tokenOptional.get();

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Verification code has expired.";
        }

        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user); 
        tokenRepository.delete(token);

        return "Email successfully verified!";
    }

    public String getCurrentUserFullName() {
        if (currentUserEmail == null) {
            return "Guest";
        }
        User user = findByEmail(currentUserEmail);
        return (user != null) ? user.getFirstName() + " " + user.getLastName() : "Unknown User";
    }


    public User getCurrentUser() {
        return (currentUserEmail != null) ? findByEmail(currentUserEmail) : null;
    }

    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
    }
}
