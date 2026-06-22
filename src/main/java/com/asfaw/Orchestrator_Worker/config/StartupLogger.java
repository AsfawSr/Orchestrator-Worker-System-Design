package com.asfaw.Orchestrator_Worker.config;

import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Logs thread pool configuration on application startup.
 * Provides visibility into what pools are configured and their settings.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupLogger {
    
    private final Map<TaskType, ThreadPoolTaskExecutor> taskExecutors;
    private final ThreadPoolProperties properties;
    
    @EventListener(ApplicationReadyEvent.class)
    public void logThreadPoolConfiguration() {
        log.info("");
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║        THREAD POOL CONFIGURATION - ORCHESTRATOR-WORKER        ║");
        log.info("╚═══════════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("Total Thread Pools Configured: {}", taskExecutors.size());
        log.info("");
        
        taskExecutors.forEach((taskType, executor) -> {
            String typeName = taskType.name().toLowerCase();
            ThreadPoolProperties.PoolConfig config = properties.getPools().get(typeName);
            
            if (config == null) {
                config = ThreadPoolProperties.PoolConfig.getDefault();
            }
            
            log.info("┌─ {} Pool ─────────────────────────", taskType);
            log.info("│  Core Threads:      {}", config.getCoreSize());
            log.info("│  Max Threads:       {}", config.getMaxSize());
            log.info("│  Queue Capacity:    {}", config.getQueueCapacity());
            log.info("│  Await Termination: {}s", config.getAwaitTerminationSeconds());
            log.info("│  Thread Prefix:     {}-worker-", typeName);
            log.info("└──────────────────────────────────────────");
            log.info("");
        });
        
        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║                   MONITORING ENDPOINTS                        ║");
        log.info("╚═══════════════════════════════════════════════════════════════╝");
        log.info("");
        log.info("  Pool Statistics:     GET /api/monitoring/pools");
        log.info("  Specific Pool:       GET /api/monitoring/pools/{{taskType}}");
        log.info("  System Overview:     GET /api/monitoring/overview");
        log.info("  Task Statistics:     GET /api/tasks/stats");
        log.info("  Health Check:        GET /api/tasks/health");
        log.info("");
        log.info("════════════════════════════════════════════════════════════════");
        log.info("");
    }
}
