package com.vivso.Vivso.Security;

import com.vivso.Vivso.Service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    // 1. El Encriptador: Le decimos a Spring que use el algoritmo BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. El Proveedor: Conecta tu UserDetailsService con el Encriptador
    @Bean
    public AuthenticationProvider authenticationProvider() {

        // ¡El cambio clave! Ahora el servicio nace inyectado desde el constructor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        // El PasswordEncoder se sigue seteando con normalidad
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // 3. El Manager: Es el objeto que vamos a llamar desde el Controller para iniciar sesión
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 4. Los Permisos: Por ahora, dejamos que las rutas "/auth/**" sean públicas
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF porque usaremos Tokens
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Login y Registro sin seguridad
                        .anyRequest().permitAll() // TODO: Temporalmente permitimos todo para no romper tus otros endpoints
                )
                .build();
    }
}
