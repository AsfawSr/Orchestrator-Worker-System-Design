package com.asfaw.Orchestrator_Worker.task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Core Task entity representing a unit of work to be processed by the system.
 * Now persisted to PostgreSQL database for durability and audit trails.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_task_type", columnList = "taskType"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Task {
    
    /**
     * Unique identifier for the task
     */
    @Id
    @Column(name = "task_id", nullable = false, length = 36)
    @Builder.Default
    private String taskId = UUID.randomUUID().toString();
    
    /**
     * Type of task determining which worker will process it
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private TaskType taskType;
    
    /**
     * Current status of the task
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    
    /**
     * Input parameters/payload for task processing
     * Stored as JSON in database
     */
    @Convert(converter = JsonMapConverter.class)
    @Column(name = "payload", columnDefinition = "TEXT")
    private Map<String, Object> payload;
    
    /**
     * Result of task execution (populated upon completion)
     * Stored as JSON in database
     */
    @Convert(converter = TaskResultConverter.class)
    @Column(name = "result", columnDefinition = "TEXT")
    private TaskResult result;
    
    /**
     * Timestamp when task was created
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * Timestamp when task processing started
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    /**
     * Timestamp when task completed (success or failure)
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    /**
     * Number of retry attempts made for this task
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;
    
    /**
     * Maximum number of retries allowed for this task
     */
    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private int maxRetries = 3;
    
    /**
     * Error message if task failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Priority level (higher number = higher priority)
     */
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private int priority = 5;
    
    /**
     * Worker ID that processed or is processing this task
     */
    @Column(name = "worker_id", length = 100)
    private String workerId;
    
    /**
     * Version for optimistic locking
     */
    @Version
    @Column(name = "version")
    private Long version;
    
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
