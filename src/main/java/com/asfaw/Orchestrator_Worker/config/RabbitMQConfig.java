package com.asfaw.Orchestrator_Worker.config;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * RabbitMQ Configuration for distributed task queuing.
 * 
 * Architecture:
 * - One exchange: "task.exchange" (Topic Exchange)
 * - One queue per task type: "compute.queue", "io.queue", "ai.queue"
 * - Routing keys: "task.compute", "task.io", "task.ai"
 * 
 * Benefits:
 * - Persistent queues (survive RabbitMQ restart)
 * - Durable messages (survive broker crash)
 * - Automatic message routing by task type
 * - Dead Letter Exchange for failed messages
 * 
 * @author TaskForge Team
 */
@Slf4j
@Configuration
public class RabbitMQConfig {
    
    // Exchange name
    public static final String TASK_EXCHANGE = "task.exchange";
    
    // Dead Letter Exchange for failed messages
    public static final String DLX_EXCHANGE = "task.dlx.exchange";
    
    // Queue names (one per task type)
    public static final String COMPUTE_QUEUE = "compute.queue";
    public static final String IO_QUEUE = "io.queue";
    public static final String AI_QUEUE = "ai.queue";
    
    // Dead Letter Queue
    public static final String DLQ = "task.dlq";
    
    // Routing keys
    public static final String COMPUTE_ROUTING_KEY = "task.compute";
    public static final String IO_ROUTING_KEY = "task.io";
    public static final String AI_ROUTING_KEY = "task.ai";
    
    /**
     * Get routing key for task type
     */
    public static String getRoutingKey(TaskType taskType) {
        return switch (taskType) {
            case COMPUTE -> COMPUTE_ROUTING_KEY;
            case IO -> IO_ROUTING_KEY;
            case AI -> AI_ROUTING_KEY;
        };
    }
    
    /**
     * Get queue name for task type
     */
    public static String getQueueName(TaskType taskType) {
        return switch (taskType) {
            case COMPUTE -> COMPUTE_QUEUE;
            case IO -> IO_QUEUE;
            case AI -> AI_QUEUE;
        };
    }
    
    /**
     * JSON message converter for Task serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
    /**
     * Main exchange for task routing
     */
    @Bean
    public TopicExchange taskExchange() {
        return ExchangeBuilder
                .topicExchange(TASK_EXCHANGE)
                .durable(true)
                .build();
    }
    
    /**
     * Dead Letter Exchange
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder
                .directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }
    
    /**
     * COMPUTE Queue
     */
    @Bean
    public Queue computeQueue() {
        return QueueBuilder
                .durable(COMPUTE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    
    /**
     * IO Queue
     */
    @Bean
    public Queue ioQueue() {
        return QueueBuilder
                .durable(IO_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    
    /**
     * AI Queue
     */
    @Bean
    public Queue aiQueue() {
        return QueueBuilder
                .durable(AI_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }
    
    /**
     * Dead Letter Queue
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DLQ)
                .build();
    }
    
    /**
     * Bind COMPUTE queue to exchange
     */
    @Bean
    public Binding computeBinding(Queue computeQueue, TopicExchange taskExchange) {
        return BindingBuilder
                .bind(computeQueue)
                .to(taskExchange)
                .with(COMPUTE_ROUTING_KEY);
    }
    
    /**
     * Bind IO queue to exchange
     */
    @Bean
    public Binding ioBinding(Queue ioQueue, TopicExchange taskExchange) {
        return BindingBuilder
                .bind(ioQueue)
                .to(taskExchange)
                .with(IO_ROUTING_KEY);
    }
    
    /**
     * Bind AI queue to exchange
     */
    @Bean
    public Binding aiBinding(Queue aiQueue, TopicExchange taskExchange) {
        return BindingBuilder
                .bind(aiQueue)
                .to(taskExchange)
                .with(AI_ROUTING_KEY);
    }
    
    /**
     * Bind Dead Letter Queue to DLX
     */
    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange dlxExchange) {
        return BindingBuilder
                .bind(deadLetterQueue)
                .to(dlxExchange)
                .with("dlq");
    }
    
    /**
     * RabbitMQ Admin for queue management
     */
    @Bean
    public org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new org.springframework.amqp.rabbit.core.RabbitAdmin(connectionFactory);
    }
    
    /**
     * Listener container factory for COMPUTE queue
     * Uses computeTaskExecutor thread pool
     */
    @Bean
    public SimpleRabbitListenerContainerFactory computeListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("computeTaskExecutor") ThreadPoolTaskExecutor computeExecutor
    ) {
        return createListenerContainerFactory(connectionFactory, computeExecutor);
    }
    
    /**
     * Listener container factory for IO queue
     * Uses ioTaskExecutor thread pool
     */
    @Bean
    public SimpleRabbitListenerContainerFactory ioListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("ioTaskExecutor") ThreadPoolTaskExecutor ioExecutor
    ) {
        return createListenerContainerFactory(connectionFactory, ioExecutor);
    }
    
    /**
     * Listener container factory for AI queue
     * Uses aiTaskExecutor thread pool
     */
    @Bean
    public SimpleRabbitListenerContainerFactory aiListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("aiTaskExecutor") ThreadPoolTaskExecutor aiExecutor
    ) {
        return createListenerContainerFactory(connectionFactory, aiExecutor);
    }
    
    /**
     * Helper method to create listener container factory
     */
    private SimpleRabbitListenerContainerFactory createListenerContainerFactory(
            ConnectionFactory connectionFactory,
            ThreadPoolTaskExecutor executor
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        
        // Use our configured thread pools for message processing
        factory.setTaskExecutor(executor);
        
        // Concurrency settings (matches thread pool size)
        factory.setConcurrentConsumers(executor.getCorePoolSize());
        factory.setMaxConcurrentConsumers(executor.getMaxPoolSize());
        
        // Prefetch count: number of unacked messages per consumer
        factory.setPrefetchCount(10);
        
        // Acknowledge mode: AUTO (ack after successful processing)
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.AUTO);
        
        return factory;
    }
}
