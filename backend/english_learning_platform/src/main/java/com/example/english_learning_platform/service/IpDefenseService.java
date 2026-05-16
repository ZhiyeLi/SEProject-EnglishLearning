package com.example.english_learning_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IpDefenseService {

    private final Map<String, Deque<Long>> attemptsStore = new ConcurrentHashMap<>();

    @Value("${auth.ip.window-minutes:5}")
    private int windowMinutes;

    @Value("${auth.ip.max-attempts:5}")
    private int maxAttempts;

    public boolean recordAttemptAndCheck(String action, String ip) {
        String key = buildKey(action, ip);
        long now = System.currentTimeMillis();
        Deque<Long> deque = attemptsStore.computeIfAbsent(key, k -> new ArrayDeque<>());
        long windowMs = Math.max(windowMinutes, 1) * 60_000L;

        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            deque.addLast(now);
            return deque.size() > maxAttempts;
        }
    }

    public void clearAttempts(String action, String ip) {
        attemptsStore.remove(buildKey(action, ip));
    }

    private String buildKey(String action, String ip) {
        String safeIp = (ip == null || ip.isBlank()) ? "unknown" : ip;
        return action + ":" + safeIp;
    }
}
