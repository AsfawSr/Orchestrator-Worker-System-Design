package com.asfaw.Orchestrator_Worker.orchestrator;

import com.asfaw.Orchestrator_Worker.queue.TaskQueue;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Task scheduler for monitoring queue statistics.
 * 
 * NOTE: With RabbitMQ integration, task processing is handled by
 * @RabbitListener methods in TaskMessageListener, not by polling.
 * This class now focuses on monitoring and logging queue statistics.
 * 
 * The polling-based task dispatch logic has been replaced by push-based
 * message consumption from RabbitMQ queues.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScheduler {
    
    private final TaskQueue taskQueue;
    private final TaskManager taskManager;
    
    /**
     * Log queue statistics every 10 seconds for monitoring
     */
    @Scheduled(fixedDelay = 10000)
    public void logQueueStatistics() {
        log.info("Queue Status - COMPUTE: {}, IO: {}, AI: {}, Total: {}", 
                taskQueue.getQueueSize(TaskType.COMPUTE),
                taskQueue.getQueueSize(TaskType.IO),
                taskQueue.getQueueSize(TaskType.AI),
                taskQueue.getTotalQueueSize());
    }
    
    /**
     * Log task statistics every 30 seconds
     */
    @Scheduled(fixedDelay = 30000)
    public void logTaskStatistics() {
        TaskManager.TaskStatistics stats = taskManager.getStatistics();
        
        log.info("Task Statistics - Total: {}, Pending: {}, Running: {}, Completed: {}, Failed: {}, Cancelled: {}",
                stats.total(),
                stats.pending(),
                stats.running(),
                stats.completed(),
                stats.failed(),
                stats.cancelled());
    }
}
