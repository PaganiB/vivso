package com.vivso.Vivso.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    // 1. La Clave Secreta: Generamos una llave matemática súper segura de 256 bits.
    // (Solo el backend sabe leer esta llave, por lo que nadie puede falsificar tus tokens).
    @Value("${jwt.secret}")
    private String secretString;


    // 2. Tiempo de vida: Le damos 8 horas de validez al token antes de que caduque.
    @Value("${jwt.expiration}")
    private long tiempoExpiracion;

    // =======================================================================
    // MÉTODOS PRINCIPALES
    // =======================================================================

    // A. CREAR EL TOKEN (Se usa en el Login)
    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de hoy
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracion)) // Fecha de vencimiento
                .signWith(getSecretKey()) // Lo firmamos con nuestra llave
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
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }
}