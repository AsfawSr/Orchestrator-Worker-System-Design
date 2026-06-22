package com.asfaw.Orchestrator_Worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for thread pools.
 * Allows flexible configuration of thread pools for any task type via application.yaml.
 * 
 * Example configuration:
 * <pre>
 * worker:
 *   pools:
 *     compute:
 *       core-size: 2
 *       max-size: 4
 *       queue-capacity: 100
 *       await-termination-seconds: 60
 *     io:
 *       core-size: 5
 *       max-size: 10
 *       queue-capacity: 100
 *       await-termination-seconds: 60
 *     ai:
 *       core-size: 3
 *       max-size: 6
 *       queue-capacity: 100
 *       await-termination-seconds: 60
 *     # Add new task types easily:
 *     video:
 *       core-size: 2
 *       max-size: 4
 *       queue-capacity: 50
 *       await-termination-seconds: 120
 * </pre>
 * 
 * @author TaskForge Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "worker")
public class ThreadPoolProperties {
    
    /**
     * Map of pool configurations keyed by task type name (lowercase).
     * e.g., "compute", "io", "ai", "video", etc.
     */
    private Map<String, PoolConfig> pools = new HashMap<>();
    
    /**
     * Configuration for a single thread pool
     */
    @Data
    public static class PoolConfig {
        /**
         * Core pool size - threads always alive
         */
        private int coreSize = 2;
        
        /**
         * Maximum pool size - max threads under load
         */
        private int maxSize = 4;
        
        /**
         * Queue capacity before rejecting tasks
         */
        private int queueCapacity = 100;
        
        /**
         * Seconds to wait for tasks to complete during shutdown
         */
        private int awaitTerminationSeconds = 60;
        
        /**
         * Get default configuration
         */
        public static PoolConfig getDefault() {
            PoolConfig config = new PoolConfig();
            config.setCoreSize(2);
            config.setMaxSize(4);
            config.setQueueCapacity(100);
            config.setAwaitTerminationSeconds(60);
            return config;
        }
    }
}
