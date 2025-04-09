package com.app.fitrack.controller;

import com.app.fitrack.model.Goal;
import com.app.fitrack.model.User;
import com.app.fitrack.service.GoalService;
import com.app.fitrack.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/user/goals")
public class GoalController {

    private static final Logger logger = LoggerFactory.getLogger(GoalController.class);

    @Autowired
    private GoalService goalService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showGoals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        List<Goal> goals = goalService.getUserGoals(user);
        
        model.addAttribute("goals", goals);
        model.addAttribute("goalService", goalService);
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        return "goals";
    }

    @GetMapping("/add")
    public String showAddGoalForm(Model model) {
        model.addAttribute("currentDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        return "add-goal";
    }

    @PostMapping("/add")
    public String addGoal(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam String goalType,
                         @RequestParam String description,
                         @RequestParam Double targetValue,
                         @RequestParam String targetDate,
                         RedirectAttributes redi) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email);
        
        LocalDateTime targetDateTime = LocalDateTime.parse(targetDate + "T00:00:00");
        
        Goal goal = goalService.createGoal(user, goalType, description, targetValue, targetDateTime);
        logger.info("Created goal: type={}, currentValue={}, targetValue={}", 
                   goal.getGoalType(), goal.getCurrentValue(), goal.getTargetValue());
        
        redi.addFlashAttribute("successMessage", "Goal created successfully!");
        return "redirect:/user/dashboard";
    }

    @PostMapping("/update/{goalId}")
    public String updateGoalProgress(@PathVariable Long goalId,
                                   @RequestParam Double currentValue) {
        goalService.updateGoalProgress(goalId, currentValue);
        return "redirect:/user/goals";
    }

    @PostMapping("/delete/{goalId}")
    public String deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return "redirect:/user/goals";
    }
} 