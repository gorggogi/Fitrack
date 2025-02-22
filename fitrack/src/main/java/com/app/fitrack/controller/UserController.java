package com.app.fitrack.controller;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.app.fitrack.dto.LoginRequest;
import com.app.fitrack.dto.LoginResponse;
import com.app.fitrack.model.Meal;
import com.app.fitrack.model.User;
import com.app.fitrack.service.UserService;
import com.app.fitrack.service.DuplicateEmailException;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    
    @Autowired
    private UserService service;
    
    @GetMapping("/user/addmeal")
    public String addMeal(Model model) {
        model.addAttribute("meal", new Meal()); 
        return "addmeal";
    }
    
    @PostMapping("/meals/add")
    public String addMeal(Meal meal) {
        service.saveMeal(meal);
        return "redirect:/user/dashboard"; 
    }

    @GetMapping("/user/dashboard")
    public String dashboard(Model model) {
        List<Meal> meals = service.getMealsForCurrentDate();
        model.addAttribute("meals", meals);
        return "dashboard"; 
    }

    @GetMapping ("/user/success")
    public String showSuccessPage () {
        return "Success";
    }
    
    @GetMapping("/user/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());  
        return "Login";  
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

    @PostMapping("/user/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, RedirectAttributes redi, HttpSession session) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        LoginResponse response = service.login(loginRequest);
    
        if (response.isSuccess()) {
            User user = service.findByEmail(email);
            session.setAttribute("firstName", user.getFirstName());
            session.setAttribute("lastName", user.getLastName());
            return "redirect:/user/dashboard";  
        } else {
            redi.addFlashAttribute("error", response.getMessage());
            return "redirect:/user/login";  
        }
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
    public String saveUserForm(@ModelAttribute("user") User user, RedirectAttributes redi, Model model) {
        try {
            service.save(user);
            redi.addFlashAttribute("message", "You have successfully registered to Fitrack! Login to your account now.");
            return "redirect:/user/success";
        } catch (DuplicateEmailException e) {
            redi.addFlashAttribute("error", e.getMessage());
            model.addAttribute("user", user); 
            return "registration-form"; 
        }
    }
}
