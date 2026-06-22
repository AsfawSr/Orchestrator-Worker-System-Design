package com.asfaw.Orchestrator_Worker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application configuration.
 * Enables async processing and scheduled tasks for the orchestrator-worker system.
 * 
 * @author TaskForge Team
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AppConfig {
    // Additional beans and configurations can be added here
}
