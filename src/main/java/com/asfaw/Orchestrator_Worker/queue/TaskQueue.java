package com.asfaw.Orchestrator_Worker.queue;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskType;

import java.util.Optional;

/**
 * Interface defining operations for task queue management.
 * Implementations can use in-memory queues, RabbitMQ, Redis, or other messaging systems.
 * 
 * @author TaskForge Team
 */
public interface TaskQueue {
    
    /**
     * Enqueue a task for processing
     * 
     * @param task the task to enqueue
     * @return true if successfully enqueued
     */
    boolean enqueue(Task task);
    
    /**
     * Dequeue the next task for a specific task type
     * 
     * @param taskType the type of task to dequeue
     * @return Optional containing the task if available, empty otherwise
     */
    Optional<Task> dequeue(TaskType taskType);
    
    /**
     * Get the current size of the queue for a specific task type
     * 
     * @param taskType the type of task
     * @return number of pending tasks in queue
     */
    int getQueueSize(TaskType taskType);
    
    /**
     * Get the total number of pending tasks across all types
     * 
     * @return total pending tasks
     */
    int getTotalQueueSize();
    
    /**
     * Check if queue is empty for a specific task type
     * 
     * @param taskType the type of task
     * @return true if queue is empty
     */
    boolean isEmpty(TaskType taskType);
    
    /**
     * Clear all tasks from the queue (use with caution)
     */
    void clear();
}
