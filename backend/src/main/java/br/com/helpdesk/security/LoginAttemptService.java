package br.com.helpdesk.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueio temporario contra forca bruta: apos N falhas seguidas para o mesmo e-mail,
 * novas tentativas sao recusadas ate o fim da janela de bloqueio.
 * Mantido em memoria (suficiente para uma unica instancia da API).
 */
@Service
public class LoginAttemptService {
    private final int maxAttempts;
    private final Duration lockDuration;
    private final Clock clock;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${app.security.login.max-attempts:5}") int maxAttempts,
            @Value("${app.security.login.lock-minutes:15}") long lockMinutes
    ) {
        this(maxAttempts, Duration.ofMinutes(lockMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration lockDuration, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    /** Segundos restantes de bloqueio, ou 0 se o login esta liberado. */
    public long remainingLockSeconds(String email) {
        Attempt attempt = attempts.get(key(email));
        if (attempt == null || attempt.lockedUntil == null) {
            return 0;
        }
        long remaining = Duration.between(clock.instant(), attempt.lockedUntil).toSeconds();
        if (remaining <= 0) {
            attempts.remove(key(email));
            return 0;
        }
        return remaining;
    }

    public void registerFailure(String email) {
        Instant now = clock.instant();
        attempts.compute(key(email), (k, current) -> {
            Attempt attempt = current == null || current.isExpired(now, lockDuration) ? new Attempt(now) : current;
            attempt.failures++;
            attempt.lastFailure = now;
            if (attempt.failures >= maxAttempts) {
                attempt.lockedUntil = now.plus(lockDuration);
            }
            return attempt;
        });
    }

    public void registerSuccess(String email) {
        attempts.remove(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Attempt {
        private int failures;
        private Instant lastFailure;
        private Instant lockedUntil;

        private Attempt(Instant now) {
            this.lastFailure = now;
        }

        // Falhas antigas (fora da janela) nao acumulam.
        private boolean isExpired(Instant now, Duration window) {
            if (lockedUntil != null) {
                return now.isAfter(lockedUntil);
            }
            return lastFailure.plus(window).isBefore(now);
        }
    }
}
