package com.pp.scrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.pp")
public class ScraperApplication {

	public static void main(String[] args){
		SpringApplication.run(ScraperApplication.class, args);
	}
}
