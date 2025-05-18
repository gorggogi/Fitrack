package com.app.fitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class FitrackApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
		System.out.println("Default JVM TimeZone ID: " + TimeZone.getDefault().getID());
		System.out.println("Default JVM TimeZone Display Name: " + TimeZone.getDefault().getDisplayName());
		System.out.println("Current LocalDate.now(): " + LocalDate.now());
		System.out.println("Current LocalDateTime.now(): " + LocalDateTime.now());
	}

	public static void main(String[] args) {
		SpringApplication.run(FitrackApplication.class, args);
	}

}
