package com.asfaw.Orchestrator_Worker.repository;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskStatus;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Repository for Task entity.
 * Provides database operations for task persistence and querying.
 * 
 * @author TaskForge Team
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    
    /**
     * Find all tasks by status
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * Find all tasks by task type
     */
    List<Task> findByTaskType(TaskType taskType);
    
    /**
     * Find all tasks by status and task type
     */
    List<Task> findByStatusAndTaskType(TaskStatus status, TaskType taskType);
    
    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Find tasks that are stuck in RUNNING status (potential failures)
     * These are tasks that have been running for more than specified minutes
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING' AND t.startedAt < :cutoffTime")
    List<Task> findStuckTasks(LocalDateTime cutoffTime);
    
    /**
     * Find tasks ready for retry
     * These are failed tasks that still have retry attempts left
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'FAILED' AND t.retryCount < t.maxRetries")
    List<Task> findTasksReadyForRetry();
    
    /**
     * Find recent tasks (last N days)
     */
    @Query("SELECT t FROM Task t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Task> findRecentTasks(LocalDateTime since);
    
    /**
     * Get task statistics
     */
    @Query("SELECT t.status as status, COUNT(t) as count FROM Task t GROUP BY t.status")
    List<TaskStatusCount> getTaskStatistics();
    
    /**
     * Interface for task statistics projection
     */
    interface TaskStatusCount {
        TaskStatus getStatus();
        Long getCount();
    }
}
