package com.asfaw.Orchestrator_Worker.monitoring;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Monitors and logs thread pool statistics.
 * Provides visibility into pool utilization, active threads, and queue depths.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThreadPoolMonitor {
    
    private final Map<TaskType, ThreadPoolTaskExecutor> taskExecutors;
    
    /**
     * Log thread pool statistics every 30 seconds
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void logPoolStatistics() {
        log.info("========== Thread Pool Statistics ==========");
        
        taskExecutors.forEach((taskType, executor) -> {
            ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
            
            log.info("{} Pool → Active: {}/{}, Queue: {}/{}, Completed: {}, Total: {}", 
                    taskType,
                    pool.getActiveCount(),
                    pool.getMaximumPoolSize(),
                    pool.getQueue().size(),
                    pool.getQueue().remainingCapacity() + pool.getQueue().size(),
                    pool.getCompletedTaskCount(),
                    pool.getTaskCount()
            );
        });
        
        log.info("============================================");
    }
    
    /**
     * Get detailed statistics for a specific pool
     */
    public PoolStatistics getPoolStatistics(TaskType taskType) {
        ThreadPoolTaskExecutor executor = taskExecutors.get(taskType);
        if (executor == null) {
            return null;
        }
        
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
        
        return PoolStatistics.builder()
                .taskType(taskType)
                .corePoolSize(pool.getCorePoolSize())
                .maxPoolSize(pool.getMaximumPoolSize())
                .currentPoolSize(pool.getPoolSize())
                .activeThreads(pool.getActiveCount())
                .queueSize(pool.getQueue().size())
                .queueCapacity(pool.getQueue().remainingCapacity() + pool.getQueue().size())
                .completedTasks(pool.getCompletedTaskCount())
                .totalTasks(pool.getTaskCount())
                .largestPoolSize(pool.getLargestPoolSize())
                .build();
    }
    
    /**
     * Get statistics for all pools
     */
    public Map<TaskType, PoolStatistics> getAllPoolStatistics() {
        Map<TaskType, PoolStatistics> stats = new java.util.HashMap<>();
        
        taskExecutors.forEach((taskType, executor) -> {
            stats.put(taskType, getPoolStatistics(taskType));
        });
        
        return stats;
    }
}
