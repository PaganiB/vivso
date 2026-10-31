package com.vivso.Vivso.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

  @Value("${corsorigins}")
  private List<String> allowedOrigins;

  @Autowired
  private UserDetailsService userDetailsService;
  @Autowired
  private JwtAuthenticationFilter jwtAuthFilter;

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
      .authenticationManager(new ProviderManager(authenticationProvider()))
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(csrf -> csrf.disable())
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .exceptionHandling(e -> e.authenticationEntryPoint(
        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
      .authorizeHttpRequests(auth -> auth

        // ── 1. AUTENTICACIÓN & GESTIÓN DE USUARIOS ───────────────────────────
        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/auth/registro").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PATCH, "/auth/cambiar-password").authenticated()
        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
        .requestMatchers(HttpMethod.POST, "/usuario/push-token").authenticated()
        .requestMatchers("/usuario/**").hasRole("ADMIN")

        // ── 2. SOLICITUD DE ORGANIZACIÓN (Registro e Ingreso) ────────────────
        .requestMatchers(HttpMethod.POST, "/solicitud-organizacion").permitAll()
        .requestMatchers(HttpMethod.GET, "/solicitud-organizacion/token/**").permitAll()
        .requestMatchers(HttpMethod.PATCH, "/solicitud-organizacion/editar-token/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/solicitud-organizacion/pendientes", "/solicitud-organizacion").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.POST, "/solicitud-organizacion/**").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.PUT, "/solicitud-organizacion/**").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.GET, "/solicitud/tecnicos-disponibles").hasAnyRole("ADMIN", "ARQUITECTO")
        .requestMatchers(HttpMethod.PUT, "/solicitud/*/asignar-tecnico/*").hasAnyRole("ADMIN", "ARQUITECTO")

        // ── 3. SOLICITUDES (Expedientes y Obras) ─────────────────────────────
        .requestMatchers(HttpMethod.POST, "/solicitud/familia").hasAnyRole("ADMIN", "INTEGRANTE")
        .requestMatchers(HttpMethod.GET, "/solicitud/mis-obras").hasAnyRole("ADMIN", "INTEGRANTE")
        .requestMatchers(HttpMethod.GET, "/solicitud/organizacion/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")
        .requestMatchers(HttpMethod.PUT, "/solicitud/*/confirmar-correccion").hasAnyRole("ADMIN", "INTEGRANTE")
        .requestMatchers(HttpMethod.PUT, "/solicitud/*/aprobar").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.PUT, "/solicitud/*/rechazar").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.PUT,  "/solicitud/*/semiAprobar").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.PATCH, "/solicitud/*/expediente").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.POST, "/solicitud", "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.GET, "/solicitud/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO", "TECNICO")

        // ── 4. ETAPAS DE CONSTRUCCIÓN ────────────────────────────────────────
        .requestMatchers(HttpMethod.POST, "/etapa/solicitud/*/iniciar").hasRole("TECNICO")
        .requestMatchers(HttpMethod.GET, "/etapa/solicitud/**").hasAnyRole("ADMIN", "OPERADOR", "TECNICO", "INTEGRANTE")
        .requestMatchers(HttpMethod.POST, "/etapa/*/enviar", "/etapa/*/fotos").hasRole("INTEGRANTE")
        .requestMatchers(HttpMethod.GET, "/etapa/*/fotos").hasAnyRole("ADMIN", "INTEGRANTE", "TECNICO", "ARQUITECTO")
        .requestMatchers(HttpMethod.POST, "/etapa/*/aprobar", "/etapa/*/observar", "/etapa/*/prorroga").hasRole("TECNICO")

        // ── 5. GESTIÓN DE DOCUMENTOS ─────────────────────────────────────────
        .requestMatchers(HttpMethod.GET, "/documento/organizacion/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")
        .requestMatchers(HttpMethod.PATCH, "/documento/*/reemplazar").hasAnyRole("ADMIN", "INTEGRANTE")
        .requestMatchers(HttpMethod.PATCH, "/documento/*/marcar-corregir").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.POST, "/documento/**").hasAnyRole("ADMIN", "OPERADOR", "INTEGRANTE")
        .requestMatchers(HttpMethod.GET, "/documento/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO")

        // ── 6. VISITAS EN TERRENO Y ARCHIVOS MULTIMEDIA ──────────────────────
        .requestMatchers(HttpMethod.POST, "/visita-inicial/**", "/visita-obra/**").hasAnyRole("ADMIN", "TECNICO")
        .requestMatchers(HttpMethod.PUT, "/visita-inicial/**", "/visita-obra/**").hasAnyRole("ADMIN", "TECNICO")
        .requestMatchers(HttpMethod.GET, "/visita-inicial/**", "/visita-obra/**", "/visita-inicial/historial-visitas").hasAnyRole("ADMIN", "TECNICO", "ARQUITECTO")

        // ── 7. VIVIENDAS ─────────────────────────────────────────────────────
        .requestMatchers(HttpMethod.PUT, "/vivienda/**").hasAnyRole("ADMIN", "ARQUITECTO")
        .requestMatchers(HttpMethod.GET, "/vivienda/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO", "TECNICO")

        // ── 8. ENTIDADES BASE (Organizaciones, Familias, Integrantes) ────────
        .requestMatchers(HttpMethod.POST, "/organizacion/**", "/familia/**", "/familiar/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.PUT, "/organizacion/**", "/familia/**", "/familiar/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR")
        .requestMatchers(HttpMethod.GET, "/familia/**", "/familiar/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO", "TECNICO")
        .requestMatchers(HttpMethod.GET, "/organizacion/**", "/integrante/**").hasAnyRole("ADMIN", "OPERADOR", "ARQUITECTO", "TECNICO", "INTEGRANTE")

        // ── 9. REGLAS GLOBALES Y CIERRE ──────────────────────────────────────
        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
        .requestMatchers("/notificaciones/**").authenticated()
        .anyRequest().hasRole("ADMIN")
      )

      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
      .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}