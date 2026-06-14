package com.justin.projectmind.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed JWT configuration bound from {@code app.jwt.*}.
 *
 * @param secret                   HMAC signing secret (>= 32 chars for HS256)
 * @param accessTokenExpirationMs  access token lifetime in milliseconds
 * @param refreshTokenExpirationMs refresh token lifetime in milliseconds
 * @param issuer                   token issuer claim
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs,
        String issuer
) {
}
