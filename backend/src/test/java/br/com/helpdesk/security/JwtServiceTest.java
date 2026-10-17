package br.com.helpdesk.security;

import br.com.helpdesk.entities.User;
import br.com.helpdesk.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class JwtServiceTest {

    private static final String SECRET = "test-secret-for-unit-tests-only!!";
    private static final long EXPIRATION_MINUTES = 120L;

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new ObjectMapper(), SECRET, EXPIRATION_MINUTES);

        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@helpdesk.com");
        user.setPassword("hashed");
        user.setRole(UserRole.TECHNICIAN.getCode());
        user.setActive(true);
    }

    // --- generateToken ---

    @Test
    void generateToken_returnsThreePartJwtString() {
        String token = jwtService.generateToken(user);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    void generateToken_producesNonEmptyToken() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_producesDifferentTokensOnSubsequentCalls() throws InterruptedException {
        String token1 = jwtService.generateToken(user);
        Thread.sleep(1100);
        String token2 = jwtService.generateToken(user);

        assertThat(token1).isNotEqualTo(token2);
    }

    // --- validateAndParse ---

    @Test
    void validateAndParse_returnsCorrectUserIdFromToken() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.userId()).isEqualTo(1L);
    }

    @Test
    void validateAndParse_returnsCorrectEmailFromToken() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.email()).isEqualTo("joao@helpdesk.com");
    }

    @Test
    void validateAndParse_returnsCorrectNameFromToken() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.name()).isEqualTo("João Silva");
    }

    @Test
    void validateAndParse_returnsCorrectRoleFromToken() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.role()).isEqualTo(UserRole.TECHNICIAN);
    }

    @Test
    void validateAndParse_throwsInvalidTokenExceptionForExpiredToken() {
        JwtService expiredJwtService = new JwtService(new ObjectMapper(), SECRET, -120L);
        String expiredToken = expiredJwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.validateAndParse(expiredToken))
                .isInstanceOf(JwtService.InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateAndParse_throwsInvalidTokenExceptionForTamperedSignature() {
        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidsignature";

        assertThatThrownBy(() -> jwtService.validateAndParse(tamperedToken))
                .isInstanceOf(JwtService.InvalidTokenException.class);
    }

    @Test
    void validateAndParse_throwsInvalidTokenExceptionForMissingParts() {
        assertThatThrownBy(() -> jwtService.validateAndParse("only.two"))
                .isInstanceOf(JwtService.InvalidTokenException.class)
                .hasMessageContaining("format");
    }

    @Test
    void validateAndParse_throwsInvalidTokenExceptionForEmptyString() {
        assertThatThrownBy(() -> jwtService.validateAndParse(""))
                .isInstanceOf(JwtService.InvalidTokenException.class);
    }

    @Test
    void validateAndParse_throwsInvalidTokenExceptionForTokenSignedWithDifferentSecret() {
        JwtService otherJwtService = new JwtService(new ObjectMapper(), "completely-different-secret-with-32-bytes!!", EXPIRATION_MINUTES);
        String tokenFromOther = otherJwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.validateAndParse(tokenFromOther))
                .isInstanceOf(JwtService.InvalidTokenException.class);
    }

    @Test
    void validateAndParse_returnsPositiveIssuedAtTimestamp() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.issuedAt()).isPositive();
    }

    @Test
    void validateAndParse_returnsExpiresAtGreaterThanIssuedAt() {
        String token = jwtService.generateToken(user);

        JwtService.TokenClaims claims = jwtService.validateAndParse(token);

        assertThat(claims.expiresAt()).isGreaterThan(claims.issuedAt());
    }

    // --- endurecimento do token ---

    @Test
    void constructor_rejectsSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService(new ObjectMapper(), "short-secret", EXPIRATION_MINUTES))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void generateToken_includesUniqueTokenIdAndVersion() {
        user.setTokenVersion(3);

        JwtService.TokenClaims first = jwtService.validateAndParse(jwtService.generateToken(user));
        JwtService.TokenClaims second = jwtService.validateAndParse(jwtService.generateToken(user));

        assertThat(first.tokenVersion()).isEqualTo(3);
        assertThat(first.tokenId()).isNotBlank().isNotEqualTo(second.tokenId());
    }

    @Test
    void validateAndParse_rejectsTokenFromAnotherIssuer() {
        JwtService otherIssuer = new JwtService(new ObjectMapper(), SECRET, EXPIRATION_MINUTES, "other-api");
        String token = otherIssuer.generateToken(user);

        assertThatThrownBy(() -> jwtService.validateAndParse(token))
                .isInstanceOf(JwtService.InvalidTokenException.class)
                .hasMessageContaining("issuer");
    }

    @Test
    void validateAndParse_rejectsAlgNoneToken() {
        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");
        String noneHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> jwtService.validateAndParse(noneHeader + "." + parts[1] + "."))
                .isInstanceOf(JwtService.InvalidTokenException.class);
    }

    @Test
    void validateAndParse_rejectsTamperedPayload() {
        String token = jwtService.generateToken(user);
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"role\":2", "\"role\":0");
        String tamperedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> jwtService.validateAndParse(parts[0] + "." + tamperedPayload + "." + parts[2]))
                .isInstanceOf(JwtService.InvalidTokenException.class)
                .hasMessageContaining("signature");
    }

    @Test
    void validateAndParse_rejectsNullToken() {
        assertThatThrownBy(() -> jwtService.validateAndParse(null))
                .isInstanceOf(JwtService.InvalidTokenException.class);
    }
}
