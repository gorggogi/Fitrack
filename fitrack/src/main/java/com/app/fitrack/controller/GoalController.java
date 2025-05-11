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
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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

    @GetMapping("")
    public String showActiveGoalsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Goal> goals = goalService.getActiveUnarchivedGoals(user);
        model.addAttribute("goals", goals);
        model.addAttribute("goalService", goalService);
        return "goals";
    }

    @GetMapping("/all")
    public String showAllGoalsPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Goal> allGoals = goalService.getAllGoalsForUser(user);
        long totalCompletedCount = goalService.getTotalCompletedGoalCount(user);
        
        model.addAttribute("allGoals", allGoals);
        model.addAttribute("totalCompletedGoalCount", totalCompletedCount);
        model.addAttribute("goalService", goalService);
        model.addAttribute("fullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("user", user);
        
        return "all-goals";
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
                                   @RequestParam Double currentValue,
                                   RedirectAttributes redirectAttributes) {
        Goal updatedGoal = goalService.updateGoalProgress(goalId, currentValue);
        if (updatedGoal != null && "COMPLETED".equalsIgnoreCase(updatedGoal.getStatus())) {
            redirectAttributes.addFlashAttribute("completedGoalName", updatedGoal.getDescription());
        }
        return "redirect:/user/goals";
    }

    @PostMapping("/delete/{goalId}")
    public String deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return "redirect:/user/goals";
    }

    @PostMapping("/archive/{goalId}")
    @ResponseBody
    public ResponseEntity<?> archiveGoal(@PathVariable Long goalId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            logger.warn("Archive attempt failed: User not found for principal {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        try {
            goalService.archiveGoal(goalId, currentUser);
            return ResponseEntity.ok().body("Goal archived successfully.");
        } catch (SecurityException e) {
            logger.error("Security error archiving goal {} for user {}: {}", goalId, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error archiving goal {} for user {}: {}", goalId, currentUser.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error archiving goal {} for user {}: {}", goalId, currentUser.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error archiving goal.");
        }
    }
} 