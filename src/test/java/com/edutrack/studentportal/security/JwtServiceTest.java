package com.edutrack.studentportal.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-jwt-signing-secret-that-is-long-enough-for-hs256";

    @Test
    void generatesHs256TokenWithConfiguredExpirationAndRole() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtService.generateToken("student1", "STUDENT");

        assertTrue(jwtService.validateToken(token));
        assertEquals("student1", jwtService.extractUsername(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
        Date expiration = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        assertTrue(expiration.getTime() >= beforeGeneration + 59_000);
        assertTrue(expiration.getTime() <= System.currentTimeMillis() + 60_000);
    }

    @Test
    void rejectsExpiredTokens() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String expiredToken = Jwts.builder()
                .subject("student1")
                .claim("role", "STUDENT")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.validateToken(expiredToken));
    }

    @Test
    void rejectsShortSigningSecretsAndNonPositiveExpiration() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 60_000));
        assertThrows(IllegalStateException.class, () -> new JwtService(SECRET, 0));
    }
}
