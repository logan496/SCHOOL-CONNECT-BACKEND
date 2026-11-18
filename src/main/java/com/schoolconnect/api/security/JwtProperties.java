package com.schoolconnect.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "schoolconnect.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
