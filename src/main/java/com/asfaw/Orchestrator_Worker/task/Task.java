package com.asfaw.Orchestrator_Worker.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core Task entity representing a unit of work to be processed by the system.
 * Contains all metadata required for task lifecycle management, execution, and tracking.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    /**
     * Unique identifier for the task
     */
    @Builder.Default
    private String taskId = UUID.randomUUID().toString();
    
    /**
     * Type of task determining which worker will process it
     */
    private TaskType taskType;
    
    /**
     * Current status of the task
     */
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    
    /**
     * Input parameters/payload for task processing
     */
    private Map<String, Object> payload;
    
    /**
     * Result of task execution (populated upon completion)
     */
    private TaskResult result;
    
    /**
     * Timestamp when task was created
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Timestamp when task processing started
     */
    private LocalDateTime startedAt;
    
    /**
     * Timestamp when task completed (success or failure)
     */
    private LocalDateTime completedAt;
    
    /**
     * Number of retry attempts made for this task
     */
    @Builder.Default
    private int retryCount = 0;
    
    /**
     * Maximum number of retries allowed for this task
     */
    @Builder.Default
    private int maxRetries = 3;
    
    /**
     * Error message if task failed
     */
    private String errorMessage;
    
    /**
     * Priority level (higher number = higher priority)
     */
    @Builder.Default
    private int priority = 5;
    
    /**
     * Worker ID that processed or is processing this task
     */
    private String workerId;
    
    /**
     * Check if task can be retried
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }
    
    /**
     * Increment retry count
     */
    public void incrementRetry() {
        this.retryCount++;
    }
    
    /**
     * Mark task as started
     */
    public void markStarted(String workerId) {
        this.status = TaskStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        this.workerId = workerId;
    }
    
    /**
     * Mark task as completed with result
     */
    public void markCompleted(TaskResult result) {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.result = result;
    }
    
    /**
     * Mark task as failed
     */
    public void markFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }
}
