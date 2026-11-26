package com.coworkhub.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Crea y valida los token JWT firmados con HMAC.
 * El token guarda el nombre de usuario y el rol; el resto se consulta en la base.
 */
@Service
public class JwtServicio {

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtServicio(@Value("${coworkhub.jwt.secreto}") String secreto,
                       @Value("${coworkhub.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public String extraerNombreUsuario(String token) {
        return leerClaims(token).getSubject();
    }

    public boolean esValido(String token) {
        try {
            leerClaims(token);
            return true;
        } catch (RuntimeException excepcion) {
            return false;
        }
    }

    public long getExpiracionMinutos() {
        return expiracionMinutos;
    }

    private Claims leerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
