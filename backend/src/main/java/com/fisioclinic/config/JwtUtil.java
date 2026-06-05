package com.fisioclinic.config;

import com.fisioclinic.model.Fisioterapeuta;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:28800000}")
    private long expiration;

    // ── Geração ──────────────────────────────────────────────────────────────

    public String gerarToken(Fisioterapeuta fisio) {
        return Jwts.builder()
            .subject(fisio.getEmail())
            .claim("perfil", fisio.getPerfil().name())
            .claim("nome",   fisio.getNome())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey())
            .compact();
    }

    // ── Extração ─────────────────────────────────────────────────────────────

    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return getClaims(token).get("perfil", String.class);
    }

    // ── Validação ────────────────────────────────────────────────────────────

    public boolean isValido(String token) {
        try {
            Date exp = getClaims(token).getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
