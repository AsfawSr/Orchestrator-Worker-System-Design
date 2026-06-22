package com.asfaw.Orchestrator_Worker.task;

/**
 * Enumeration representing the lifecycle status of a task in the system.
 * 
 * @author TaskForge Team
 */
public enum TaskStatus {
    /**
     * Task has been submitted but not yet picked up by a worker
     */
    PENDING,
    
    /**
     * Task is currently being processed by a worker
     */
    RUNNING,
    
    /**
     * Task has completed successfully
     */
    COMPLETED,
    
    /**
     * Task has failed after all retry attempts
     */
    FAILED,
    
    /**
     * Task has been cancelled by user or system
     */
    CANCELLED
}
