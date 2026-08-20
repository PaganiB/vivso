package com.vivso.Vivso.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ── PÚBLICO ──────────────────────────────────────────────────────────
                        //En realidad solo el login tiene acceso público, el registrar usuario no.
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/solicitud/organizacion").permitAll()

                        // ── INTEGRANTE (solo accede a sus cosas) ─────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/solicitud/familia").hasRole("INTEGRANTE")
                        .requestMatchers(HttpMethod.GET, "/solicitud/organizacion/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")
                        .requestMatchers(HttpMethod.GET, "/documento/organizacion/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")
                        .requestMatchers(HttpMethod.PATCH, "/documento/*/reemplazar").hasAnyRole("ADMIN", "INTEGRANTE")
                        .requestMatchers(HttpMethod.POST, "/documento/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")

                        // ── OPERADOR (bandeja de entrada, aprueba/rechaza) ────────────────────
                        .requestMatchers(HttpMethod.PUT, "/solicitud/*/aprobar").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/solicitud/*/rechazar").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PATCH, "/documento/*/revisar").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers("/usuario/**").hasAnyRole("ADMIN", "OPERADOR")

                        // ── TECNICO (visitas) ─────────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/visita/**", "/visita-obra/**").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.PUT, "/visita/**", "/visita-obra/**").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.GET, "/visita/**", "/visita-obra/**").hasAnyRole("ADMIN", "TECNICO", "ARQUITECTO")

                        // ── ARQUITECTO (revisión y decisiones) ───────────────────────────────
                        .requestMatchers(HttpMethod.PUT, "/vivienda/**").hasAnyRole("ADMIN", "ARQUITECTO")
                        .requestMatchers(HttpMethod.GET, "/vivienda/**").hasAnyRole("ADMIN", "ARQUITECTO", "TECNICO", "OPERADOR")

                        // ── LECTURA GENERAL (OPERADOR y ARQUITECTO ven expedientes completos) ─
                        .requestMatchers(HttpMethod.GET, "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO")
                        .requestMatchers(HttpMethod.GET, "/familia/**", "/familiar/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO", "TECNICO")
                        .requestMatchers(HttpMethod.GET, "/organizacion/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO")
                        .requestMatchers(HttpMethod.GET, "/documento/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO")

                        // ── ESCRITURA GENERAL (solo OPERADOR registra datos) ─────────────────
                        .requestMatchers(HttpMethod.POST, "/organizacion/**", "/familia/**",
                                "/familiar/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PUT, "/organizacion/**", "/familia/**",
                                "/familiar/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR")

                        // ── BORRADO (solo ADMIN) ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                        // ── CUALQUIER OTRA COSA ───────────────────────────────────────────────
                        .anyRequest().hasRole("ADMIN")
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://fetch-reproduce-eatery.ngrok-free.dev"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}