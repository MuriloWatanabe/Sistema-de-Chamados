package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final SecretKeySpec secretKey;
    private final Duration expiration;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes:120}") long expirationMinutes
    ) {
        this.objectMapper = objectMapper;
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String generateToken(User user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Instant now = Instant.now();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getEmail());
            payload.put("uid", user.getId());
            payload.put("name", user.getName());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole());
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", now.plus(expiration).getEpochSecond());

            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signature = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Could not generate token", ex);
        }
    }

    public TokenClaims validateAndParse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new InvalidTokenException("Invalid token format");
            }

            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = sign(signingInput);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            )) {
                throw new InvalidTokenException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(decodeJson(parts[1]), MAP_TYPE);
            long expiresAt = toLong(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= expiresAt) {
                throw new InvalidTokenException("Token expired");
            }

            return new TokenClaims(
                    toLong(payload.get("uid")),
                    String.valueOf(payload.get("name")),
                    String.valueOf(payload.get("email")),
                    UserRole.fromCode((int) toLong(payload.get("role"))),
                    toLong(payload.get("iat")),
                    expiresAt
            );
        } catch (InvalidTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidTokenException("Could not validate token", ex);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return encoder.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String decodeJson(String value) {
        return new String(decoder.decode(value), StandardCharsets.UTF_8);
    }

    private String sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        return encoder.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    public record TokenClaims(
            Long userId,
            String name,
            String email,
            UserRole role,
            long issuedAt,
            long expiresAt
    ) {
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
