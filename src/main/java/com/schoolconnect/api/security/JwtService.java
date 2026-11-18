package com.schoolconnect.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    public record TokenData(String userId, List<String> authorities) {
    }

    private final SecretKey key;
    private final Duration ttl;

    public JwtService(JwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("schoolconnect.jwt.secret doit contenir au moins 32 caractères");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.ttl = Duration.ofMinutes(properties.expirationMinutes());
    }

    public String generate(UUID userId, List<String> authorities) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public long expiresInSeconds() {
        return ttl.toSeconds();
    }

    public Optional<TokenData> parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Object raw = claims.get("roles");
            List<String> authorities = raw instanceof List<?> list
                    ? list.stream().map(String::valueOf).toList()
                    : List.of();
            return Optional.of(new TokenData(claims.getSubject(), authorities));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
