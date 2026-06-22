package com.asfaw.Orchestrator_Worker.worker;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base class for all worker implementations.
 * Provides common functionality for task processing, error handling, and metrics.
 * 
 * Workers extend this class and implement the processTask method with their specific logic.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Getter
public abstract class BaseWorker implements Runnable {
    
    /**
     * Unique identifier for this worker instance
     */
    private final String workerId;
    
    /**
     * Type of tasks this worker can process
     */
    private final TaskType taskType;
    
    /**
     * Counter for total tasks processed by this worker
     */
    private final AtomicInteger tasksProcessed = new AtomicInteger(0);
    
    /**
     * Counter for successful task executions
     */
    private final AtomicInteger tasksSucceeded = new AtomicInteger(0);
    
    /**
     * Counter for failed task executions
     */
    private final AtomicInteger tasksFailed = new AtomicInteger(0);
    
    /**
     * Flag indicating if worker is currently processing a task
     */
    private final AtomicBoolean isBusy = new AtomicBoolean(false);
    
    /**
     * Flag to control worker lifecycle
     */
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    /**
     * Constructor initializing worker with specific task type
     * 
     * @param taskType the type of tasks this worker handles
     */
    protected BaseWorker(TaskType taskType) {
        this.workerId = generateWorkerId(taskType);
        this.taskType = taskType;
        log.info("Worker {} created for task type {}", workerId, taskType);
    }
    
    /**
     * Generate a unique worker ID
     */
    private String generateWorkerId(TaskType taskType) {
        return taskType.name() + "-WORKER-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Abstract method to be implemented by specific worker types.
     * Contains the actual task processing logic.
     * 
     * @param task the task to process
     * @return TaskResult containing execution outcome
     * @throws Exception if task processing fails
     */
    protected abstract TaskResult processTask(Task task) throws Exception;
    
    /**
     * Execute the task with error handling and metrics tracking
     * 
     * @param task the task to execute
     * @return TaskResult containing execution outcome
     */
    public TaskResult execute(Task task) {
        if (task == null) {
            log.error("Worker {} received null task", workerId);
            return TaskResult.failure("Null task received", 0);
        }
        
        isBusy.set(true);
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Worker {} started processing task {}", workerId, task.getTaskId());
            task.markStarted(workerId);
            
            // Execute task-specific processing logic
            TaskResult result = processTask(task);
            
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);
            
            // Update task with result
            task.markCompleted(result);
            
            // Update metrics
            tasksProcessed.incrementAndGet();
            if (result.isSuccess()) {
                tasksSucceeded.incrementAndGet();
                log.info("Worker {} successfully completed task {} in {}ms", 
                        workerId, task.getTaskId(), executionTime);
            } else {
                tasksFailed.incrementAndGet();
                log.error("Worker {} failed to process task {}: {}", 
                        workerId, task.getTaskId(), result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Worker {} encountered exception processing task {}: {}", 
                    workerId, task.getTaskId(), e.getMessage(), e);
            
            tasksProcessed.incrementAndGet();
            tasksFailed.incrementAndGet();
            
            TaskResult failureResult = TaskResult.failure(
                    "Exception: " + e.getMessage(), 
                    executionTime
            );
            
            task.markFailed(failureResult.getErrorMessage());
            
            return failureResult;
            
        } finally {
            isBusy.set(false);
        }
    }
    
    /**
     * Check if worker is currently available for new tasks
     */
    public boolean isAvailable() {
        return running.get() && !isBusy.get();
    }
    
    /**
     * Shutdown this worker gracefully
     */
    public void shutdown() {
        log.info("Worker {} shutting down. Stats - Total: {}, Success: {}, Failed: {}", 
                workerId, tasksProcessed.get(), tasksSucceeded.get(), tasksFailed.get());
        running.set(false);
    }
    
    /**
     * Get worker statistics
     */
    public WorkerStats getStats() {
        return new WorkerStats(
                workerId,
                taskType,
                tasksProcessed.get(),
                tasksSucceeded.get(),
                tasksFailed.get(),
                isBusy.get()
        );
    }
    
    @Override
    public void run() {
        log.info("Worker {} thread started", workerId);
        // Worker thread lifecycle managed by ThreadPoolExecutor
        // Actual task execution happens via execute() method
    }
    
    /**
     * Inner class for worker statistics
     */
    @Getter
    public static class WorkerStats {
        private final String workerId;
        private final TaskType taskType;
        private final int totalProcessed;
        private final int totalSucceeded;
        private final int totalFailed;
        private final boolean busy;
        
        public WorkerStats(String workerId, TaskType taskType, int totalProcessed, 
                          int totalSucceeded, int totalFailed, boolean busy) {
            this.workerId = workerId;
            this.taskType = taskType;
            this.totalProcessed = totalProcessed;
            this.totalSucceeded = totalSucceeded;
            this.totalFailed = totalFailed;
            this.busy = busy;
        }
    }
}
