package com.asfaw.Orchestrator_Worker.controller;

import com.asfaw.Orchestrator_Worker.monitoring.PoolStatistics;
import com.asfaw.Orchestrator_Worker.monitoring.ThreadPoolMonitor;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for monitoring thread pools and system health.
 * Provides real-time visibility into thread pool utilization and performance.
 * 
 * @author TaskForge Team
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    
    private final ThreadPoolMonitor threadPoolMonitor;
    
    /**
     * Get statistics for all thread pools
     * 
     * GET /api/monitoring/pools
     * 
     * @return pool statistics for all task types
     */
    @GetMapping("/pools")
    public ResponseEntity<Map<String, Object>> getAllPoolStats() {
        log.debug("Fetching statistics for all pools");
        
        Map<TaskType, PoolStatistics> poolStats = threadPoolMonitor.getAllPoolStatistics();
        
        Map<String, Object> response = new HashMap<>();
        poolStats.forEach((taskType, stats) -> {
            Map<String, Object> poolInfo = new HashMap<>();
            poolInfo.put("taskType", stats.getTaskType());
            poolInfo.put("configuration", Map.of(
                    "corePoolSize", stats.getCorePoolSize(),
                    "maxPoolSize", stats.getMaxPoolSize(),
                    "queueCapacity", stats.getQueueCapacity()
            ));
            poolInfo.put("current", Map.of(
                    "poolSize", stats.getCurrentPoolSize(),
                    "activeThreads", stats.getActiveThreads(),
                    "queueSize", stats.getQueueSize(),
                    "largestPoolSize", stats.getLargestPoolSize()
            ));
            poolInfo.put("metrics", Map.of(
                    "completedTasks", stats.getCompletedTasks(),
                    "totalTasks", stats.getTotalTasks(),
                    "threadUtilization", String.format("%.2f%%", stats.getThreadUtilization()),
                    "queueUtilization", String.format("%.2f%%", stats.getQueueUtilization())
            ));
            poolInfo.put("status", Map.of(
                    "idle", stats.isIdle(),
                    "underPressure", stats.isUnderPressure()
            ));
            
            response.put(taskType.name().toLowerCase(), poolInfo);
        });
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get statistics for a specific pool
     * 
     * GET /api/monitoring/pools/{taskType}
     * 
     * @param taskType the task type (COMPUTE, IO, AI, etc.)
     * @return pool statistics for the specified type
     */
    @GetMapping("/pools/{taskType}")
    public ResponseEntity<Map<String, Object>> getPoolStats(@PathVariable TaskType taskType) {
        log.debug("Fetching statistics for {} pool", taskType);
        
        PoolStatistics stats = threadPoolMonitor.getPoolStatistics(taskType);
        
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("taskType", stats.getTaskType());
        response.put("configuration", Map.of(
                "corePoolSize", stats.getCorePoolSize(),
                "maxPoolSize", stats.getMaxPoolSize(),
                "queueCapacity", stats.getQueueCapacity()
        ));
        response.put("current", Map.of(
                "poolSize", stats.getCurrentPoolSize(),
                "activeThreads", stats.getActiveThreads(),
                "queueSize", stats.getQueueSize(),
                "largestPoolSize", stats.getLargestPoolSize()
        ));
        response.put("metrics", Map.of(
                "completedTasks", stats.getCompletedTasks(),
                "totalTasks", stats.getTotalTasks(),
                "threadUtilization", String.format("%.2f%%", stats.getThreadUtilization()),
                "queueUtilization", String.format("%.2f%%", stats.getQueueUtilization())
        ));
        response.put("status", Map.of(
                "idle", stats.isIdle(),
                "underPressure", stats.isUnderPressure()
        ));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get system overview with all pools
     * 
     * GET /api/monitoring/overview
     * 
     * @return system-wide overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        log.debug("Fetching system overview");
        
        Map<TaskType, PoolStatistics> poolStats = threadPoolMonitor.getAllPoolStatistics();
        
        int totalActiveThreads = 0;
        int totalMaxThreads = 0;
        int totalQueueSize = 0;
        int totalQueueCapacity = 0;
        long totalCompleted = 0;
        int poolsUnderPressure = 0;
        
        for (PoolStatistics stats : poolStats.values()) {
            totalActiveThreads += stats.getActiveThreads();
            totalMaxThreads += stats.getMaxPoolSize();
            totalQueueSize += stats.getQueueSize();
            totalQueueCapacity += stats.getQueueCapacity();
            totalCompleted += stats.getCompletedTasks();
            if (stats.isUnderPressure()) {
                poolsUnderPressure++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalPools", poolStats.size());
        response.put("threads", Map.of(
                "active", totalActiveThreads,
                "maximum", totalMaxThreads,
                "utilization", String.format("%.2f%%", 
                        totalMaxThreads > 0 ? (double) totalActiveThreads / totalMaxThreads * 100 : 0)
        ));
        response.put("queues", Map.of(
                "totalPending", totalQueueSize,
                "totalCapacity", totalQueueCapacity,
                "utilization", String.format("%.2f%%", 
                        totalQueueCapacity > 0 ? (double) totalQueueSize / totalQueueCapacity * 100 : 0)
        ));
        response.put("tasks", Map.of(
                "completedTotal", totalCompleted
        ));
        response.put("health", Map.of(
                "poolsUnderPressure", poolsUnderPressure,
                "status", poolsUnderPressure > 0 ? "WARNING" : "HEALTHY"
        ));
        
        return ResponseEntity.ok(response);
    }
}
