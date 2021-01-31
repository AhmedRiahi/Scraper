package com.pp.mozart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.pp")
@EnableCaching(proxyTargetClass = true)
public class MozartApplication {

	public static void main(String[] args) {
		SpringApplication.run(MozartApplication.class, args);
	}
}
