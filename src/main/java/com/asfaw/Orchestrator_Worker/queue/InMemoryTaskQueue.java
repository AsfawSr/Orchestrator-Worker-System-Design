package com.asfaw.Orchestrator_Worker.queue;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * In-memory implementation of TaskQueue using concurrent data structures.
 * Uses priority-based queues to process high-priority tasks first.
 * Thread-safe and suitable for single-instance deployments.
 * 
 * For distributed systems, consider using RabbitMQ, Redis, or Kafka.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class InMemoryTaskQueue implements TaskQueue {
    
    // Separate queue for each task type to enable specialized worker pools
    private final Map<TaskType, PriorityBlockingQueue<Task>> queues;
    
    public InMemoryTaskQueue() {
        this.queues = new ConcurrentHashMap<>();
        
        // Initialize a queue for each task type
        for (TaskType taskType : TaskType.values()) {
            // Priority queue: higher priority value = processed first
            queues.put(taskType, new PriorityBlockingQueue<>(
                    100, 
                    (t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority())
            ));
        }
        
        log.info("InMemoryTaskQueue initialized with {} queues", TaskType.values().length);
    }
    
    @Override
    public boolean enqueue(Task task) {
        if (task == null || task.getTaskType() == null) {
            log.warn("Attempted to enqueue null task or task with null type");
            return false;
        }
        
        PriorityBlockingQueue<Task> queue = queues.get(task.getTaskType());
        boolean success = queue.offer(task);
        
        if (success) {
            log.debug("Task {} enqueued to {} queue. Queue size: {}", 
                    task.getTaskId(), task.getTaskType(), queue.size());
        } else {
            log.error("Failed to enqueue task {} to {} queue", 
                    task.getTaskId(), task.getTaskType());
        }
        
        return success;
    }
    
    @Override
    public Optional<Task> dequeue(TaskType taskType) {
        if (taskType == null) {
            log.warn("Attempted to dequeue with null task type");
            return Optional.empty();
        }
        
        PriorityBlockingQueue<Task> queue = queues.get(taskType);
        Task task = queue.poll();
        
        if (task != null) {
            log.debug("Task {} dequeued from {} queue. Remaining: {}", 
                    task.getTaskId(), taskType, queue.size());
        }
        
        return Optional.ofNullable(task);
    }
    
    @Override
    public int getQueueSize(TaskType taskType) {
        if (taskType == null) {
            return 0;
        }
        return queues.get(taskType).size();
    }
    
    @Override
    public int getTotalQueueSize() {
        return queues.values().stream()
                .mapToInt(PriorityBlockingQueue::size)
                .sum();
    }
    
    @Override
    public boolean isEmpty(TaskType taskType) {
        if (taskType == null) {
            return true;
        }
        return queues.get(taskType).isEmpty();
    }
    
    @Override
    public void clear() {
        log.warn("Clearing all task queues");
        queues.values().forEach(PriorityBlockingQueue::clear);
    }
}
