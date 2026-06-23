package com.asfaw.Orchestrator_Worker.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual Redisson Configuration for Distributed Locks.
 * 
 * We manually configure Redisson because redisson-spring-boot-starter
 * has compatibility issues with Spring Boot 4.1.0.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Configuration
public class RedissonConfig {
    
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // Configure single server
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000)
                .setConnectTimeout(10000);
        
        RedissonClient redissonClient = Redisson.create(config);
        
        log.info("Redisson client configured for Redis at {}:{}", redisHost, redisPort);
        
        return redissonClient;
    }
}
