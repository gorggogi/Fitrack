package com.app.fitrack.service;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.repository.UserRepository;
import com.app.fitrack.repository.MealRepository;
import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;


@Service
public class UserService {
    @Autowired
    private UserRepository repo;

    @Autowired
    private MealRepository mealRepository;
    
    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        User user = findByEmail(loginRequest.getEmail());
        
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            return new LoginResponse(true, "null");
        }
        
        return new LoginResponse(false, "Invalid email or password.Please try again");
    }
    
    public String save(User user) throws DuplicateEmailException {
        if (repo.findByEmail(user.getEmail()) != null) {
            throw new DuplicateEmailException("An an account with this email already exists.");
        }
        repo.save(user);
        return "redirect:/user/success";
    }

      public Meal saveMeal(Meal meal) {
        return mealRepository.save(meal);
    }

    public List<Meal> getMealsForCurrentDate() {
        LocalDate today = LocalDate.now();
        List<Meal> meals = mealRepository.findByDateTimeBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
        
        // Log meals and check for null dateTime
        for (Meal meal : meals) {
            if (meal.getDateTime() == null) {
                System.out.println("Meal with ID " + meal.getId() + " has a null dateTime.");
            }
        }
        
        return meals;
    }

    public int getTotalCaloriesForCurrentDate() {
        LocalDate today = LocalDate.now();
        List<Meal> meals = mealRepository.findByDateTimeBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
        return meals.stream().mapToInt(Meal::getCalories).sum();
    }

}
