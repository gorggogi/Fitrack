package com.app.fitrack.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, 
                                                   @Qualifier("customAuthenticationFailureHandler") 
                                                   AuthenticationFailureHandler failureHandler) throws Exception {
        return http
            .csrf(csrf -> csrf
            .ignoringRequestMatchers("/user/verify-code", "/meals/estimate-calories") 
            )
            .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/styles/**",
                "/images/**",
                "/js/**",
                "/webjars/**"
            ).permitAll()
            .requestMatchers(
                "/user/login",
                "/user/new",
                "/user/save",
                "/user/verify",
                "/user/verify-code",
                "/user/resend-code",
                "/user/resend-verification",
                "/user/reset-password**",
                "/user/change-password**",
                "/meals/estimate-calories",
                "/error**"
            ).permitAll()
            .requestMatchers("/meals").hasAnyRole("USER", "ADMIN") 
            .anyRequest().authenticated()
        )
            .formLogin(form -> form
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .defaultSuccessUrl("/user/dashboard", true)
                .failureHandler(failureHandler)  
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login?logout=true")
            )
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
