package com.app.fitrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.DuplicateEmailException;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    
    @Autowired
    private UserService userService; 

    @GetMapping("/user/success")
    public String showSuccessPage () {
        return "Success";
    }
    
    @GetMapping("/user/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());  
        return "Login";  
    }

    @PostMapping("/user/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, RedirectAttributes redi, HttpSession session) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        LoginResponse response = userService.login(loginRequest);
    
        if (response.isSuccess()) {
            User user = userService.findByEmail(email);
            session.setAttribute("firstName", user.getFirstName());
            session.setAttribute("lastName", user.getLastName());
            return "redirect:/user/dashboard";  
        } else {
            redi.addFlashAttribute("error", response.getMessage());
            return "redirect:/user/login";  
        }
    }

    @PostMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/user/login"; 
    }
    
    @GetMapping ("/user/new")
    public String showUserPage(Model model) {
        model.addAttribute("user", new User());
        return "registration-form";
    }

    @ModelAttribute("fullName")
    public String getUserFullName(HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return "";
    }

    @PostMapping("/user/save")
    public String saveUserForm(@ModelAttribute("user") User user, @RequestParam String confirmPassword, RedirectAttributes redi) {
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                redi.addFlashAttribute("error", "Passwords do not match.");
                redi.addFlashAttribute("user", user);
                return "redirect:/user/new";
            } else {
                userService.save(user);
                redi.addFlashAttribute("message", "You have successfully registered to Fitrack! Login to your account now.");
            }
            return "redirect:/user/success";
        } catch (DuplicateEmailException e) {
            redi.addFlashAttribute("error", e.getMessage());
            redi.addFlashAttribute("user", user);
            return "redirect:/user/new";
        }
    }
}
