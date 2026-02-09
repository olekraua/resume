package net.devstudy.resume.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import net.devstudy.resume.auth.api.model.CurrentProfile;

class CurrentProfileJwtConverterTest {

    private final CurrentProfileJwtConverter converter = new CurrentProfileJwtConverter();

    @Test
    void shouldConvertNumericProfileIdClaim() {
        Jwt jwt = jwt(Map.of(
                "sub", "john",
                "uid", "john",
                "name", "John Doe",
                "profile_id", 42L));

        CurrentProfile principal = convert(jwt);

        assertEquals(42L, principal.getId());
        assertEquals("john", principal.getUsername());
        assertEquals("John Doe", principal.getFullName());
    }

    @Test
    void shouldConvertStringProfileIdClaim() {
        Jwt jwt = jwt(Map.of(
                "sub", "jane",
                "uid", "jane",
                "name", "Jane Doe",
                "profile_id", "7"));

        CurrentProfile principal = convert(jwt);

        assertEquals(7L, principal.getId());
        assertEquals("jane", principal.getUsername());
        assertEquals("Jane Doe", principal.getFullName());
    }

    @Test
    void shouldIgnoreInvalidStringProfileIdClaim() {
        Jwt jwt = jwt(Map.of(
                "sub", "jane",
                "uid", "jane",
                "name", "Jane Doe",
                "profile_id", "not-a-number"));

        CurrentProfile principal = convert(jwt);

        assertNull(principal.getId());
        assertEquals("jane", principal.getUsername());
        assertEquals("Jane Doe", principal.getFullName());
    }

    private CurrentProfile convert(Jwt jwt) {
        AbstractAuthenticationToken token = converter.convert(jwt);
        return (CurrentProfile) token.getPrincipal();
    }

    private Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        claims.forEach(builder::claim);
        return builder.build();
    }
}
