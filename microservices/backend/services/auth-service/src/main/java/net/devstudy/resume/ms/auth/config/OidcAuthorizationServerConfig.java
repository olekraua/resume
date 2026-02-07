package net.devstudy.resume.ms.auth.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.jdbc.core.JdbcTemplate;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import net.devstudy.resume.auth.api.model.CurrentProfile;
import net.devstudy.resume.ms.auth.security.PersistentJwtSigningKeyStore;
import net.devstudy.resume.web.security.CurrentProfileJwtConverter;
import net.devstudy.resume.web.security.JwtAuthenticationFailureEntryPoint;

@Configuration
@ConditionalOnProperty(name = "app.security.oidc.enabled", havingValue = "true")
public class OidcAuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.oidc(withDefaults());

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.securityMatcher(endpointsMatcher)
                .cors(withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .apply(authorizationServerConfigurer);

        http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
        );
        return http.build();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
            CurrentProfileJwtConverter currentProfileJwtConverter,
            JwtAuthenticationFailureEntryPoint jwtAuthenticationFailureEntryPoint) throws Exception {
        http
                .cors(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/features").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/uid-hint").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/restore").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/restore/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/restore/*").permitAll()
                        .requestMatchers("/api/csrf").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(jwtAuthenticationFailureEntryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(currentProfileJwtConverter)))
                .formLogin(withDefaults());
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
            @Value("${app.security.oidc.client-id:resume-spa}") String clientId,
            @Value("${app.security.oidc.redirect-uri:http://localhost:4200/auth/callback}") String redirectUri,
            @Value("${app.security.oidc.post-logout-redirect-uri:http://localhost:4200/}") String postLogoutRedirectUri,
            @Value("${app.security.oidc.access-token-ttl:PT10M}") Duration accessTokenTtl) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);
        RegisteredClient existing = repository.findByClientId(clientId);
        if (!isPublicSpaClientUpToDate(existing, redirectUri, postLogoutRedirectUri, accessTokenTtl)) {
            String id = existing != null ? existing.getId() : UUID.randomUUID().toString();
            repository.save(buildPublicSpaClient(id, clientId, redirectUri, postLogoutRedirectUri, accessTokenTtl));
        }
        return repository;
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(PersistentJwtSigningKeyStore persistentJwtSigningKeyStore) {
        return (jwkSelector, securityContext) ->
                jwkSelector.select(persistentJwtSigningKeyStore.loadOrCreateSigningJwkSet());
    }

    @Bean
    public SmartInitializingSingleton oidcSigningKeyStoreFailFastInitializer(
            PersistentJwtSigningKeyStore persistentJwtSigningKeyStore) {
        return persistentJwtSigningKeyStore::verifyStoreAvailabilityOrFailFast;
    }

    @Bean
    public SmartInitializingSingleton oidcTokenLifetimeWindowGuard(
            @Value("${app.security.oidc.access-token-ttl:PT10M}") Duration accessTokenTtl,
            @Value("${app.security.oidc.signing-key.token-ttl:${app.security.oidc.access-token-ttl:PT10M}}") Duration signingKeyTokenTtl,
            @Value("${app.security.oidc.signing-key.clock-skew:PT1M}") Duration signingKeyClockSkew) {
        return () -> validateTokenLifetimeWindow(accessTokenTtl, signingKeyTokenTtl, signingKeyClockSkew);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.security.oidc.issuer:http://localhost:8080}") String issuer) {
        return AuthorizationServerSettings.builder()
                .issuer(issuer)
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getPrincipal() != null && context.getPrincipal().getPrincipal() instanceof CurrentProfile profile) {
                context.getClaims().claim("profile_id", profile.getId());
                context.getClaims().claim("uid", profile.getUsername());
                context.getClaims().claim("name", profile.getFullName());
            }
        };
    }

    private RegisteredClient buildPublicSpaClient(String id, String clientId, String redirectUri,
            String postLogoutRedirectUri,
            Duration accessTokenTtl) {
        return RegisteredClient.withId(id)
                .clientId(clientId)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .postLogoutRedirectUri(postLogoutRedirectUri)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("offline_access")
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(accessTokenTtl)
                        .reuseRefreshTokens(false)
                        .build())
                .build();
    }

    private boolean isPublicSpaClientUpToDate(RegisteredClient existing,
            String redirectUri,
            String postLogoutRedirectUri,
            Duration accessTokenTtl) {
        if (existing == null) {
            return false;
        }
        TokenSettings tokenSettings = existing.getTokenSettings();
        return existing.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)
                && existing.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE)
                && existing.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
                && existing.getRedirectUris().contains(redirectUri)
                && existing.getPostLogoutRedirectUris().contains(postLogoutRedirectUri)
                && existing.getScopes().contains(OidcScopes.OPENID)
                && existing.getScopes().contains(OidcScopes.PROFILE)
                && existing.getScopes().contains("offline_access")
                && existing.getClientSettings().isRequireProofKey()
                && !existing.getClientSettings().isRequireAuthorizationConsent()
                && tokenSettings != null
                && accessTokenTtl.equals(tokenSettings.getAccessTokenTimeToLive())
                && !tokenSettings.isReuseRefreshTokens();
    }

    private void validateTokenLifetimeWindow(Duration accessTokenTtl,
            Duration signingKeyTokenTtl,
            Duration signingKeyClockSkew) {
        if (accessTokenTtl == null || accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalStateException("app.security.oidc.access-token-ttl must be positive");
        }
        if (signingKeyTokenTtl == null || signingKeyTokenTtl.isNegative() || signingKeyTokenTtl.isZero()) {
            throw new IllegalStateException("app.security.oidc.signing-key.token-ttl must be positive");
        }
        if (signingKeyClockSkew != null && signingKeyClockSkew.isNegative()) {
            throw new IllegalStateException("app.security.oidc.signing-key.clock-skew must be non-negative");
        }
        if (accessTokenTtl.compareTo(signingKeyTokenTtl) > 0) {
            throw new IllegalStateException(
                    "access-token-ttl must be <= signing-key.token-ttl to keep old kid available for active tokens");
        }
    }
}
