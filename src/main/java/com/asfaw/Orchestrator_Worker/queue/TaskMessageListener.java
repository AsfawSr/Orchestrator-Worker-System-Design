package com.asfaw.Orchestrator_Worker.queue;

import com.asfaw.Orchestrator_Worker.config.RabbitMQConfig;
import com.asfaw.Orchestrator_Worker.orchestrator.TaskManager;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import com.asfaw.Orchestrator_Worker.worker.AiWorker;
import com.asfaw.Orchestrator_Worker.worker.BaseWorker;
import com.asfaw.Orchestrator_Worker.worker.ComputeWorker;
import com.asfaw.Orchestrator_Worker.worker.IoWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message listener for consuming and processing tasks.
 * 
 * Each task type has a dedicated listener method that:
 * 1. Receives task message from RabbitMQ queue
 * 2. Executes task using appropriate worker
 * 3. Updates task status in database
 * 4. Handles retries by re-queuing failed tasks
 * 
 * Benefits:
 * - Automatic message acknowledgment
 * - Concurrent processing via thread pools
 * - Built-in error handling and retry logic
 * - Clean separation per task type
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskMessageListener {
    
    private final TaskManager taskManager;
    private final RabbitTemplate rabbitTemplate;
    
    private final ComputeWorker computeWorker;
    private final IoWorker ioWorker;
    private final AiWorker aiWorker;
    
    /**
     * Listen to COMPUTE queue
     * Concurrency: Controlled by thread pool (computeTaskExecutor)
     */
    @RabbitListener(
            queues = RabbitMQConfig.COMPUTE_QUEUE,
            containerFactory = "computeListenerContainerFactory"
    )
    public void handleComputeTask(Task task) {
        log.debug("Received COMPUTE task {} from queue", task.getTaskId());
        processTask(task, computeWorker, TaskType.COMPUTE);
    }
    
    /**
     * Listen to IO queue
     * Concurrency: Controlled by thread pool (ioTaskExecutor)
     */
    @RabbitListener(
            queues = RabbitMQConfig.IO_QUEUE,
            containerFactory = "ioListenerContainerFactory"
    )
    public void handleIoTask(Task task) {
        log.debug("Received IO task {} from queue", task.getTaskId());
        processTask(task, ioWorker, TaskType.IO);
    }
    
    /**
     * Listen to AI queue
     * Concurrency: Controlled by thread pool (aiTaskExecutor)
     */
    @RabbitListener(
            queues = RabbitMQConfig.AI_QUEUE,
            containerFactory = "aiListenerContainerFactory"
    )
    public void handleAiTask(Task task) {
        log.debug("Received AI task {} from queue", task.getTaskId());
        processTask(task, aiWorker, TaskType.AI);
    }
    
    /**
     * Common task processing logic
     */
    private void processTask(Task task, BaseWorker worker, TaskType taskType) {
        try {
            // Fetch latest task state from database
            Task latestTask = taskManager.getTask(task.getTaskId())
                    .orElse(task);
            
            // Check if task was cancelled
            if (latestTask.getStatus() == com.asfaw.Orchestrator_Worker.task.TaskStatus.CANCELLED) {
                log.info("Task {} was cancelled, skipping execution", task.getTaskId());
                return;
            }
            
            // Execute task
            TaskResult result = worker.execute(latestTask);
            
            // Update task in database
            taskManager.updateTask(latestTask);
            
            // Handle failure with retry logic
            if (!result.isSuccess() && latestTask.canRetry()) {
                handleTaskRetry(latestTask, taskType);
            } else if (!result.isSuccess()) {
                log.error("Task {} failed after all retries", latestTask.getTaskId());
            } else {
                log.info("Task {} completed successfully", latestTask.getTaskId());
            }
            
        } catch (Exception e) {
            log.error("Exception while processing task {}: {}", task.getTaskId(), e.getMessage(), e);
            handleTaskFailure(task, "Execution exception: " + e.getMessage());
        }
    }
    
    /**
     * Handle task retry logic
     */
    private void handleTaskRetry(Task task, TaskType taskType) {
        task.incrementRetry();
        log.warn("Task {} failed. Retrying... (Attempt {}/{})", 
                task.getTaskId(), task.getRetryCount(), task.getMaxRetries());
        
        // Update task in database
        taskManager.updateTask(task);
        
        // Re-enqueue task to RabbitMQ for retry
        try {
            String routingKey = RabbitMQConfig.getRoutingKey(taskType);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    routingKey,
                    task
            );
            log.info("Task {} re-queued to RabbitMQ for retry", task.getTaskId());
        } catch (Exception e) {
            log.error("Failed to re-queue task {} for retry: {}", task.getTaskId(), e.getMessage());
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
}
