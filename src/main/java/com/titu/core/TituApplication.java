package com.titu.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TituApplication {

    public static void main(String[] args) {
        SpringApplication.run(TituApplication.class, args);
    }

}
