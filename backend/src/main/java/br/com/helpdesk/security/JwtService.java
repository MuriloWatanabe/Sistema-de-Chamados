package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;

@Service
public class JwtService {
    public static final String DEFAULT_ISSUER = "helpdesk-api";
    // HS256 exige chave de pelo menos 256 bits (RFC 7518, secao 3.2).
    static final int MIN_SECRET_BYTES = 32;
    // Tolerancia para pequenas diferencas de relogio entre servidores.
    private static final long CLOCK_SKEW_SECONDS = 30;
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final SecretKeySpec secretKey;
    private final Duration expiration;
    private final String issuer;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    @Autowired
    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes:120}") long expirationMinutes,
            @Value("${app.security.jwt.issuer:" + DEFAULT_ISSUER + "}") String issuer
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must have at least " + MIN_SECRET_BYTES + " bytes. Set APP_SECURITY_JWT_SECRET."
            );
        }
        this.objectMapper = objectMapper;
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.issuer = issuer;
    }

    public JwtService(ObjectMapper objectMapper, String secret, long expirationMinutes) {
        this(objectMapper, secret, expirationMinutes, DEFAULT_ISSUER);
    }

    public String generateToken(User user) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            Instant now = Instant.now();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("iss", issuer);
            payload.put("sub", user.getEmail());
            payload.put("jti", UUID.randomUUID().toString());
            payload.put("uid", user.getId());
            payload.put("name", user.getName());
            payload.put("email", user.getEmail());
            payload.put("role", user.getRole());
            payload.put("ver", user.getTokenVersion() == null ? 0 : user.getTokenVersion());
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

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }

    public TokenClaims validateAndParse(String token) {
        try {
            if (token == null || token.isBlank()) {
                throw new InvalidTokenException("Invalid token format");
            }
            String[] parts = token.split("\\.", -1);
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

            // Mesmo com assinatura valida, so aceitamos o algoritmo esperado (evita confusao de algoritmo / "alg": "none").
            Map<String, Object> header = objectMapper.readValue(decodeJson(parts[0]), MAP_TYPE);
            if (!"HS256".equals(header.get("alg"))) {
                throw new InvalidTokenException("Unsupported token algorithm");
            }

            Map<String, Object> payload = objectMapper.readValue(decodeJson(parts[1]), MAP_TYPE);
            if (!issuer.equals(payload.get("iss"))) {
                throw new InvalidTokenException("Invalid token issuer");
            }

            long nowSeconds = Instant.now().getEpochSecond();
            long expiresAt = toLong(payload.get("exp"));
            if (nowSeconds >= expiresAt) {
                throw new InvalidTokenException("Token expired");
            }
            long issuedAt = toLong(payload.get("iat"));
            if (issuedAt > nowSeconds + CLOCK_SKEW_SECONDS) {
                throw new InvalidTokenException("Token issued in the future");
            }

            Object version = payload.get("ver");
            return new TokenClaims(
                    toLong(payload.get("uid")),
                    String.valueOf(payload.get("name")),
                    String.valueOf(payload.get("email")),
                    UserRole.fromCode((int) toLong(payload.get("role"))),
                    issuedAt,
                    expiresAt,
                    version == null ? 0 : (int) toLong(version),
                    payload.get("jti") == null ? null : String.valueOf(payload.get("jti"))
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
        if (value == null) {
            throw new InvalidTokenException("Missing token claim");
        }
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
            long expiresAt,
            int tokenVersion,
            String tokenId
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
