package com.app.fitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import com.app.fitrack.model.User;
import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.repository.VerificationTokenRepository;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public void sendVerificationEmail(User user) {
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

    public void sendPasswordResetEmail(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(verificationToken);
    
        String resetUrl = "http://localhost:8080/user/change-password?token=" + token;
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
