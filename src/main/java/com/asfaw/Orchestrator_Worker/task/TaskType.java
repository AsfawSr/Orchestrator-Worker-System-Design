package com.asfaw.Orchestrator_Worker.task;

/**
 * Enumeration representing different types of tasks that can be processed.
 * Each type is handled by a specialized worker.
 * 
 * @author TaskForge Team
 */
public enum TaskType {
    /**
     * CPU-intensive computational tasks (e.g., mathematical calculations, data processing)
     */
    COMPUTE,
    
    /**
     * I/O-bound tasks (e.g., file operations, database queries, API calls)
     */
    IO,
    
    /**
     * AI/ML related tasks (e.g., model inference, data analysis)
     */
    AI
}
