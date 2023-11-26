package com.example.CokeRestAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CokeRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CokeRestApiApplication.class, args);
	}

}
