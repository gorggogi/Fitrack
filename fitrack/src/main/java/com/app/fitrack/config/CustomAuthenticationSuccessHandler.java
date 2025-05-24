package com.app.fitrack.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.app.fitrack.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        com.app.fitrack.model.User user = userService.findByEmail(email);
        
        String contextPath = request.getContextPath();
        // Check if profile is incomplete (any of the required fields is null)
        if (user.getAge() == null || user.getGender() == null || 
            user.getHeight() == null || user.getWeight() == null) {
            response.sendRedirect(contextPath + "/user/profile");
        } else {
            response.sendRedirect(contextPath + "/user/dashboard");
        }
    }
} 