package com.asfaw.Orchestrator_Worker.queue;

import com.asfaw.Orchestrator_Worker.config.RabbitMQConfig;
import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * RabbitMQ implementation of TaskQueue for distributed task queuing.
 * Uses RabbitMQ for reliable, persistent message delivery across multiple application instances.
 * 
 * Benefits over in-memory queue:
 * - Survives application restarts
 * - Supports horizontal scaling (multiple consumers)
 * - Built-in message persistence and durability
 * - Dead letter queue for failed messages
 * - Better visibility and monitoring through RabbitMQ Management UI
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class RabbitMQTaskQueue implements TaskQueue {
    
    private final AmqpTemplate amqpTemplate;
    private final RabbitAdmin rabbitAdmin;
    
    @Override
    public boolean enqueue(Task task) {
        if (task == null || task.getTaskType() == null) {
            log.warn("Attempted to enqueue null task or task with null type");
            return false;
        }
        
        try {
            // Get routing key based on task type
            String routingKey = RabbitMQConfig.getRoutingKey(task.getTaskType());
            
            // Send task to exchange with routing key
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.TASK_EXCHANGE,
                    routingKey,
                    task
            );
            
            log.debug("Task {} enqueued to RabbitMQ with routing key {}", 
                    task.getTaskId(), routingKey);
            
            return true;
            
        } catch (AmqpException e) {
            log.error("Failed to enqueue task {} to RabbitMQ: {}", 
                    task.getTaskId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<Task> dequeue(TaskType taskType) {
        // Note: This method is not actively used in RabbitMQ implementation
        // Workers consume messages directly via @RabbitListener annotations
        // This is kept for interface compatibility
        
        if (taskType == null) {
            log.warn("Attempted to dequeue with null task type");
            return Optional.empty();
        }
        
        try {
            String queueName = RabbitMQConfig.getQueueName(taskType);
            
            // Receive and convert message (non-blocking)
            Object message = amqpTemplate.receiveAndConvert(queueName, 0);
            
            if (message instanceof Task task) {
                log.debug("Task {} dequeued from RabbitMQ queue {}", 
                        task.getTaskId(), queueName);
                return Optional.of(task);
            }
            
            return Optional.empty();
            
        } catch (AmqpException e) {
            log.error("Failed to dequeue from RabbitMQ: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public int getQueueSize(TaskType taskType) {
        if (taskType == null) {
            return 0;
        }
        
        try {
            String queueName = RabbitMQConfig.getQueueName(taskType);
            QueueInformation queueInfo = rabbitAdmin.getQueueInfo(queueName);
            
            if (queueInfo != null) {
                // getMessageCount() returns long, cast to int
                return (int) queueInfo.getMessageCount();
            }
            
            return 0;
            
        } catch (Exception e) {
            log.error("Failed to get queue size for {}: {}", taskType, e.getMessage());
            return 0;
        }
    }
    
    @Override
    public int getTotalQueueSize() {
        int total = 0;
        
        for (TaskType taskType : TaskType.values()) {
            total += getQueueSize(taskType);
        }
        
        return total;
    }
    
    @Override
    public boolean isEmpty(TaskType taskType) {
        return getQueueSize(taskType) == 0;
    }
    
    @Override
    public void clear() {
        log.warn("Clearing all RabbitMQ task queues - this will purge all pending messages!");
        
        for (TaskType taskType : TaskType.values()) {
            try {
                String queueName = RabbitMQConfig.getQueueName(taskType);
                rabbitAdmin.purgeQueue(queueName);
                log.info("Purged queue: {}", queueName);
            } catch (Exception e) {
                log.error("Failed to purge queue for {}: {}", taskType, e.getMessage());
            }
        }
    }
}
