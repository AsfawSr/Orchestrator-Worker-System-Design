package com.asfaw.Orchestrator_Worker.controller.dto;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for task response data.
 * Provides task status and result information to clients.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {
    
    /**
     * Unique task identifier
     */
    private String taskId;
    
    /**
     * Type of task
     */
    private TaskType taskType;
    
    /**
     * Current task status
     */
    private TaskStatus status;
    
    /**
     * Task priority
     */
    private int priority;
    
    /**
     * Number of retry attempts made
     */
    private int retryCount;
    
    /**
     * Maximum retries allowed
     */
    private int maxRetries;
    
    /**
     * Worker ID that processed the task
     */
    private String workerId;
    
    /**
     * Task creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Task start timestamp
     */
    private LocalDateTime startedAt;
    
    /**
     * Task completion timestamp
     */
    private LocalDateTime completedAt;
    
    /**
     * Task input payload
     */
    private Map<String, Object> payload;
    
    /**
     * Task result (only included when completed)
     */
    private TaskResult result;
    
    /**
     * Error message (only included when failed)
     */
    private String errorMessage;
    
    /**
     * Convert Task entity to TaskResponse DTO
     */
    public static TaskResponse fromTask(Task task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .priority(task.getPriority())
                .retryCount(task.getRetryCount())
                .maxRetries(task.getMaxRetries())
                .workerId(task.getWorkerId())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .payload(task.getPayload())
                .result(task.getResult())
                .errorMessage(task.getErrorMessage())
                .build();
    }
}
