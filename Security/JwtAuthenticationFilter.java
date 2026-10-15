package com.vivso.Vivso.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

        // 1. Buscamos el token en la mochila de la petición (el header 'Authorization')
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        final String username;

        // 2. Extraemos el JWT ya sea del Header o del parámetro de la URL
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Le sacamos la palabra "Bearer " (que tiene 7 caracteres) para quedarnos con el token puro
            jwt = authHeader.substring(7);
        } else if (request.getParameter("token") != null) {
            // Si viene desde el componente Image de React Native en la URL (?token=...)
            jwt = request.getParameter("token");
        }

        // 3. Si no encontramos el token por ningún lado, lo dejamos pasar sin autenticar.
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Extraemos el nombre de usuario de adentro del token
        username = jwtUtils.extraerUsername(jwt);

        // 5. Si hay un usuario, y todavía no está logueado en este flujo actual:
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Buscamos los datos reales del usuario en la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Validamos matemáticamente que el token no esté vencido ni adulterado
            if (jwtUtils.validarToken(jwt, userDetails.getUsername())) {

                // 7. ¡Token válido! Le creamos una credencial oficial de Spring y lo dejamos pasar
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Guardamos la autorización en el contexto
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Pase el que sigue
        filterChain.doFilter(request, response);
    }
}
