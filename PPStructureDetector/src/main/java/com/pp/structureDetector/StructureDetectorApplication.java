package com.pp.structureDetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.pp")
@EnableAutoConfiguration(exclude={MongoDataAutoConfiguration.class,MongoAutoConfiguration.class})
public class StructureDetectorApplication {
    public static void main(String[] args){
        SpringApplication.run(StructureDetectorApplication.class, args);
    }
}
