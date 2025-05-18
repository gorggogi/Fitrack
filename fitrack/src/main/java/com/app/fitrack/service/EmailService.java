package com.app.fitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;
import com.app.fitrack.model.User;
import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.repository.VerificationTokenRepository;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    @Transactional
    public void sendVerificationEmail(User user) {

        tokenRepository.deleteByUser(user);
        tokenRepository.flush(); 

        String code = String.format("%06d", new Random().nextInt(999999));
        VerificationToken verificationToken = new VerificationToken(code, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.saveAndFlush(verificationToken); 

        String subject = "Your Fitrack Verification Code";
        String body = "Your verification code is: " + code + "\nThis code will expire in 30 minutes.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Sends an email with a verification code to a specified email address.
     * Used for verifying a *new* email address during profile updates.
     * This method does NOT generate or store a VerificationToken itself.
     *
     * @param email The email address to send the verification code to.
     * @param code The verification code to include in the email.
     */
    @Async
    public void sendVerificationEmail(String email, String code) {
        String subject = "Verify Your New Fitrack Email Address";
        // Consider making the expiry time configurable or passing it in if needed.
        String body = "Your verification code for the new email address is: " + code + "\nThis code will expire in 30 minutes."; 

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email); // Send to the new/pending email
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    @Transactional
    public void sendPasswordResetEmail(User user, String token) {
      
        tokenRepository.deleteByUser(user);
        tokenRepository.flush();  

        VerificationToken verificationToken = new VerificationToken(token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.saveAndFlush(verificationToken); 

        String resetUrl = baseUrl + "/user/change-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "Hi " + user.getFirstName() + ",\n\n" +
                      "You requested a password reset. Click the link below to reset your password:\n" +
                      resetUrl + "\n\n" +
                      "If you didn't request this, please ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
