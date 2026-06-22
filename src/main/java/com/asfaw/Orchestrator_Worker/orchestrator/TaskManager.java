package com.asfaw.Orchestrator_Worker.orchestrator;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages task lifecycle and state tracking.
 * Provides centralized storage and retrieval of task information.
 * Thread-safe implementation using concurrent data structures.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class TaskManager {
    
    /**
     * In-memory storage for all tasks
     * Key: taskId, Value: Task object
     */
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    
    /**
     * Store a new task
     * 
     * @param task the task to store
     */
    public void saveTask(Task task) {
        if (task == null || task.getTaskId() == null) {
            log.warn("Attempted to save null task or task with null ID");
            return;
        }
        
        tasks.put(task.getTaskId(), task);
        log.debug("Task {} saved with status {}", task.getTaskId(), task.getStatus());
    }
    
    /**
     * Retrieve a task by ID
     * 
     * @param taskId the task identifier
     * @return Optional containing the task if found
     */
    public Optional<Task> getTask(String taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(tasks.get(taskId));
    }
    
    /**
     * Update an existing task
     * 
     * @param task the task with updated information
     */
    public void updateTask(Task task) {
        if (task == null || task.getTaskId() == null) {
            log.warn("Attempted to update null task or task with null ID");
            return;
        }
        
        tasks.put(task.getTaskId(), task);
        log.debug("Task {} updated to status {}", task.getTaskId(), task.getStatus());
    }
    
    /**
     * Get all tasks with a specific status
     * 
     * @param status the status to filter by
     * @return map of tasks with the specified status
     */
    public Map<String, Task> getTasksByStatus(TaskStatus status) {
        Map<String, Task> filteredTasks = new ConcurrentHashMap<>();
        
        tasks.forEach((id, task) -> {
            if (task.getStatus() == status) {
                filteredTasks.put(id, task);
            }
        });
        
        return filteredTasks;
    }
    
    /**
     * Get total count of tasks by status
     * 
     * @param status the status to count
     * @return number of tasks with the specified status
     */
    public long countByStatus(TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }
    
    /**
     * Get total number of tasks
     * 
     * @return total task count
     */
    public int getTotalTaskCount() {
        return tasks.size();
    }
    
    /**
     * Get all tasks
     * 
     * @return map of all tasks
     */
    public Map<String, Task> getAllTasks() {
        return new ConcurrentHashMap<>(tasks);
    }
    
    /**
     * Remove a task (use with caution)
     * 
     * @param taskId the task identifier
     * @return true if task was removed
     */
    public boolean removeTask(String taskId) {
        if (taskId == null) {
            return false;
        }
        
        Task removed = tasks.remove(taskId);
        if (removed != null) {
            log.info("Task {} removed from TaskManager", taskId);
            return true;
        }
        return false;
    }
    
    /**
     * Clear all tasks (use with caution)
     */
    public void clearAllTasks() {
        log.warn("Clearing all tasks from TaskManager");
        tasks.clear();
    }
    
    /**
     * Get task statistics
     */
    public TaskStatistics getStatistics() {
        long pending = countByStatus(TaskStatus.PENDING);
        long running = countByStatus(TaskStatus.RUNNING);
        long completed = countByStatus(TaskStatus.COMPLETED);
        long failed = countByStatus(TaskStatus.FAILED);
        long cancelled = countByStatus(TaskStatus.CANCELLED);
        
        return new TaskStatistics(
                tasks.size(),
                pending,
                running,
                completed,
                failed,
                cancelled
        );
    }
    
    /**
     * Inner class for task statistics
     */
    public record TaskStatistics(
            long total,
            long pending,
            long running,
            long completed,
            long failed,
            long cancelled
    ) {}
}
