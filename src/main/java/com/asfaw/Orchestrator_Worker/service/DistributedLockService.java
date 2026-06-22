package com.asfaw.Orchestrator_Worker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Distributed Lock Service using Redisson.
 * 
 * Prevents duplicate task processing across multiple application instances.
 * Uses Redis-based distributed locks to ensure only one instance processes a task.
 * 
 * Example Usage:
 * <pre>
 * boolean success = lockService.executeWithLock(
 *     "task:" + taskId,
 *     10,  // wait up to 10 seconds to acquire lock
 *     30,  // hold lock for max 30 seconds
 *     () -> {
 *         // Process task here
 *         return processTask(task);
 *     }
 * );
 * </pre>
 * 
 * @author TaskForge Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {
    
    private final RedissonClient redissonClient;
    
    /**
     * Execute code with distributed lock protection
     * 
     * @param lockKey unique lock identifier (e.g., "task:abc-123")
     * @param waitTime maximum time to wait for lock (seconds)
     * @param leaseTime maximum time to hold lock (seconds)
     * @param action code to execute while holding lock
     * @return true if lock acquired and action executed, false otherwise
     */
    public boolean executeWithLock(
            String lockKey,
            long waitTime,
            long leaseTime,
            Runnable action
    ) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Try to acquire lock
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            
            if (acquired) {
                try {
                    log.debug("Lock acquired: {}", lockKey);
                    action.run();
                    return true;
                } finally {
                    // Always release lock
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("Lock released: {}", lockKey);
                    }
                }
            } else {
                log.warn("Failed to acquire lock: {} (timeout after {}s)", lockKey, waitTime);
                return false;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("Error executing with lock: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * Execute code with distributed lock and return result
     * 
     * @param lockKey unique lock identifier
     * @param waitTime maximum time to wait for lock (seconds)
     * @param leaseTime maximum time to hold lock (seconds)
     * @param supplier code to execute that returns a value
     * @param <T> return type
     * @return result of supplier if lock acquired, null otherwise
     */
    public <T> T executeWithLockAndReturn(
            String lockKey,
            long waitTime,
            long leaseTime,
            Supplier<T> supplier
    ) {
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            
            if (acquired) {
                try {
                    log.debug("Lock acquired: {}", lockKey);
                    return supplier.get();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("Lock released: {}", lockKey);
                    }
                }
            } else {
                log.warn("Failed to acquire lock: {} (timeout after {}s)", lockKey, waitTime);
                return null;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            return null;
        } catch (Exception e) {
            log.error("Error executing with lock: {}", lockKey, e);
            return null;
        }
    }
    
    /**
     * Check if lock exists (is held by any instance)
     * 
     * @param lockKey lock identifier
     * @return true if lock is currently held
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
    
    /**
     * Force unlock (use with caution!)
     * Only unlocks if current thread holds the lock
     * 
     * @param lockKey lock identifier
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.warn("Force unlocked: {}", lockKey);
        } else {
            log.warn("Cannot force unlock {} - not held by current thread", lockKey);
        }
    }
}
