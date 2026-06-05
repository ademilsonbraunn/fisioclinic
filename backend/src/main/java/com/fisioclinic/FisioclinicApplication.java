package com.fisioclinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FisioclinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(FisioclinicApplication.class, args);
    }
}
