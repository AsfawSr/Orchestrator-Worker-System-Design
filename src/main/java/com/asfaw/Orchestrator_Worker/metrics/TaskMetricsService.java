package com.asfaw.Orchestrator_Worker.metrics;

import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Custom Metrics Service for Prometheus.
 * 
 * Tracks:
 * - Task submission rate
 * - Task completion rate by status
 * - Task processing duration
 * - Queue sizes
 * - Error rates
 * 
 * Metrics exposed at: /actuator/prometheus
 * 
 * @author TaskForge Team
 */
@Slf4j
@Service
public class TaskMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Counters
    private final Counter tasksSubmittedCounter;
    private final Counter tasksCompletedCounter;
    private final Counter tasksFailedCounter;
    
    // Timers
    private final Timer taskProcessingTimer;
    
    public TaskMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.tasksSubmittedCounter = Counter.builder("tasks.submitted.total")
                .description("Total number of tasks submitted")
                .register(meterRegistry);
        
        this.tasksCompletedCounter = Counter.builder("tasks.completed.total")
                .description("Total number of tasks completed successfully")
                .register(meterRegistry);
        
        this.tasksFailedCounter = Counter.builder("tasks.failed.total")
                .description("Total number of tasks failed")
                .register(meterRegistry);
        
        // Initialize timers
        this.taskProcessingTimer = Timer.builder("task.processing.duration")
                .description("Time taken to process a task")
                .register(meterRegistry);
        
        log.info("TaskMetricsService initialized with custom metrics");
    }
    
    /**
     * Record task submission
     */
    public void recordTaskSubmitted(TaskType taskType) {
        tasksSubmittedCounter.increment();
        
        // Also record by task type
        Counter.builder("tasks.submitted.by.type")
                .tag("type", taskType.name())
                .description("Tasks submitted by type")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record task completion
     */
    public void recordTaskCompleted(TaskType taskType, TaskStatus status, long durationMs) {
        if (status == TaskStatus.COMPLETED) {
            tasksCompletedCounter.increment();
        } else if (status == TaskStatus.FAILED) {
            tasksFailedCounter.increment();
        }
        
        // Record by type and status
        Counter.builder("tasks.processed.by.type.and.status")
                .tag("type", taskType.name())
                .tag("status", status.name())
                .description("Tasks processed by type and status")
                .register(meterRegistry)
                .increment();
        
        // Record processing duration
        taskProcessingTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        // Also record duration by task type
        Timer.builder("task.processing.duration.by.type")
                .tag("type", taskType.name())
                .description("Processing duration by task type")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Record queue size (gauge)
     */
    public void recordQueueSize(TaskType taskType, int size) {
        meterRegistry.gauge("queue.size",
                java.util.List.of(
                        io.micrometer.core.instrument.Tag.of("type", taskType.name())
                ),
                size);
    }
    
    /**
     * Record thread pool metrics
     */
    public void recordThreadPoolMetrics(String poolName, int activeThreads, int queuedTasks) {
        meterRegistry.gauge("thread.pool.active",
                java.util.List.of(
                        io.micrometer.core.instrument.Tag.of("pool", poolName)
                ),
                activeThreads);
        
        meterRegistry.gauge("thread.pool.queued",
                java.util.List.of(
                        io.micrometer.core.instrument.Tag.of("pool", poolName)
                ),
                queuedTasks);
    }
    
    /**
     * Record cache hit/miss
     */
    public void recordCacheHit(boolean hit) {
        Counter.builder("cache.requests")
                .tag("result", hit ? "hit" : "miss")
                .description("Cache hit/miss rate")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record rate limit violations
     */
    public void recordRateLimitViolation(String clientIp) {
        Counter.builder("rate.limit.violations")
                .tag("client", clientIp)
                .description("Rate limit violations by client")
                .register(meterRegistry)
                .increment();
    }
    
    /**
     * Record distributed lock acquisition
     */
    public void recordLockAcquisition(boolean acquired) {
        Counter.builder("distributed.lock.attempts")
                .tag("result", acquired ? "acquired" : "failed")
                .description("Distributed lock acquisition attempts")
                .register(meterRegistry)
                .increment();
    }
}
