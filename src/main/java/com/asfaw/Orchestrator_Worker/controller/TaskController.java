package com.asfaw.Orchestrator_Worker.controller;

import com.asfaw.Orchestrator_Worker.controller.dto.TaskRequest;
import com.asfaw.Orchestrator_Worker.controller.dto.TaskResponse;
import com.asfaw.Orchestrator_Worker.metrics.TaskMetricsService;
import com.asfaw.Orchestrator_Worker.orchestrator.OrchestratorService;
import com.asfaw.Orchestrator_Worker.orchestrator.TaskManager;
import com.asfaw.Orchestrator_Worker.service.RateLimitService;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST API controller for task management.
 * Now with rate limiting to protect against abuse.
 * 
 * @author TaskForge Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    
    private final OrchestratorService orchestratorService;
    private final RateLimitService rateLimitService;
    private final TaskMetricsService metricsService;
    
    /**
     * Submit a new task for processing
     * Rate limited: 100 requests per minute per IP
     * 
     * POST /api/tasks
     * 
     * @param request task submission request
     * @return submitted task details
     */
    @PostMapping
    public ResponseEntity<?> submitTask(
            @Valid @RequestBody TaskRequest request,
            HttpServletRequest httpRequest
    ) {
        // Rate limiting: 100 requests per minute per IP
        String clientIp = getClientIp(httpRequest);
        RateLimitService.RateLimitInfo rateLimitInfo = 
                rateLimitService.checkLimit("ip:" + clientIp, 100, 60);
        
        if (!rateLimitInfo.allowed()) {
            log.warn("Rate limit exceeded for IP: {} ({}/{})", 
                    clientIp, rateLimitInfo.currentCount(), rateLimitInfo.limit());
            
            // Record metrics
            metricsService.recordRateLimitViolation(clientIp);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Rate limit exceeded");
            error.put("message", "Too many requests. Please try again later.");
            error.put("retryAfter", rateLimitInfo.resetInSeconds());
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
        }
        
        log.info("Received task submission request: type={}, operation={} (IP: {}, {}/{})", 
                request.getTaskType(), request.getPayload().get("operation"),
                clientIp, rateLimitInfo.currentCount(), rateLimitInfo.limit());
        
        try {
            // Convert request to Task entity
            Task task = Task.builder()
                    .taskType(request.getTaskType())
                    .payload(request.getPayload())
                    .priority(request.getPriority())
                    .maxRetries(request.getMaxRetries())
                    .build();
            
            // Submit task
            Task submittedTask = orchestratorService.submitTask(task);
            
            // Convert to response
            TaskResponse response = TaskResponse.fromTask(submittedTask);
            
            log.info("Task {} submitted successfully", submittedTask.getTaskId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid task submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Failed to submit task: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get task status and details by ID
     * 
     * GET /api/tasks/{taskId}
     * 
     * @param taskId the task identifier
     * @return task details if found
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTaskStatus(@PathVariable String taskId) {
        log.debug("Fetching task status for taskId={}", taskId);
        
        Optional<Task> taskOpt = orchestratorService.getTaskStatus(taskId);
        
        if (taskOpt.isEmpty()) {
            log.warn("Task not found: {}", taskId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        TaskResponse response = TaskResponse.fromTask(taskOpt.get());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all tasks
     * 
     * GET /api/tasks
     * 
     * @return list of all tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.debug("Fetching all tasks");
        
        Map<String, Task> tasks = orchestratorService.getAllTasks();
        List<TaskResponse> responses = tasks.values().stream()
                .map(TaskResponse::fromTask)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Get tasks by status
     * 
     * GET /api/tasks/status/{status}
     * 
     * @param status task status to filter by
     * @return list of tasks with specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.debug("Fetching tasks with status={}", status);
        
        Map<String, Task> tasks = orchestratorService.getTasksByStatus(status);
        List<TaskResponse> responses = tasks.values().stream()
                .map(TaskResponse::fromTask)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Cancel a pending task
     * 
     * DELETE /api/tasks/{taskId}
     * 
     * @param taskId the task identifier
     * @return success or error response
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Map<String, String>> cancelTask(@PathVariable String taskId) {
        log.info("Cancelling task: {}", taskId);
        
        boolean cancelled = orchestratorService.cancelTask(taskId);
        
        Map<String, String> response = new HashMap<>();
        if (cancelled) {
            response.put("message", "Task cancelled successfully");
            response.put("taskId", taskId);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Task cannot be cancelled (not found or not pending)");
            response.put("taskId", taskId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Get system statistics
     * 
     * GET /api/tasks/stats
     * 
     * @return system statistics including queue sizes and task counts
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.debug("Fetching system statistics");
        
        TaskManager.TaskStatistics stats = orchestratorService.getStatistics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("tasks", Map.of(
                "total", stats.total(),
                "pending", stats.pending(),
                "running", stats.running(),
                "completed", stats.completed(),
                "failed", stats.failed(),
                "cancelled", stats.cancelled()
        ));
        
        response.put("queue", Map.of(
                "compute", orchestratorService.getQueueSize(com.asfaw.Orchestrator_Worker.task.TaskType.COMPUTE),
                "io", orchestratorService.getQueueSize(com.asfaw.Orchestrator_Worker.task.TaskType.IO),
                "ai", orchestratorService.getQueueSize(com.asfaw.Orchestrator_Worker.task.TaskType.AI),
                "total", orchestratorService.getTotalQueueSize()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * 
     * GET /api/tasks/health
     * 
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Orchestrator-Worker");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Extract client IP address from request
     * Handles X-Forwarded-For header for proxy/load balancer scenarios
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
