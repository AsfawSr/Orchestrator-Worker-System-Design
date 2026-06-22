package com.asfaw.Orchestrator_Worker.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the result of a task execution.
 * Contains output data, execution metrics, and metadata.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResult {
    
    /**
     * Whether the task executed successfully
     */
    private boolean success;
    
    /**
     * Output data produced by the task
     */
    private Map<String, Object> data;
    
    /**
     * Error message if task failed
     */
    private String errorMessage;
    
    /**
     * Time taken to execute the task (in milliseconds)
     */
    private long executionTimeMs;
    
    /**
     * Timestamp when result was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Additional metadata about the execution
     */
    private Map<String, String> metadata;
    
    /**
     * Create a successful result
     */
    public static TaskResult success(Map<String, Object> data, long executionTimeMs) {
        return TaskResult.builder()
                .success(true)
                .data(data)
                .executionTimeMs(executionTimeMs)
                .build();
    }
    
    /**
     * Create a failed result
     */
    public static TaskResult failure(String errorMessage, long executionTimeMs) {
        return TaskResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .executionTimeMs(executionTimeMs)
                .build();
    }
}
