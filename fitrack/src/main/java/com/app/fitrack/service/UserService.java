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
import java.util.UUID;
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

    @Autowired
    private MailUtil2 mailUtil2;

    @Autowired EmailService emailService;

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



    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found.";
        }
    
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(verificationToken);
    
   
        mailUtil2.sendMail(user.getEmail(), user.getFirstName(), token);
    
        return "Password reset link sent to your email.";
    }
    

public String resetPassword(String token, String newPassword) {
    Optional<VerificationToken> tokenOptional = tokenRepository.findByCode(token);

    if (tokenOptional.isEmpty()) {
        return "Invalid or expired token.";
    }

    VerificationToken verificationToken = tokenOptional.get();


    if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
        tokenRepository.delete(verificationToken);  
        return "Token has expired. Request a new password reset.";
    }

    User user = verificationToken.getUser();
    user.setPassword(hashPassword(newPassword));  
    userRepository.save(user);

    tokenRepository.delete(verificationToken); 

    return "Password successfully reset. You can now log in with your new password.";
}

public String resendVerificationCode(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
        return "No account found with this email.";
    }
    
    if (user.isVerified()) {
        return "Your email is already verified.";
    }

    emailService.sendVerificationEmail(user);  // 
    return "A new verification code has been sent to your email.";
}


}


