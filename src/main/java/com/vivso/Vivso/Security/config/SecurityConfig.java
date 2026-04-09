package com.vivso.Vivso.Security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Crucial: sin esto, los PATCH/POST/PUT fallan siempre
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Abrimos la puerta para probar tranquilos
                );

        return http.build();
    }
}