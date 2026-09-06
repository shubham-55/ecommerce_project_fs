package com.ecommerce.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SbEcomApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(SbEcomApplication.class, args);
		} catch (Exception e) {

			System.out.println("Error starting application: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
