package com.asfaw.Orchestrator_Worker.service;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Task Cache Service using Redis.
 * 
 * Caches task data to reduce database load:
 * - Task status: Cached for 5 seconds (frequently updated)
 * - Task results: Cached for 30 minutes (rarely change after completion)
 * 
 * Features:
 * - Automatic cache invalidation on task updates
 * - TTL-based expiration
 * - Cache-aside pattern for manual caching
 * 
 * @author TaskForge Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TASK_CACHE_PREFIX = "task:";
    private static final String TASK_STATUS_CACHE_PREFIX = "task:status:";
    private static final Duration TASK_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration STATUS_CACHE_TTL = Duration.ofSeconds(5);
    
    /**
     * Cache task object
     * 
     * @param task task to cache
     */
    public void cacheTask(Task task) {
        if (task == null || task.getTaskId() == null) {
            return;
        }
        
        try {
            String key = TASK_CACHE_PREFIX + task.getTaskId();
            redisTemplate.opsForValue().set(key, task, TASK_CACHE_TTL);
            
            log.debug("Cached task: {}", task.getTaskId());
        } catch (Exception e) {
            log.error("Error caching task: {}", task.getTaskId(), e);
        }
    }
    
    /**
     * Get cached task
     * 
     * @param taskId task identifier
     * @return cached task if found
     */
    public Optional<Task> getCachedTask(String taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        
        try {
            String key = TASK_CACHE_PREFIX + taskId;
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached instanceof Task task) {
                log.debug("Task found in cache: {}", taskId);
                return Optional.of(task);
            }
            
            log.debug("Task not in cache: {}", taskId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error getting cached task: {}", taskId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Invalidate (remove) task from cache
     * 
     * @param taskId task identifier
     */
    public void invalidateTask(String taskId) {
        if (taskId == null) {
            return;
        }
        
        try {
            String taskKey = TASK_CACHE_PREFIX + taskId;
            String statusKey = TASK_STATUS_CACHE_PREFIX + taskId;
            
            redisTemplate.delete(taskKey);
            redisTemplate.delete(statusKey);
            
            log.debug("Invalidated cache for task: {}", taskId);
        } catch (Exception e) {
            log.error("Error invalidating cache for task: {}", taskId, e);
        }
    }
    
    /**
     * Cache task status only (lighter than full task)
     * 
     * @param taskId task identifier
     * @param status task status
     */
    public void cacheTaskStatus(String taskId, TaskStatus status) {
        if (taskId == null || status == null) {
            return;
        }
        
        try {
            String key = TASK_STATUS_CACHE_PREFIX + taskId;
            redisTemplate.opsForValue().set(key, status.name(), STATUS_CACHE_TTL);
            
            log.debug("Cached task status: {} -> {}", taskId, status);
        } catch (Exception e) {
            log.error("Error caching task status: {}", taskId, e);
        }
    }
    
    /**
     * Get cached task status
     * 
     * @param taskId task identifier
     * @return cached status if found
     */
    public Optional<TaskStatus> getCachedTaskStatus(String taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        
        try {
            String key = TASK_STATUS_CACHE_PREFIX + taskId;
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                TaskStatus status = TaskStatus.valueOf(cached.toString());
                log.debug("Task status found in cache: {} -> {}", taskId, status);
                return Optional.of(status);
            }
            
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error getting cached task status: {}", taskId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Check if task exists in cache
     * 
     * @param taskId task identifier
     * @return true if cached
     */
    public boolean isCached(String taskId) {
        if (taskId == null) {
            return false;
        }
        
        try {
            String key = TASK_CACHE_PREFIX + taskId;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking if task is cached: {}", taskId, e);
            return false;
        }
    }
    
    /**
     * Clear all task caches (use with caution!)
     */
    public void clearAllTaskCaches() {
        try {
            // Delete all keys matching pattern
            redisTemplate.delete(
                    redisTemplate.keys(TASK_CACHE_PREFIX + "*")
            );
            redisTemplate.delete(
                    redisTemplate.keys(TASK_STATUS_CACHE_PREFIX + "*")
            );
            
            log.warn("Cleared all task caches");
        } catch (Exception e) {
            log.error("Error clearing task caches", e);
        }
    }
}
