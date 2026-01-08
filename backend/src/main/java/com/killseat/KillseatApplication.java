package com.killseat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KillseatApplication {

	public static void main(String[] args) {
		SpringApplication.run(KillseatApplication.class, args);
	}

}
