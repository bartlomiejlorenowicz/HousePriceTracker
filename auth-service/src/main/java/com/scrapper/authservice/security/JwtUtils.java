package com.scrapper.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key key;

    private final Clock clock;

    private JwtParser parser;

    public JwtUtils(Clock clock) {
        this.clock = clock;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing JwtUtils with secret length={} and expirationMs={}",
                secretKey.length(), expirationTime);
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        Instant now = clock.instant();
        Date iat = Date.from(now);
        Date expiry = Date.from(now.plusMillis(expirationTime));

        return Jwts.builder()
                .setSubject(email) // todo change to user id or uuid
                .setIssuedAt(iat)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        log.debug("Extracting username from token");
        try {
            String username = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.trace("Extracted username='{}'", username);
            return username;
        } catch (JwtException e) {
            log.warn("Failed to extract username from token: {}", e.getMessage());
            throw e;
        }
    }

//    public boolean validateToken(String token, String username) {
//        log.debug("Validating token for user='{}'", username);
//        try {
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            boolean subjectOk = username.equals(claims.getSubject());
//            boolean notExpired = claims.getExpiration().after(Date.from(clock.instant()));
//
//            return username.equals(claims.getSubject())
//                    && !claims.getExpiration().before(new Date());
//        } catch (JwtException e) {
//            log.warn("Token validation error: {}", e.getMessage());
//            return false;
//        }
//    }

    public boolean validateToken(String token, String username) {
        try {
            Claims claims = parser.parseClaimsJws(token).getBody();
            boolean subjectOk = username.equals(claims.getSubject());
            boolean notExpired = claims.getExpiration().after(Date.from(clock.instant()));
            return subjectOk && notExpired;
        } catch (JwtException e) {
            log.warn("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    // todo test expiry token Clock

}
