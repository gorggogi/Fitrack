package com.app.fitrack.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.DuplicateEmailException;
import jakarta.servlet.http.HttpSession;
@Controller
public class UserController {
    
    @Autowired
    private UserService userService; 

    @GetMapping("/user/success")
    public String showSuccessPage() {
        return "Success";
    }

    @GetMapping("/user/verify")
    public String showVerificationPage() {
    return "Verify";
    }

    @GetMapping("/user/resend-code")
    public String resendVerificationPage() {
    return "Resend-code";
    }
    
    @GetMapping("/user/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());  
        return "Login";  
    }

    @PostMapping("/user/resend-verification")
public String resendVerification(@RequestParam String email, RedirectAttributes redi) {
    String message = userService.resendVerificationCode(email);
    redi.addFlashAttribute("message", message);
    return "redirect:/user/resend-code";
}


    @PostMapping("/user/login")
public String loginUser(@RequestParam String email, @RequestParam String password, RedirectAttributes redi, HttpSession session) {
    LoginRequest loginRequest = new LoginRequest(email, password);
    LoginResponse response = userService.login(loginRequest);

    if (response.isSuccess()) {
        User user = userService.findByEmail(email);

        if (!user.isVerified()) {  
            redi.addFlashAttribute("error", "Your account is not verified. Please verify your email.");
            redi.addFlashAttribute("email", email);  
            return "redirect:/user/resend-code";  
        }

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
    
    @GetMapping("/user/new")
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
        }

        userService.registerUser(user); 
        redi.addFlashAttribute("message", "Verify your account with the code we sent you to continue to Fitrack.");
        return "redirect:/user/verify";
    } catch (DuplicateEmailException e) {
        redi.addFlashAttribute("error", e.getMessage());
        redi.addFlashAttribute("user", user);
        return "redirect:/user/new";
    }
}

@PostMapping("/user/verify-code")
public String verifyUser(@RequestParam String code, RedirectAttributes redi) {
    String result = userService.verifyUser(code); 

    if (result.equals("Email successfully verified!")) { 
        redi.addFlashAttribute("message", result);
        return "redirect:/user/login";
    }

    redi.addFlashAttribute("error", result); 
    return "redirect:/user/verify";
}

    @GetMapping("/user/reset-password")
    public String showResetPasswordPage() {
        return "Reset-password";  
    }

    @PostMapping("/user/reset-password")
    public String processResetPassword(@RequestParam String email, RedirectAttributes redi) {
        String message = userService.generatePasswordResetToken(email);
        redi.addFlashAttribute("message", message);
        return "redirect:/user/reset-password";
    }

    @GetMapping("/user/change-password")
    public String showChangePasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "Change-password";  
    }

    @PostMapping("/user/change-password")
    public String processChangePassword(@RequestParam String token, 
                                        @RequestParam String newPassword, 
                                        @RequestParam String confirmPassword, 
                                        RedirectAttributes redi) {
        if (!newPassword.equals(confirmPassword)) {
            redi.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/user/change-password?token=" + token;
        }

        String message = userService.resetPassword(token, newPassword);
        redi.addFlashAttribute("message", message);
        return "redirect:/user/login";
    }
}

    

