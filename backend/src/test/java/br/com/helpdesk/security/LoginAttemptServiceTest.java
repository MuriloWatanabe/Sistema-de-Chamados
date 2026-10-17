package br.com.helpdesk.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private MutableClock clock;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
        service = new LoginAttemptService(3, Duration.ofMinutes(15), clock);
    }

    @Test
    void notLockedBeforeReachingMaxAttempts() {
        service.registerFailure("user@helpdesk.com");
        service.registerFailure("user@helpdesk.com");

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isZero();
    }

    @Test
    void lockedAfterMaxAttempts() {
        failTimes("user@helpdesk.com", 3);

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isEqualTo(15 * 60);
    }

    @Test
    void lockIsCaseInsensitiveOnEmail() {
        failTimes("User@Helpdesk.com", 3);

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isPositive();
    }

    @Test
    void lockExpiresAfterDuration() {
        failTimes("user@helpdesk.com", 3);
        clock.advance(Duration.ofMinutes(16));

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isZero();
    }

    @Test
    void successResetsFailureCounter() {
        failTimes("user@helpdesk.com", 2);
        service.registerSuccess("user@helpdesk.com");
        failTimes("user@helpdesk.com", 2);

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isZero();
    }

    @Test
    void oldFailuresOutsideWindowDoNotAccumulate() {
        failTimes("user@helpdesk.com", 2);
        clock.advance(Duration.ofMinutes(20));
        service.registerFailure("user@helpdesk.com");

        assertThat(service.remainingLockSeconds("user@helpdesk.com")).isZero();
    }

    @Test
    void lockingOneEmailDoesNotAffectOthers() {
        failTimes("user@helpdesk.com", 3);

        assertThat(service.remainingLockSeconds("other@helpdesk.com")).isZero();
    }

    private void failTimes(String email, int times) {
        for (int i = 0; i < times; i++) {
            service.registerFailure(email);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
