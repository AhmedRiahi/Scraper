package com.pp.engine;

import com.pp.engine.config.SchedulerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Import({ SchedulerConfig.class })
@ComponentScan("com.pp")
@SpringBootApplication
@EnableAutoConfiguration(exclude={MongoDataAutoConfiguration.class,MongoAutoConfiguration.class})
public class EngineApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(EngineApplication.class, args);
	}
}
