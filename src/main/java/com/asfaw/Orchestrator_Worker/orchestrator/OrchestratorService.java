package com.asfaw.Orchestrator_Worker.orchestrator;

import com.asfaw.Orchestrator_Worker.queue.TaskQueue;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Main orchestrator service - entry point for task submission and management.
 * Coordinates between the task queue, task manager, and scheduler.
 * 
 * Responsibilities:
 * - Accept new task submissions
 * - Enqueue tasks for processing
 * - Provide task status and results
 * - Manage task lifecycle
 * 
 * @author TaskForge Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorService {
    
    private final TaskQueue taskQueue;
    private final TaskManager taskManager;
    
    /**
     * Submit a new task for processing.
     * 
     * @param task the task to submit
     * @return the submitted task with assigned ID
     */
    public Task submitTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        if (task.getTaskType() == null) {
            throw new IllegalArgumentException("Task type must be specified");
        }
        
        // Ensure task has an ID
        if (task.getTaskId() == null) {
            task = Task.builder()
                    .taskType(task.getTaskType())
                    .payload(task.getPayload())
                    .priority(task.getPriority())
                    .maxRetries(task.getMaxRetries())
                    .build();
        }
        
        // Set initial status
        task.setStatus(TaskStatus.PENDING);
        
        // Save to task manager
        taskManager.saveTask(task);
        
        // Enqueue for processing
        boolean enqueued = taskQueue.enqueue(task);
        
        if (!enqueued) {
            log.error("Failed to enqueue task {}", task.getTaskId());
            task.markFailed("Failed to enqueue task");
            taskManager.updateTask(task);
            throw new RuntimeException("Failed to enqueue task");
        }
        
        log.info("Task {} submitted successfully. Type: {}, Priority: {}", 
                task.getTaskId(), task.getTaskType(), task.getPriority());
        
        return task;
    }
    
    /**
     * Get task status and details by ID
     * 
     * @param taskId the task identifier
     * @return Optional containing the task if found
     */
    public Optional<Task> getTaskStatus(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            log.warn("Attempted to get task with null or blank ID");
            return Optional.empty();
        }
        
        return taskManager.getTask(taskId);
    }
    
    /**
     * Get all tasks
     * 
     * @return map of all tasks
     */
    public Map<String, Task> getAllTasks() {
        return taskManager.getAllTasks();
    }
    
    /**
     * Get tasks by status
     * 
     * @param status the status to filter by
     * @return map of tasks with the specified status
     */
    public Map<String, Task> getTasksByStatus(TaskStatus status) {
        return taskManager.getTasksByStatus(status);
    }
    
    /**
     * Cancel a pending task
     * 
     * @param taskId the task identifier
     * @return true if task was cancelled
     */
    public boolean cancelTask(String taskId) {
        Optional<Task> taskOpt = taskManager.getTask(taskId);
        
        if (taskOpt.isEmpty()) {
            log.warn("Cannot cancel task {} - task not found", taskId);
            return false;
        }
        
        Task task = taskOpt.get();
        
        // Can only cancel pending tasks
        if (task.getStatus() != TaskStatus.PENDING) {
            log.warn("Cannot cancel task {} - status is {}", taskId, task.getStatus());
            return false;
        }
        
        task.setStatus(TaskStatus.CANCELLED);
        taskManager.updateTask(task);
        
        log.info("Task {} cancelled successfully", taskId);
        return true;
    }
    
    /**
     * Get system statistics
     * 
     * @return task statistics
     */
    public TaskManager.TaskStatistics getStatistics() {
        return taskManager.getStatistics();
    }
    
    /**
     * Get queue size for a specific task type
     * 
     * @return current queue size
     */
    public int getQueueSize(com.asfaw.Orchestrator_Worker.task.TaskType taskType) {
        return taskQueue.getQueueSize(taskType);
    }
    
    /**
     * Get total queue size across all task types
     * 
     * @return total pending tasks in queue
     */
    public int getTotalQueueSize() {
        return taskQueue.getTotalQueueSize();
    }
}
