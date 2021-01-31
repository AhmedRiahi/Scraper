package com.pp.cleaner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.pp")
@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class CleanerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CleanerApplication.class, args);
	}
}
