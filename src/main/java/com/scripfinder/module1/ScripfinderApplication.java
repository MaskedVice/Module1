package com.scripfinder.module1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScripfinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScripfinderApplication.class, args);
	}

}
