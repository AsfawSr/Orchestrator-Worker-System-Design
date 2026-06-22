package com.asfaw.Orchestrator_Worker.config;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Flexible configuration for thread pools used by workers.
 * Dynamically creates thread pools based on configuration for each task type.
 * 
 * Benefits of flexible approach:
 * - Add new task types without code changes
 * - Configure each pool independently via application.yaml
 * - Centralized thread pool management
 * - Easy to tune performance per task type
 * 
 * Thread pool sizing guidelines:
 * - COMPUTE: CPU-bound tasks, pool size ~ number of CPU cores
 * - IO: I/O-bound tasks, larger pool to handle blocking operations
 * - AI: ML/AI tasks, moderate pool size based on model complexity
 * 
 * @author TaskForge Team
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {
    
    /**
     * Creates thread pool executors for each task type based on configuration.
     * This map can be injected wherever you need access to all executors.
     * 
     * @param properties the thread pool configuration properties
     * @return Map of task type to thread pool executor
     */
    @Bean
    public Map<TaskType, ThreadPoolTaskExecutor> taskExecutors(ThreadPoolProperties properties) {
        Map<TaskType, ThreadPoolTaskExecutor> executors = new HashMap<>();
        
        // Create a thread pool for each configured task type
        for (TaskType taskType : TaskType.values()) {
            String typeName = taskType.name().toLowerCase();
            ThreadPoolProperties.PoolConfig poolConfig = properties.getPools().get(typeName);
            
            if (poolConfig == null) {
                log.warn("No thread pool configuration found for {}, using defaults", taskType);
                poolConfig = ThreadPoolProperties.PoolConfig.getDefault();
            }
            
            ThreadPoolTaskExecutor executor = createExecutor(
                    taskType, 
                    poolConfig.getCoreSize(), 
                    poolConfig.getMaxSize(), 
                    poolConfig.getQueueCapacity(),
                    poolConfig.getAwaitTerminationSeconds()
            );
            
            executors.put(taskType, executor);
            
            log.info("{} TaskExecutor initialized: core={}, max={}, queue={}, termination={}s", 
                    taskType, 
                    poolConfig.getCoreSize(), 
                    poolConfig.getMaxSize(), 
                    poolConfig.getQueueCapacity(),
                    poolConfig.getAwaitTerminationSeconds());
        }
        
        return executors;
    }
    
    /**
     * Individual bean definitions for backward compatibility.
     * These allow injection by name (e.g., @Qualifier("computeTaskExecutor"))
     */
    @Bean(name = "computeTaskExecutor")
    public ThreadPoolTaskExecutor computeTaskExecutor(Map<TaskType, ThreadPoolTaskExecutor> executors) {
        return executors.get(TaskType.COMPUTE);
    }
    
    @Bean(name = "ioTaskExecutor")
    public ThreadPoolTaskExecutor ioTaskExecutor(Map<TaskType, ThreadPoolTaskExecutor> executors) {
        return executors.get(TaskType.IO);
    }
    
    @Bean(name = "aiTaskExecutor")
    public ThreadPoolTaskExecutor aiTaskExecutor(Map<TaskType, ThreadPoolTaskExecutor> executors) {
        return executors.get(TaskType.AI);
    }
    
    /**
     * Creates and configures a thread pool executor with the given parameters.
     * 
     * @param taskType the task type this executor handles
     * @param coreSize core pool size
     * @param maxSize maximum pool size
     * @param queueCapacity task queue capacity
     * @param awaitTerminationSeconds seconds to wait during shutdown
     * @return configured thread pool executor
     */
    private ThreadPoolTaskExecutor createExecutor(
            TaskType taskType, 
            int coreSize, 
            int maxSize, 
            int queueCapacity,
            int awaitTerminationSeconds) {
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(taskType.name().toLowerCase() + "-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.initialize();
        
        return executor;
    }
}
