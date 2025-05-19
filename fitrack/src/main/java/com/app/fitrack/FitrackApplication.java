package com.app.fitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import com.app.fitrack.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class FitrackApplication extends SpringBootServletInitializer {

	@Autowired
	private CloudinaryService cloudinaryService;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(FitrackApplication.class);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
		System.out.println("Default JVM TimeZone ID: " + TimeZone.getDefault().getID());
		System.out.println("Default JVM TimeZone Display Name: " + TimeZone.getDefault().getDisplayName());
		System.out.println("Current LocalDate.now(): " + LocalDate.now());
		System.out.println("Current LocalDateTime.now(): " + LocalDateTime.now());

		try {
			System.out.println("Attempting to upload application logo...");
			String logoUrl = cloudinaryService.uploadApplicationLogo();
			System.out.println("Application logo uploaded successfully. URL: " + logoUrl);
		} catch (IOException e) {
			System.err.println("Failed to upload application logo: " + e.getMessage());
			// Decide if you want to throw a runtime exception or just log the error
			// For now, just printing to error stream
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(FitrackApplication.class, args);
	}

}
