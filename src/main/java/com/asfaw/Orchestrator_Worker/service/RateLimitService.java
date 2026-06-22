package com.asfaw.Orchestrator_Worker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Service using Redis.
 * 
 * Protects API endpoints from abuse by limiting requests per user/IP.
 * Uses Redis counters with TTL to track request counts within time windows.
 * 
 * Example Usage:
 * <pre>
 * // Allow max 10 requests per minute per user
 * if (rateLimitService.isAllowed("user:123", 10, 60)) {
 *     // Process request
 * } else {
 *     // Return 429 Too Many Requests
 * }
 * </pre>
 * 
 * @author TaskForge Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    
    /**
     * Check if request is allowed under rate limit
     * 
     * @param key unique identifier (e.g., "user:123", "ip:192.168.1.1")
     * @param maxRequests maximum requests allowed
     * @param windowSeconds time window in seconds
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            // Increment counter
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            
            if (currentCount == null) {
                log.error("Failed to increment rate limit counter for key: {}", key);
                return true; // Fail open (allow request if Redis error)
            }
            
            // Set expiration on first request
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            
            boolean allowed = currentCount <= maxRequests;
            
            if (!allowed) {
                log.warn("Rate limit exceeded for key: {} ({}/**{} in {}s)", 
                        key, currentCount, maxRequests, windowSeconds);
            } else {
                log.debug("Rate limit check: {} ({}/{} in {}s)", 
                        key, currentCount, maxRequests, windowSeconds);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return true; // Fail open on error
        }
    }
    
    /**
     * Get current request count for a key
     * 
     * @param key unique identifier
     * @return current count, or 0 if key doesn't exist
     */
    public long getCurrentCount(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            Object value = redisTemplate.opsForValue().get(redisKey);
            return value != null ? Long.parseLong(value.toString()) : 0;
        } catch (Exception e) {
            log.error("Error getting rate limit count for key: {}", key, e);
            return 0;
        }
    }
    
    /**
     * Get remaining time until rate limit resets
     * 
     * @param key unique identifier
     * @return seconds until reset, or -1 if key doesn't exist
     */
    public long getTimeToReset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            log.error("Error getting rate limit TTL for key: {}", key, e);
            return -1;
        }
    }
    
    /**
     * Reset rate limit for a key (manual override)
     * 
     * @param key unique identifier
     */
    public void reset(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        
        try {
            redisTemplate.delete(redisKey);
            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
    
    /**
     * Check rate limit and return detailed info
     * 
     * @param key unique identifier
     * @param maxRequests maximum requests allowed
     * @param windowSeconds time window in seconds
     * @return rate limit info with current count and time to reset
     */
    public RateLimitInfo checkLimit(String key, int maxRequests, int windowSeconds) {
        boolean allowed = isAllowed(key, maxRequests, windowSeconds);
        long currentCount = getCurrentCount(key);
        long timeToReset = getTimeToReset(key);
        long remaining = Math.max(0, maxRequests - currentCount);
        
        return new RateLimitInfo(
                allowed,
                currentCount,
                maxRequests,
                remaining,
                timeToReset
        );
    }
    
    /**
     * Rate limit information
     */
    public record RateLimitInfo(
            boolean allowed,
            long currentCount,
            long limit,
            long remaining,
            long resetInSeconds
    ) {}
}
