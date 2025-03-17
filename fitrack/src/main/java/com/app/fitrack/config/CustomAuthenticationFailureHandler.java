package com.app.fitrack.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) 
            throws IOException, ServletException {
        

        if (exception instanceof DisabledException) {
            String email = request.getParameter("username");
            response.sendRedirect("/user/resend-code?email=" + email);
            return;
        } 
        
        if (exception instanceof BadCredentialsException) {

            super.setDefaultFailureUrl("/user/login?error=true");
            super.onAuthenticationFailure(request, response, exception);
            return;
        } 
        
        super.setDefaultFailureUrl("/user/login?error=unknown");
        super.onAuthenticationFailure(request, response, exception);
    }
}
