package com.app.fitrack.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailUtil2 {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String recipient, String firstName, String token) {
        String subject = "Password Reset Request";
        String resetUrl = "http://localhost:8080/user/change-password?token=" + token;
        String body = "<p>Hi " + firstName + ",</p>" +
                      "<p>You requested a password reset. Click the link below to reset your password:</p>" +
                      "<p><a href='" + resetUrl + "'>Reset Password</a></p>";
    
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);  
            mailSender.send(message);
            System.out.println("Password reset email sent successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email.");
        }
    }
    
    
}
