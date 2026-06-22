package com.asfaw.Orchestrator_Worker.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Configuration for caching, distributed locks, and session management.
 * 
 * Features:
 * - Task result caching with configurable TTL
 * - JSON serialization for complex objects
 * - RedisTemplate for manual cache operations
 * - CacheManager for @Cacheable annotations
 * 
 * @author TaskForge Team
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {
    
    /**
     * Configure ObjectMapper for Redis JSON serialization
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());
        
        // Enable polymorphic type handling for inheritance
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        return mapper;
    }
    
    /**
     * RedisTemplate for manual Redis operations
     * Used for custom caching logic and data access
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        log.info("RedisTemplate configured with JSON serialization");
        return template;
    }
    
    /**
     * CacheManager for Spring Cache abstraction (@Cacheable, @CacheEvict, etc.)
     * Supports multiple caches with different TTL configurations
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper
    ) {
        // JSON serializer
        GenericJackson2JsonRedisSerializer jsonSerializer = 
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        // Default cache configuration (10 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                jsonSerializer
                        )
                )
                .disableCachingNullValues();
        
        // Create cache manager with custom configurations per cache
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Task results cache: 30 minutes
                .withCacheConfiguration("taskResults",
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // Task status cache: 5 seconds (frequently updated)
                .withCacheConfiguration("taskStatus",
                        defaultConfig.entryTtl(Duration.ofSeconds(5)))
                // Statistics cache: 1 minute
                .withCacheConfiguration("statistics",
                        defaultConfig.entryTtl(Duration.ofMinutes(1)))
                // Worker pool stats cache: 10 seconds
                .withCacheConfiguration("poolStats",
                        defaultConfig.entryTtl(Duration.ofSeconds(10)))
                .build();
        
        log.info("RedisCacheManager configured with multiple cache regions");
        return cacheManager;
    }
}
