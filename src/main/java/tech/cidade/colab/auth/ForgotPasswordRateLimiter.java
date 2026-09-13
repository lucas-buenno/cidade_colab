package tech.cidade.colab.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.cidade.colab.exception.RateLimitExceededException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ForgotPasswordRateLimiter {

    private final int maxAttempts;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    @Autowired
    public ForgotPasswordRateLimiter(
            @Value("${keycloak.forgot-password.rate-limit.max-attempts:3}") int maxAttempts,
            @Value("${keycloak.forgot-password.rate-limit.window-seconds:900}") long windowSeconds
    ) {
        this(maxAttempts, windowSeconds, Clock.systemUTC());
    }

    public ForgotPasswordRateLimiter(int maxAttempts, long windowSeconds, Clock clock) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
        this.clock = clock;
    }


    public void check(String clientIp, String normalizedEmail) {
        String ipKey = "ip:" + (clientIp == null || clientIp.isBlank() ? "unknown" : clientIp);
        String emailKey = "email:" + sha256(normalizedEmail);
        long now = clock.millis();

        if (isLimited(ipKey, now) || isLimited(emailKey, now)) {
            throw new RateLimitExceededException();
        }

        record(ipKey, now);
        record(emailKey, now);
    }

    public void checkResetIp(String clientIp) {
        String ipKey = "reset-ip:" + (clientIp == null || clientIp.isBlank() ? "unknown" : clientIp);
        long now = clock.millis();
        if (isLimited(ipKey, now)) {
            throw new RateLimitExceededException();
        }
        record(ipKey, now);
    }

    private boolean isLimited(String key, long now) {
        Deque<Long> timestamps = prune(key, now);
        return timestamps.size() >= maxAttempts;
    }

    private void record(String key, long now) {
        attempts.compute(key, (ignored, existing) -> {
            Deque<Long> timestamps = existing == null ? new ArrayDeque<>() : existing;
            dropExpired(timestamps, now);
            timestamps.addLast(now);
            return timestamps;
        });
    }

    private Deque<Long> prune(String key, long now) {
        Deque<Long> timestamps = attempts.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        dropExpired(timestamps, now);
        return timestamps;
    }

    private void dropExpired(Deque<Long> timestamps, long now) {
        long cutoff = now - window.toMillis();
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.removeFirst();
        }
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
