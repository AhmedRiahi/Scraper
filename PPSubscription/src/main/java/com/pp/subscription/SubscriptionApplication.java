package com.pp.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.pp")
@SpringBootApplication
@EnableAutoConfiguration(exclude={MongoDataAutoConfiguration.class,MongoAutoConfiguration.class})
public class SubscriptionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptionApplication.class, args);
	}
}
