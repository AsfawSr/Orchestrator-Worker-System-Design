package com.asfaw.Orchestrator_Worker.orchestrator;

import com.asfaw.Orchestrator_Worker.queue.TaskQueue;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import com.asfaw.Orchestrator_Worker.worker.AiWorker;
import com.asfaw.Orchestrator_Worker.worker.BaseWorker;
import com.asfaw.Orchestrator_Worker.worker.ComputeWorker;
import com.asfaw.Orchestrator_Worker.worker.IoWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Responsible for continuously polling the task queue and dispatching tasks to workers.
 * Runs as a scheduled background process that:
 * 1. Checks queue for pending tasks
 * 2. Assigns tasks to available workers based on task type
 * 3. Handles task retries on failure
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class TaskScheduler {
    
    private final TaskQueue taskQueue;
    private final TaskManager taskManager;
    
    private final ThreadPoolTaskExecutor computeTaskExecutor;
    private final ThreadPoolTaskExecutor ioTaskExecutor;
    private final ThreadPoolTaskExecutor aiTaskExecutor;
    
    private final ComputeWorker computeWorkerPrototype;
    private final IoWorker ioWorkerPrototype;
    private final AiWorker aiWorkerPrototype;
    
    public TaskScheduler(
            TaskQueue taskQueue,
            TaskManager taskManager,
            @Qualifier("computeTaskExecutor") ThreadPoolTaskExecutor computeTaskExecutor,
            @Qualifier("ioTaskExecutor") ThreadPoolTaskExecutor ioTaskExecutor,
            @Qualifier("aiTaskExecutor") ThreadPoolTaskExecutor aiTaskExecutor,
            ComputeWorker computeWorkerPrototype,
            IoWorker ioWorkerPrototype,
            AiWorker aiWorkerPrototype
    ) {
        this.taskQueue = taskQueue;
        this.taskManager = taskManager;
        this.computeTaskExecutor = computeTaskExecutor;
        this.ioTaskExecutor = ioTaskExecutor;
        this.aiTaskExecutor = aiTaskExecutor;
        this.computeWorkerPrototype = computeWorkerPrototype;
        this.ioWorkerPrototype = ioWorkerPrototype;
        this.aiWorkerPrototype = aiWorkerPrototype;
        
        log.info("TaskScheduler initialized and ready to process tasks");
    }
    
    /**
     * Scheduled method that runs every 500ms to process pending tasks.
     * Checks each task type queue and dispatches work to appropriate workers.
     */
    @Scheduled(fixedDelay = 500)
    public void scheduleTasks() {
        // Process each task type
        for (TaskType taskType : TaskType.values()) {
            processPendingTasks(taskType);
        }
    }
    
    /**
     * Process pending tasks for a specific task type
     */
    private void processPendingTasks(TaskType taskType) {
        // Process multiple tasks if queue has them
        int processedCount = 0;
        int maxBatchSize = 10; // Process up to 10 tasks per cycle
        
        while (processedCount < maxBatchSize && !taskQueue.isEmpty(taskType)) {
            Optional<Task> taskOpt = taskQueue.dequeue(taskType);
            
            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                dispatchTaskToWorker(task);
                processedCount++;
            } else {
                break; // Queue is empty
            }
        }
        
        if (processedCount > 0) {
            log.debug("Processed {} {} tasks from queue", processedCount, taskType);
        }
    }
    
    /**
     * Dispatch a task to the appropriate worker pool
     */
    private void dispatchTaskToWorker(Task task) {
        log.debug("Dispatching task {} of type {}", task.getTaskId(), task.getTaskType());
        
        // Get appropriate worker and executor based on task type
        BaseWorker worker = getWorkerForTaskType(task.getTaskType());
        ThreadPoolTaskExecutor executor = getExecutorForTaskType(task.getTaskType());
        
        if (worker == null || executor == null) {
            log.error("No worker or executor found for task type {}", task.getTaskType());
            handleTaskFailure(task, "No worker available for task type");
            return;
        }
        
        // Submit task to thread pool for async execution
        executor.submit(() -> executeTask(task, worker));
    }
    
    /**
     * Execute task and handle result/failure
     */
    private void executeTask(Task task, BaseWorker worker) {
        try {
            // Execute task
            TaskResult result = worker.execute(task);
            
            // Update task in manager
            taskManager.updateTask(task);
            
            // Handle failure with retry logic
            if (!result.isSuccess() && task.canRetry()) {
                handleTaskRetry(task);
            } else if (!result.isSuccess()) {
                log.error("Task {} failed after all retries", task.getTaskId());
            }
            
        } catch (Exception e) {
            log.error("Exception while executing task {}: {}", task.getTaskId(), e.getMessage(), e);
            handleTaskFailure(task, "Execution exception: " + e.getMessage());
        }
    }
    
    /**
     * Handle task retry logic
     */
    private void handleTaskRetry(Task task) {
        task.incrementRetry();
        log.warn("Task {} failed. Retrying... (Attempt {}/{})", 
                task.getTaskId(), task.getRetryCount(), task.getMaxRetries());
        
        // Re-enqueue task for retry
        boolean enqueued = taskQueue.enqueue(task);
        
        if (enqueued) {
            taskManager.updateTask(task);
            log.info("Task {} re-queued for retry", task.getTaskId());
        } else {
            log.error("Failed to re-queue task {} for retry", task.getTaskId());
            handleTaskFailure(task, "Failed to re-queue for retry");
        }
    }
    
    /**
     * Handle task failure
     */
    private void handleTaskFailure(Task task, String errorMessage) {
        task.markFailed(errorMessage);
        taskManager.updateTask(task);
        log.error("Task {} marked as FAILED: {}", task.getTaskId(), errorMessage);
    }
    
    /**
     * Get worker instance for task type
     */
    private BaseWorker getWorkerForTaskType(TaskType taskType) {
        return switch (taskType) {
            case COMPUTE -> computeWorkerPrototype;
            case IO -> ioWorkerPrototype;
            case AI -> aiWorkerPrototype;
        };
    }
    
    /**
     * Get executor for task type
     */
    private ThreadPoolTaskExecutor getExecutorForTaskType(TaskType taskType) {
        return switch (taskType) {
            case COMPUTE -> computeTaskExecutor;
            case IO -> ioTaskExecutor;
            case AI -> aiTaskExecutor;
        };
    }
    
    /**
     * Get queue statistics (for monitoring)
     */
    @Scheduled(fixedDelay = 10000)
    public void logQueueStatistics() {
        log.info("Queue Status - COMPUTE: {}, IO: {}, AI: {}, Total: {}", 
                taskQueue.getQueueSize(TaskType.COMPUTE),
                taskQueue.getQueueSize(TaskType.IO),
                taskQueue.getQueueSize(TaskType.AI),
                taskQueue.getTotalQueueSize());
    }
}
