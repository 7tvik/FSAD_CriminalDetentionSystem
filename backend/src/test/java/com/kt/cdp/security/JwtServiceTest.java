package com.kt.cdp.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtService}. The service is constructed directly with a
 * test secret and expiry (mirroring app.jwt.secret / app.jwt.expiration-ms), so no
 * Spring context is needed.
 */
class JwtServiceTest {

    private static final String SECRET = "test_secret_key_used_only_for_tests_at_least_32_bytes_long_xxxx";
    private static final long ONE_HOUR = 3_600_000L;

    private final JwtService jwtService = new JwtService(SECRET, ONE_HOUR);

    @Test
    void generatesTokenAndExtractsUsername() {
        String token = jwtService.generateToken("alice");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void validTokenIsValidForMatchingUsername() {
        String token = jwtService.generateToken("bob");

        assertThat(jwtService.isTokenValid(token, "bob")).isTrue();
        assertThat(jwtService.isTokenValid(token, "someone-else")).isFalse();
    }

    @Test
    void tamperedTokenIsInvalid() {
        String token = jwtService.generateToken("carol");
        // Flip the last character of the signature to break verification.
        char last = token.charAt(token.length() - 1);
        char replacement = last == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 1) + replacement;

        assertThat(jwtService.isTokenValid(tampered, "carol")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        // Negative expiry produces an already-expired token.
        JwtService expiring = new JwtService(SECRET, -1_000L);
        String token = expiring.generateToken("dave");

        assertThat(expiring.isTokenValid(token, "dave")).isFalse();
    }
}
