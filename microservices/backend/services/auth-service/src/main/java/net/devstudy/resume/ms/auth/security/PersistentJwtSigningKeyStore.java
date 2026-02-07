package net.devstudy.resume.ms.auth.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

@Component
@ConditionalOnProperty(name = "app.security.oidc.enabled", havingValue = "true")
public class PersistentJwtSigningKeyStore {

    private static final short KEY_ROW_ID = 1;

    private final JdbcTemplate jdbcTemplate;

    public PersistentJwtSigningKeyStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RSAKey loadOrCreateSigningKey() {
        RSAKey existing = readSigningKey();
        if (existing != null) {
            return existing;
        }
        RSAKey generated = generateRsa();
        jdbcTemplate.update(
                "INSERT INTO public.oauth2_signing_key (id, jwk_json) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
                KEY_ROW_ID,
                generated.toJSONString()
        );
        RSAKey persisted = readSigningKey();
        if (persisted == null) {
            throw new IllegalStateException("Failed to load persistent JWT signing key");
        }
        return persisted;
    }

    private RSAKey readSigningKey() {
        List<String> jsonKeys = jdbcTemplate.query(
                "SELECT jwk_json FROM public.oauth2_signing_key WHERE id = ?",
                (rs, rowNum) -> rs.getString(1),
                KEY_ROW_ID
        );
        if (jsonKeys.isEmpty()) {
            return null;
        }
        String json = jsonKeys.get(0);
        try {
            JWK parsed = JWK.parse(json);
            if (!(parsed instanceof RSAKey rsaKey)) {
                throw new IllegalStateException("Stored JWT signing key is not RSA");
            }
            if (rsaKey.toPrivateKey() == null) {
                throw new IllegalStateException("Stored JWT signing key has no private part");
            }
            return rsaKey;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse stored JWT signing key", ex);
        }
    }

    private RSAKey generateRsa() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate RSA key", ex);
        }
    }
}
