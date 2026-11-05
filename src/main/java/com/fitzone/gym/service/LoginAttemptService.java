package com.fitzone.gym.service;

import com.fitzone.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);
    private static final int MAX_TRACKED = 10_000;

    private record Attempt(int count, Instant lockedUntil) { }

    private final UserRepository users;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    private String key(String typed) {
        String id = typed == null ? "" : typed.trim();
        return users.findByEmailOrMobileOrMemberCode(id.toLowerCase(), id, id.toUpperCase())
                .map(u -> "user:" + u.getId())
                .orElse("name:" + id.toLowerCase());
    }

    public boolean isLocked(String typed) {
        String k = key(typed);
        Attempt a = attempts.get(k);
        if (a == null || a.lockedUntil() == null) return false;
        if (Instant.now().isAfter(a.lockedUntil())) {
            attempts.remove(k);
            return false;
        }
        return true;
    }

    public void failed(String typed) {
        if (attempts.size() > MAX_TRACKED) attempts.clear();    // memory na bhare

        attempts.merge(key(typed), new Attempt(1, null), (old, fresh) -> {
            int c = old.count() + 1;
            return new Attempt(c, c >= MAX_ATTEMPTS ? Instant.now().plus(LOCK_TIME) : null);
        });
    }

    public void succeeded(String typed) {
        attempts.remove(key(typed));
    }
}