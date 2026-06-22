package com.asfaw.Orchestrator_Worker.orchestrator;

import com.asfaw.Orchestrator_Worker.repository.TaskRepository;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages task lifecycle and state tracking.
 * Now backed by PostgreSQL database for persistence and durability.
 * Provides centralized storage and retrieval of task information.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskManager {
    
    private final TaskRepository taskRepository;
    
    /**
     * Store a new task
     * 
     * @param task the task to store
     */
    @Transactional
    public void saveTask(Task task) {
        if (task == null || task.getTaskId() == null) {
            log.warn("Attempted to save null task or task with null ID");
            return;
        }
        
        taskRepository.save(task);
        log.debug("Task {} saved to database with status {}", task.getTaskId(), task.getStatus());
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
        
        return taskRepository.findById(taskId);
    }
    
    /**
     * Update an existing task
     * 
     * @param task the task with updated information
     */
    @Transactional
    public void updateTask(Task task) {
        if (task == null || task.getTaskId() == null) {
            log.warn("Attempted to update null task or task with null ID");
            return;
        }
        
        taskRepository.save(task);
        log.debug("Task {} updated in database to status {}", task.getTaskId(), task.getStatus());
    }
    
    /**
     * Get all tasks with a specific status
     * 
     * @param status the status to filter by
     * @return map of tasks with the specified status
     */
    public Map<String, Task> getTasksByStatus(TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        
        return tasks.stream()
                .collect(Collectors.toMap(
                        Task::getTaskId,
                        task -> task
                ));
    }
    
    /**
     * Get total count of tasks by status
     * 
     * @param status the status to count
     * @return number of tasks with the specified status
     */
    public long countByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }
    
    /**
     * Get total number of tasks
     * 
     * @return total task count
     */
    public long getTotalTaskCount() {
        return taskRepository.count();
    }
    
    /**
     * Get all tasks
     * 
     * @return map of all tasks
     */
    public Map<String, Task> getAllTasks() {
        List<Task> allTasks = taskRepository.findAll();
        
        return allTasks.stream()
                .collect(Collectors.toMap(
                        Task::getTaskId,
                        task -> task
                ));
    }
    
    /**
     * Remove a task (use with caution)
     * 
     * @param taskId the task identifier
     * @return true if task was removed
     */
    @Transactional
    public boolean removeTask(String taskId) {
        if (taskId == null) {
            return false;
        }
        
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            log.info("Task {} removed from database", taskId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear all tasks (use with caution)
     */
    @Transactional
    public void clearAllTasks() {
        log.warn("Clearing all tasks from database");
        taskRepository.deleteAll();
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
                getTotalTaskCount(),
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
