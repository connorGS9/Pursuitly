package com.pursuitly.pursuitly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PursuitlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PursuitlyApplication.class, args);
	}

}
