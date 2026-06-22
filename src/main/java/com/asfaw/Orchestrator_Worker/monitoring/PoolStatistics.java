package com.asfaw.Orchestrator_Worker.monitoring;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.Builder;
import lombok.Data;

/**
 * Statistics for a single thread pool.
 * 
 * @author TaskForge Team
 */
@Data
@Builder
public class PoolStatistics {
    
    /**
     * Task type this pool handles
     */
    private TaskType taskType;
    
    /**
     * Core pool size (minimum threads)
     */
    private int corePoolSize;
    
    /**
     * Maximum pool size
     */
    private int maxPoolSize;
    
    /**
     * Current number of threads in pool
     */
    private int currentPoolSize;
    
    /**
     * Number of threads actively executing tasks
     */
    private int activeThreads;
    
    /**
     * Current queue size (pending tasks)
     */
    private int queueSize;
    
    /**
     * Total queue capacity
     */
    private int queueCapacity;
    
    /**
     * Total completed tasks
     */
    private long completedTasks;
    
    /**
     * Total tasks submitted
     */
    private long totalTasks;
    
    /**
     * Largest pool size ever reached
     */
    private int largestPoolSize;
    
    /**
     * Calculate utilization percentage
     */
    public double getThreadUtilization() {
        if (maxPoolSize == 0) return 0.0;
        return (double) activeThreads / maxPoolSize * 100.0;
    }
    
    /**
     * Calculate queue utilization percentage
     */
    public double getQueueUtilization() {
        if (queueCapacity == 0) return 0.0;
        return (double) queueSize / queueCapacity * 100.0;
    }
    
    /**
     * Check if pool is idle
     */
    public boolean isIdle() {
        return activeThreads == 0 && queueSize == 0;
    }
    
    /**
     * Check if pool is under pressure
     */
    public boolean isUnderPressure() {
        return getQueueUtilization() > 80.0 || getThreadUtilization() > 90.0;
    }
}
