package com.asfaw.Orchestrator_Worker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration for thread pools used by workers.
 * Separate thread pools for each worker type to ensure isolation and optimal resource allocation.
 * 
 * Thread pool sizing considerations:
 * - COMPUTE: CPU-bound tasks, pool size ~ number of CPU cores
 * - IO: I/O-bound tasks, larger pool to handle blocking operations
 * - AI: ML/AI tasks, moderate pool size based on model complexity
 * 
 * @author TaskForge Team
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {
    
    @Value("${worker.compute.pool.core-size:2}")
    private int computePoolCoreSize;
    
    @Value("${worker.compute.pool.max-size:4}")
    private int computePoolMaxSize;
    
    @Value("${worker.io.pool.core-size:5}")
    private int ioPoolCoreSize;
    
    @Value("${worker.io.pool.max-size:10}")
    private int ioPoolMaxSize;
    
    @Value("${worker.ai.pool.core-size:3}")
    private int aiPoolCoreSize;
    
    @Value("${worker.ai.pool.max-size:6}")
    private int aiPoolMaxSize;
    
    @Value("${worker.pool.queue-capacity:100}")
    private int queueCapacity;
    
    /**
     * Thread pool for COMPUTE workers.
     * Optimized for CPU-intensive tasks.
     */
    @Bean(name = "computeTaskExecutor")
    public ThreadPoolTaskExecutor computeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(computePoolCoreSize);
        executor.setMaxPoolSize(computePoolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("compute-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Compute TaskExecutor initialized: core={}, max={}, queue={}", 
                computePoolCoreSize, computePoolMaxSize, queueCapacity);
        
        return executor;
    }
    
    /**
     * Thread pool for IO workers.
     * Larger pool size to handle blocking I/O operations efficiently.
     */
    @Bean(name = "ioTaskExecutor")
    public ThreadPoolTaskExecutor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ioPoolCoreSize);
        executor.setMaxPoolSize(ioPoolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("io-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("IO TaskExecutor initialized: core={}, max={}, queue={}", 
                ioPoolCoreSize, ioPoolMaxSize, queueCapacity);
        
        return executor;
    }
    
    /**
     * Thread pool for AI workers.
     * Moderate pool size for ML/AI inference tasks.
     */
    @Bean(name = "aiTaskExecutor")
    public ThreadPoolTaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(aiPoolCoreSize);
        executor.setMaxPoolSize(aiPoolMaxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("ai-worker-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("AI TaskExecutor initialized: core={}, max={}, queue={}", 
                aiPoolCoreSize, aiPoolMaxSize, queueCapacity);
        
        return executor;
    }
}
