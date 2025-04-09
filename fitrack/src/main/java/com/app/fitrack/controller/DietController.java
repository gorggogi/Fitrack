package com.app.fitrack.controller;

import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.service.MealService;
import com.app.fitrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DietController {

    @Autowired
    private UserService userService;

    @Autowired
    private MealService mealService;

    // Define default reference macro percentages
    private static final int DEFAULT_PROTEIN_PERCENT = 30;
    private static final int DEFAULT_CARBS_PERCENT = 40;
    private static final int DEFAULT_FAT_PERCENT = 30;

    @GetMapping("/user/diet")
    public String showDietPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/user/login";
        }
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        if (user == null) {
            // Should not happen if userDetails is not null, but good practice
             return "redirect:/user/login";
        }

        // Get today's data
        List<Meal> todaysMeals = mealService.getMealsForCurrentDate();
        int todaysTotalCalories = mealService.getTotalCaloriesForCurrentDate();

        // Prepare data for the reference pie chart
        Map<String, Integer> referenceMacroPercents = new HashMap<>();
        referenceMacroPercents.put("Protein", DEFAULT_PROTEIN_PERCENT);
        referenceMacroPercents.put("Carbohydrates", DEFAULT_CARBS_PERCENT);
        referenceMacroPercents.put("Fat", DEFAULT_FAT_PERCENT);

        // Add data to the model
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("todaysMeals", todaysMeals);
        model.addAttribute("todaysTotalCalories", todaysTotalCalories);
        model.addAttribute("referenceMacroPercents", referenceMacroPercents);

        return "diet"; // Name of the Thymeleaf template
    }
}