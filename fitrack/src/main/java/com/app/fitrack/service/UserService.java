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
    public String createUserProfile(String email, Integer age, String gender, Double height, Double weight) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found.";
        }

        if (age != null) user.setAge(age);
        if (gender != null) user.setGender(gender);
        if (height != null) user.setHeight(height);
        if (weight != null) user.setWeight(weight);

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
        double avgCaloriesPerMinute = avgDailyCaloriesBurned / avgDailyExerciseMinutes;

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
}
