package com.vivso.Vivso.Security;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

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
                // 1. HABILITAMOS CORS: Le decimos a Spring que acepte peticiones externas
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Deshabilitamos CSRF porque usaremos Tokens
                .csrf(csrf -> csrf.disable())

                // 3. Reglas de las puertas (Rutas)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Login y Registro sin seguridad

                        // ATENCIÓN: Si Javi ya manda el token JWT, cambiá 'permitAll()' por 'authenticated()'
                        .anyRequest().permitAll()
                )

                // 4. Agregamos el filtro JWT antes del filtro de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Le damos permiso explícito al puerto de React (Vite) y a tu túnel de Ngrok
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "https://blade-pretty-obstruct.ngrok-free.dev"
        ));

        // 2. Permitimos todos los métodos, fundamental el "OPTIONS" para el preflight de CORS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 3. Permitimos los headers que Javi necesita mandar (como el Token y el FormData)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));

        // 4. Importante si el frontend maneja credenciales o tokens de sesión
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicamos estas reglas a TODAS las rutas de tu API (/**)
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
