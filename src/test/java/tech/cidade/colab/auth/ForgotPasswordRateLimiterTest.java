package tech.cidade.colab.auth;

import org.junit.jupiter.api.Test;
import tech.cidade.colab.exception.RateLimitExceededException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ForgotPasswordRateLimiterTest {

    @Test
    void fourthAttemptInWindowIsLimitedByEmail() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ForgotPasswordRateLimiter limiter = new ForgotPasswordRateLimiter(3, 900, clock);

        assertDoesNotThrow(() -> limiter.check("1.1.1.1", "a@b.com"));
        clock.plusSeconds(1);
        assertDoesNotThrow(() -> limiter.check("8.8.8.8", "a@b.com"));
        clock.plusSeconds(1);
        assertDoesNotThrow(() -> limiter.check("9.9.9.9", "a@b.com"));
        clock.plusSeconds(1);
        assertThrows(RateLimitExceededException.class, () -> limiter.check("4.4.4.4", "a@b.com"));
    }

    @Test
    void fourthAttemptInWindowIsLimitedByIp() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ForgotPasswordRateLimiter limiter = new ForgotPasswordRateLimiter(3, 900, clock);

        assertDoesNotThrow(() -> limiter.check("1.1.1.1", "a@b.com"));
        clock.plusSeconds(1);
        assertDoesNotThrow(() -> limiter.check("1.1.1.1", "c@d.com"));
        clock.plusSeconds(1);
        assertDoesNotThrow(() -> limiter.check("1.1.1.1", "e@f.com"));
        clock.plusSeconds(1);
        assertThrows(RateLimitExceededException.class, () -> limiter.check("1.1.1.1", "g@h.com"));
    }

    @Test
    void allowsAgainAfterWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ForgotPasswordRateLimiter limiter = new ForgotPasswordRateLimiter(3, 900, clock);

        limiter.check("1.1.1.1", "a@b.com");
        limiter.check("1.1.1.1", "a@b.com");
        limiter.check("1.1.1.1", "a@b.com");
        assertThrows(RateLimitExceededException.class, () -> limiter.check("1.1.1.1", "a@b.com"));

        clock.plusSeconds(901);
        assertDoesNotThrow(() -> limiter.check("1.1.1.1", "a@b.com"));
    }

    @Test
    void resetIpFourthAttemptIsLimited() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ForgotPasswordRateLimiter limiter = new ForgotPasswordRateLimiter(3, 900, clock);

        limiter.checkResetIp("1.1.1.1");
        limiter.checkResetIp("1.1.1.1");
        limiter.checkResetIp("1.1.1.1");
        assertThrows(RateLimitExceededException.class, () -> limiter.checkResetIp("1.1.1.1"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void plusSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
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
            return instant;
        }
    }
}
