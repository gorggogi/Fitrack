package com.app.fitrack.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.app.fitrack.model.User;
import com.app.fitrack.model.VerificationToken;
import com.app.fitrack.repository.UserRepository;
import com.app.fitrack.repository.VerificationTokenRepository;
import org.springframework.security.core.Authentication;
import com.app.fitrack.model.WorkoutLog;
import com.app.fitrack.repository.WorkoutLogRepository;
import com.app.fitrack.model.BodyMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Service
@Transactional 
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WorkoutLogRepository workoutLogRepository;

    @Autowired
    private BodyMeasurementService bodyMeasurementService;

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

    public void registerUser(User user) throws DuplicateEmailException {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        String hashed = passwordEncoder.encode(newPassword);
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

    @Transactional
    public String createUserProfile(String email, Integer age, String gender, Double height, Double weight, String profilePicture) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found.";
        }

        if (age != null) user.setAge(age);
        if (gender != null) user.setGender(gender);
        if (height != null) user.setHeight(height);
        if (weight != null) user.setWeight(weight);
        if (profilePicture != null) user.setProfilePicture(profilePicture);

        userRepository.save(user);

        // Create initial body measurement record
        if (weight != null) {
            BodyMeasurement initialMeasurement = new BodyMeasurement();
            initialMeasurement.setUser(user);
            initialMeasurement.setWeight(weight);
            initialMeasurement.setDateTime(LocalDateTime.now());
            initialMeasurement.setNotes("Initial measurement from profile creation");
            bodyMeasurementService.saveMeasurement(initialMeasurement);
        }

        return "Profile created successfully.";
    }

    // Add a general save method
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public String updateUserProfile(String currentEmail, User updatedUserData, String profilePicturePath) {
        User user = findByEmail(currentEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + currentEmail);
        }

        // Check if email is being changed
        boolean emailChanged = !currentEmail.equalsIgnoreCase(updatedUserData.getEmail());

        if (emailChanged) {
            // Email change is handled separately via verification flow.
            // We only update other fields here.
            logger.info("Email change detected for user {}. Deferring email update to verification flow.", currentEmail);
        } else {
            // If email is NOT changing, update it (in case casing changed etc.)
             user.setEmail(updatedUserData.getEmail());
        }

        // Update other fields (excluding email if changed)
        user.setFirstName(updatedUserData.getFirstName());
        user.setLastName(updatedUserData.getLastName());
        user.setAge(updatedUserData.getAge());
        user.setGender(updatedUserData.getGender());
        user.setHeight(updatedUserData.getHeight());
        user.setWeight(updatedUserData.getWeight());
        user.setProfilePicture(profilePicturePath); // Update profile picture path

        userRepository.save(user);
        logger.info("Profile updated (excluding potential email change) for user {}", currentEmail);

        if (emailChanged) {
            return "Profile updated. Please verify your new email address to complete the change.";
        }
        return "Profile updated successfully!";
    }

    public double calculateBMR(User user) {
        if (user == null || user.getWeight() == null || user.getHeight() == null || user.getAge() == null || user.getGender() == null) {
            // Log this? Return 0 or throw exception?
            // Returning 0 for now, assuming TDEE calculation will handle it.
            return 0.0; 
        }

        double weight = user.getWeight(); // kg
        double height = user.getHeight(); // cm
        int age = user.getAge(); // years
        String gender = user.getGender();

        double bmr;
        if ("Male".equalsIgnoreCase(gender)) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else if ("Female".equalsIgnoreCase(gender)) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        } else {
            // Handle "Other" or unspecified - perhaps average the male/female results or use a unisex formula?
            // For now, averaging male and female as a rough estimate
            double bmrMale = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            double bmrFemale = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            bmr = (bmrMale + bmrFemale) / 2.0;
        }
        return bmr > 0 ? bmr : 0.0; // Ensure BMR is not negative
    }

    public double calculateTDEE(User user, double activityFactor) {
        double bmr = calculateBMR(user);
        return bmr * activityFactor;
    }

    public double calculateDynamicActivityFactor(User user, int days) {
        if (user == null) {
            return 1.2; // Default sedentary factor
        }

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        // Get workout logs for the period
        List<WorkoutLog> recentWorkouts = workoutLogRepository.findByUserAndCompletedAtBetween(user, startDate, endDate);
        
        if (recentWorkouts.isEmpty()) {
            return 1.2; // Default sedentary factor
        }

        // Calculate average daily exercise minutes
        double totalExerciseMinutes = recentWorkouts.stream()
            .mapToDouble(WorkoutLog::getDuration)
            .sum();
        double avgDailyExerciseMinutes = totalExerciseMinutes / days;

        // Calculate average daily calories burned
        double totalCaloriesBurned = recentWorkouts.stream()
            .mapToDouble(WorkoutLog::getCaloriesBurned)
            .sum();
        double avgDailyCaloriesBurned = totalCaloriesBurned / days;

        // Calculate workout frequency (days with workouts)
        long daysWithWorkouts = recentWorkouts.stream()
            .map(w -> w.getCompletedAt().toLocalDate())
            .distinct()
            .count();
        double workoutFrequency = (double) daysWithWorkouts / days;

        // Calculate intensity based on calories burned per minute
        double avgCaloriesPerMinute = 0.0;
        if (avgDailyExerciseMinutes > 0) {
            avgCaloriesPerMinute = avgDailyCaloriesBurned / avgDailyExerciseMinutes;
        }

        // Base activity factor calculation
        double baseActivityFactor = 1.2; // Start with sedentary

        // Adjust based on workout frequency (scientifically validated ranges)
        if (workoutFrequency >= 0.7) { // 5+ days per week
            baseActivityFactor += 0.3; // Very active
        } else if (workoutFrequency >= 0.4) { // 3-4 days per week
            baseActivityFactor += 0.2; // Moderately active
        } else if (workoutFrequency >= 0.1) { // 1-2 days per week
            baseActivityFactor += 0.1; // Lightly active
        }

        // Adjust based on intensity (calories per minute)
        if (avgCaloriesPerMinute > 8) { // High intensity (>8 kcal/min)
            baseActivityFactor += 0.2;
        } else if (avgCaloriesPerMinute > 5) { // Moderate intensity (>5 kcal/min)
            baseActivityFactor += 0.1;
        }

        // Adjust based on duration (exercise physiology research)
        if (avgDailyExerciseMinutes > 60) {
            baseActivityFactor += 0.1; // Extended duration impact
        } else if (avgDailyExerciseMinutes > 30) {
            baseActivityFactor += 0.05; // Moderate duration impact
        }

        // Recovery day adjustment
        // Reduce activity factor if consecutive high-intensity days
        boolean hasConsecutiveIntenseDays = false;
        java.time.LocalDate previousDate = null;
        int consecutiveIntenseDays = 0;
        
        for (WorkoutLog workout : recentWorkouts) {
            java.time.LocalDate currentDate = workout.getCompletedAt().toLocalDate();
            if (previousDate != null && currentDate.equals(previousDate.plusDays(1))) {
                consecutiveIntenseDays++;
            } else {
                consecutiveIntenseDays = 1;
            }
            
            if (consecutiveIntenseDays >= 3) {
                hasConsecutiveIntenseDays = true;
                break;
            }
            
            previousDate = currentDate;
        }
        
        if (hasConsecutiveIntenseDays) {
            baseActivityFactor -= 0.05; // Account for potential overtraining
        }

        // Cap the activity factor between 1.2 and 1.9 (scientifically validated range)
        return Math.min(Math.max(baseActivityFactor, 1.2), 1.9);
    }

    // Method to initiate email change verification
    public void requestEmailChangeVerification(User user, String newEmail) throws DuplicateEmailException {
        // 1. Check if the new email is already in use by another user
        User existingUserWithNewEmail = userRepository.findByEmail(newEmail);
        if (existingUserWithNewEmail != null && !existingUserWithNewEmail.getId().equals(user.getId())) {
            throw new DuplicateEmailException("Email address " + newEmail + " is already registered.");
        }

        // 2. Generate verification code and expiry
        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); // Simple 6-char code
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1); // Code valid for 1 hour

        // 3. Store pending email, code, and expiry on the user object
        user.setPendingEmail(newEmail);
        user.setEmailChangeCode(code);
        user.setEmailChangeCodeExpiry(expiryTime);

        // 4. Save the user with pending changes
        userRepository.save(user);

        // 5. Send verification email to the *new* email address
        emailService.sendVerificationEmail(newEmail, code); 

        logger.info("Sent email change verification code to {} for user {}", newEmail, user.getEmail());
    }

    // Method to verify the code and complete the email change
    public boolean verifyEmailChange(User user, String code) {
        if (user.getPendingEmail() == null || user.getEmailChangeCode() == null || user.getEmailChangeCodeExpiry() == null) {
            logger.warn("Email change verification attempt for user {} with no pending change data.", user.getEmail());
            return false; // No pending change
        }

        // Check if code matches and is not expired
        boolean codeMatches = code.equals(user.getEmailChangeCode());
        boolean codeNotExpired = LocalDateTime.now().isBefore(user.getEmailChangeCodeExpiry());

        if (codeMatches && codeNotExpired) {
            String oldEmail = user.getEmail();
            String newEmail = user.getPendingEmail();
            logger.info("Email change code verified for user {}. Updating email to {}.", oldEmail, newEmail);

            // Update email and mark as verified
            user.setEmail(newEmail);
            user.setVerified(true); // Assume changing email requires verification

            // Clear pending change fields
            user.setPendingEmail(null);
            user.setEmailChangeCode(null);
            user.setEmailChangeCodeExpiry(null);

            // Save the user
            userRepository.save(user);

            // IMPORTANT: Update Spring Security Context
            updateSecurityContext(oldEmail, newEmail, user);

            return true;
        } else {
            if (!codeMatches) {
                 logger.warn("Invalid email change code provided for user {}", user.getEmail());
            } 
            if (!codeNotExpired) {
                 logger.warn("Expired email change code provided for user {}", user.getEmail());
                 // Optionally clear expired code fields here
                 // user.setPendingEmail(null);
                 // user.setEmailChangeCode(null);
                 // user.setEmailChangeCodeExpiry(null);
                 // userRepository.save(user);
            }
            return false;
        }
    }

    // Helper method to update Spring Security Context after successful email change
    private void updateSecurityContext(String oldEmail, String newEmail, User user) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.getName().equals(oldEmail)) {
            // Recreate the UserDetails or principal object if necessary based on your CustomUserDetailsService
            // Assuming your principal is the standard UserDetails:
             UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(newEmail)
                .password(user.getPassword()) // Use the existing encoded password
                .authorities("ROLE_USER") // Or fetch roles dynamically if needed
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isVerified()) 
                .build();

            // Create a new Authentication token with the updated principal
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                userDetails, 
                currentAuth.getCredentials(), // Keep existing credentials (usually null after authentication)
                userDetails.getAuthorities());
            newAuth.setDetails(currentAuth.getDetails()); // Keep existing details (like IP, Session ID)

            // Set the new Authentication object in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            logger.info("Updated SecurityContext for user. Old email: {}, New email: {}", oldEmail, newEmail);
        } else {
            logger.warn("Could not update SecurityContext. Authentication mismatch or null. Old email: {}, New email: {}", oldEmail, newEmail);
        }
    }
}
