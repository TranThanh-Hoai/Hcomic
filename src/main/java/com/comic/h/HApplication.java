package com.comic.h;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HApplication {

	public static void main(String[] args) {
		SpringApplication.run(HApplication.class, args);
	}

}

