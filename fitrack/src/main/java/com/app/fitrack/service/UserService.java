package com.app.fitrack.service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.app.fitrack.model.User;
import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.repository.UserRepository;
import com.app.fitrack.repository.VerificationTokenRepository;
import org.springframework.security.core.Authentication;

@Service
@Transactional 
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${app.base-url}") 
    private String baseUrl;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
        String email = authentication.getName();
        return findByEmail(email);
    }
    return null;
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

   
        emailService.sendVerificationEmail(user);
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

    @Transactional
    public String generatePasswordResetToken(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
        return "User not found.";
    }

    String token = UUID.randomUUID().toString();


    emailService.sendPasswordResetEmail(user, token);

    return "Password reset link sent to your email.";
}


@Transactional
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

    String email = verificationToken.getUser().getEmail();
    User user = userRepository.findByEmail(email);

    if (user == null) {
        return "User not found.";
    }

    System.out.println("Old hashed password: " + user.getPassword());
    String hashed = hashPassword(newPassword);
    System.out.println("New hashed password: " + hashed);

    user.setPassword(hashed);  
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

        emailService.sendVerificationEmail(user);
        return "A new verification code has been sent to your email.";
    }
}
