package com.app.fitrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.util.List;
import java.time.LocalDateTime;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Temporal;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = true, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, name = "firstname", length = 45)
    private String firstName;

    @Column(nullable = false, name = "lastname", length = 45)
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meal> meals;

    @Column(nullable = false)
    private boolean verified = false;  

    @Column(nullable = true)
    private Integer age;

    @Column(nullable = true, length = 10)
    private String gender;

    @Column(nullable = true)
    private Double height; // in centimeters

    @Column(nullable = true)
    private Double weight; // in kilograms

    @Column(nullable = true)
    private String profilePicture; // Path to stored profile picture

    @Transient
    private String confirmPassword;

    // --- Fields for Email Change Verification ---
    @Column(name = "pending_email", nullable = true)
    private String pendingEmail; // Stores the email address waiting for verification

    @Column(name = "email_change_code", nullable = true)
    private String emailChangeCode; // Stores the verification code for the email change

    @Column(name = "email_change_code_expiry", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime emailChangeCodeExpiry; // Stores the expiry time for the code
    // --- End Email Change Fields ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // --- Getters and Setters for new fields ---
    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public String getEmailChangeCode() {
        return emailChangeCode;
    }

    public void setEmailChangeCode(String emailChangeCode) {
        this.emailChangeCode = emailChangeCode;
    }

    public LocalDateTime getEmailChangeCodeExpiry() {
        return emailChangeCodeExpiry;
    }

    public void setEmailChangeCodeExpiry(LocalDateTime emailChangeCodeExpiry) {
        this.emailChangeCodeExpiry = emailChangeCodeExpiry;
    }
    // --- End Getters and Setters ---
}