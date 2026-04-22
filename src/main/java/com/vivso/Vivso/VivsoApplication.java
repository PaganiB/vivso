package com.vivso.Vivso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VivsoApplication {

    public static void main(String[] args) {
        SpringApplication.run(VivsoApplication.class, args);
    }

}
