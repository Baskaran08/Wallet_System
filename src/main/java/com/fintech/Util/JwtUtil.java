package com.fintech.Util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "UrgH85ZcmBKKbuC/57/6hO1gguxdAszjBopwbYdqESs=";
    private static final long EXPIRATION = 1000 * 60 * 60;

    public static SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public static String generateToken(Long userId, String email) {

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSecretKey())
                .compact();
    }

    public static Claims validateToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}