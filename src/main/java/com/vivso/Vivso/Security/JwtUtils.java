package com.vivso.Vivso.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    // 1. La Clave Secreta: Generamos una llave matemática súper segura de 256 bits.
    // (Solo el backend sabe leer esta llave, por lo que nadie puede falsificar tus tokens).
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 2. Tiempo de vida: Le damos 8 horas de validez al token antes de que caduque.
    private static final long TIEMPO_EXPIRACION = 1000 * 60 * 60 * 8;

    // =======================================================================
    // MÉTODOS PRINCIPALES
    // =======================================================================

    // A. CREAR EL TOKEN (Se usa en el Login)
    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de hoy
                .setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION)) // Fecha de vencimiento
                .signWith(SECRET_KEY) // Lo firmamos con nuestra llave
                .compact();
    }

    // B. LEER EL TOKEN (Se usa cuando React nos manda peticiones)
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    // C. VALIDAR EL TOKEN (Chequea que sea del usuario correcto y no esté vencido)
    public boolean validarToken(String token, String usernameBD) {
        final String usernameToken = extraerUsername(token);
        return (usernameToken.equals(usernameBD) && !isTokenExpirado(token));
    }

    // =======================================================================
    // MÉTODOS AUXILIARES INTERNOS (Magia de la librería JJWT)
    // =======================================================================
    private boolean isTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}