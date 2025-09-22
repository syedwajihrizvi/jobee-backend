package com.rizvi.jobee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JobeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobeeApplication.class, args);
	}

}
