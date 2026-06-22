package com.asfaw.Orchestrator_Worker.controller.dto;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for task submission requests.
 * Validates incoming task data before processing.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    /**
     * Type of task (COMPUTE, IO, or AI)
     */
    @NotNull(message = "Task type is required")
    private TaskType taskType;
    
    /**
     * Task payload containing operation parameters
     */
    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;
    
    /**
     * Task priority (1-10, higher = more important)
     */
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority must be at most 10")
    @Builder.Default
    private int priority = 5;
    
    /**
     * Maximum number of retry attempts
     */
    @Min(value = 0, message = "Max retries cannot be negative")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    @Builder.Default
    private int maxRetries = 3;
}
