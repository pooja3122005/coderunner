package com.example.coderunner.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // ✅ KEEP THIS SAME ALWAYS (do NOT change after login)
    private static final String SECRET = "mySuperSecretKeyForJwtAuthentication1234567890";

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ GENERATE TOKEN
    public static String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ EXTRACT USER ID (FIXED)
    public static Long extractUserId(String token) {

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }
}