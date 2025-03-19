package com.app.fitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FitrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitrackApplication.class, args);
	}

}
