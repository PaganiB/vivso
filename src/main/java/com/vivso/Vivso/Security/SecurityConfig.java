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
                        // Login y registro de usuarios internos
                        .requestMatchers("/auth/**").permitAll()
                        // Formulario de la landing: una ONG sin cuenta puede presentar su solicitud
                        .requestMatchers(HttpMethod.POST, "/solicitud/organizacion").permitAll()

                        // ── USUARIO (solo ADMIN gestiona cuentas) ────────────────────────────
                        .requestMatchers("/usuario/**").hasRole("ADMIN")

                        // ── VISITA INICIAL (TECNICO realiza el relevamiento social) ──────────
                        .requestMatchers(HttpMethod.POST, "/visita/**").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.PUT,  "/visita/**").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.DELETE, "/visita/**").hasRole("ADMIN")

                        // ── VISITA OBRA (ARQUITECTO registra avance de construcción) ─────────
                        .requestMatchers(HttpMethod.POST, "/visita-obra/**").hasAnyRole("ADMIN", "ARQUITECTO")
                        .requestMatchers(HttpMethod.PUT,  "/visita-obra/**").hasAnyRole("ADMIN", "ARQUITECTO")
                        .requestMatchers(HttpMethod.DELETE, "/visita-obra/**").hasRole("ADMIN")

                        // ── VIVIENDA (ARQUITECTO aprueba y actualiza el AFO) ─────────────────
                        .requestMatchers(HttpMethod.POST,   "/vivienda/**").hasAnyRole("ADMIN", "ARQUITECTO")
                        .requestMatchers(HttpMethod.PUT,    "/vivienda/**").hasAnyRole("ADMIN", "ARQUITECTO")
                        .requestMatchers(HttpMethod.DELETE, "/vivienda/**").hasRole("ADMIN")

                        // ── SOLICITUD (OPERADOR carga, ARQUITECTO puede actualizar estado) ───
                        .requestMatchers(HttpMethod.POST,   "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PUT,    "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO")
                        .requestMatchers(HttpMethod.DELETE, "/solicitud/**").hasRole("ADMIN")

                        // ── ONG / FAMILIA / INTEGRANTE (OPERADOR registra) ───────────────────
                        .requestMatchers(HttpMethod.POST,   "/organizacion/**", "/familia/**",
                                "/familiar/**",     "/integrante/**")
                        .hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.PUT,    "/organizacion/**", "/familia/**",
                                "/familiar/**",     "/integrante/**")
                        .hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/organizacion/**", "/familia/**",
                                "/familiar/**",     "/integrante/**")
                        .hasRole("ADMIN")

                        // ── DOCUMENTOS (OPERADOR y TECNICO pueden subir) ─────────────────────
                        .requestMatchers(HttpMethod.POST,   "/documento/**").hasAnyRole("ADMIN", "OPERADOR", "TECNICO")
                        .requestMatchers(HttpMethod.PUT,    "/documento/**").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.DELETE, "/documento/**").hasRole("ADMIN")

                        // ── LECTURA (todos los roles autenticados pueden consultar) ──────────
                        // CONSULTOR solo llega hasta acá — todos sus intentos de escribir
                        // fueron rechazados arriba, solo le quedan los GET
                        .requestMatchers(HttpMethod.GET, "/**").authenticated()

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