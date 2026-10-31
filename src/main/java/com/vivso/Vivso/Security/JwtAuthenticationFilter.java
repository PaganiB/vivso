package com.vivso.Vivso.Security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

    // 0. IGNORAR PETICIONES DE CORS (PREFLIGHT OPTIONS) Y EL LOGIN
    if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || request.getServletPath().contains("/auth/login")) {
      filterChain.doFilter(request, response);
      return;
    }

    // 1. El token solo se acepta por el header Authorization: Bearer <token>
    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);   // sin token: sigue como anónimo
      return;
    }

    final String jwt = authHeader.substring(7);

    try {
      // 2. Usuario que dice el token (lanza excepción si está vencido o adulterado)
      final String username = jwtUtils.extraerUsername(jwt);

      // 3. Si hay usuario y todavía no hay autenticación en este pedido
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        // 4. Token coherente con el usuario real y cuenta activa
        if (jwtUtils.validarToken(jwt, userDetails.getUsername()) && userDetails.isEnabled()) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
      // Token vencido, mal formado, con firma incorrecta o de un usuario inexistente: no se autentica. El 401 lo devuelve el entry point de SecurityConfig.
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }
}
