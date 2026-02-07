package net.devstudy.resume.ms.messaging.ws;

import java.util.List;
import java.util.Locale;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.devstudy.resume.web.security.CurrentProfileJwtConverter;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    private static final List<String> AUTH_HEADER_NAMES = List.of(
            "Authorization",
            "authorization",
            "X-Authorization",
            "x-authorization"
    );

    private final JwtDecoder jwtDecoder;
    private final CurrentProfileJwtConverter jwtConverter;

    public WebSocketAuthChannelInterceptor(JwtDecoder jwtDecoder, CurrentProfileJwtConverter jwtConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtConverter = jwtConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveBearer(accessor);
            if (token == null || token.isBlank()) {
                throw new AccessDeniedException("Unauthorized");
            }
            Jwt jwt;
            try {
                jwt = jwtDecoder.decode(token);
            } catch (RuntimeException ex) {
                logJwtValidationError(ex);
                throw ex;
            }
            AbstractAuthenticationToken authentication = jwtConverter.convert(jwt);
            if (authentication == null) {
                throw new AccessDeniedException("Unauthorized");
            }
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                || StompCommand.SEND.equals(accessor.getCommand())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                var principal = accessor.getUser();
                if (principal instanceof Authentication auth) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        return message;
    }

    private String resolveBearer(StompHeaderAccessor accessor) {
        if (accessor == null) {
            return null;
        }
        for (String name : AUTH_HEADER_NAMES) {
            String header = accessor.getFirstNativeHeader(name);
            if (header == null || header.isBlank()) {
                continue;
            }
            String trimmed = header.trim();
            if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return trimmed.substring(7).trim();
            }
        }
        return null;
    }

    private void logJwtValidationError(RuntimeException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        String normalized = message.toLowerCase(Locale.ROOT);
        String errorType;
        if (normalized.contains("invalid signature")
                || normalized.contains("bad jws signature")
                || normalized.contains("jws verification failed")) {
            errorType = "invalid_signature";
        } else if (normalized.contains("no matching key(s) found")
                || normalized.contains("no matching key found")
                || (normalized.contains("kid") && normalized.contains("not found"))) {
            errorType = "unknown_kid";
        } else {
            return;
        }
        LOGGER.error("jwt_validation_error=1 jwt_error_type={} transport=websocket error_class={} reason=\"{}\"",
                errorType,
                ex.getClass().getSimpleName(),
                sanitize(message));
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\n', ' ')
                .replace('\r', ' ')
                .replace('"', '\'')
                .trim();
        if (normalized.length() > 300) {
            return normalized.substring(0, 300);
        }
        return normalized;
    }
}
